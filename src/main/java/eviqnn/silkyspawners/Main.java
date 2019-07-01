package eviqnn.silkyspawners;

import eviqnn.silkyspawners.util.BlockUtil;
import eviqnn.silkyspawners.util.References;

import net.minecraft.block.Block;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Objects;
import java.util.Properties;


@Mod(modid = References.MOD_ID, name = References.NAME, version = References.VERSION, acceptableRemoteVersions = "*")
public class Main {

	@Instance
	public static Main instance;

	public static Logger logger;

	private static boolean canDebug = false;

	private static File config;

	private static void readProperties()
	{
		try {
			Properties lProperties = new Properties();
			if (config.exists())
			{
				lProperties.load(new FileInputStream(config));
				String debug = lProperties.getProperty("debug", "false");
				lProperties.setProperty("debug", debug);
				FileOutputStream out = new FileOutputStream(config);
				lProperties.store(out, "Set \"debug\" to true for debug info");
				out.close();
				canDebug = Boolean.getBoolean(debug);
			}
			else
			{
				lProperties.setProperty("debug", "false");
				FileOutputStream out = new FileOutputStream(config);
				lProperties.store(out, "Set \"debug\" to true for debug info");
			}
		} catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event)
	{
		config = event.getSuggestedConfigurationFile();
		logger = event.getModLog();
		readProperties();
	}

	@EventHandler
	public static void init(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(instance);
	}

	@EventHandler
	public static void postinit(FMLPostInitializationEvent event)
	{
		logger.info("SilkySpawners has loaded with no errors.");
	}

	@SubscribeEvent
	public void blockDrop(BlockEvent.BreakEvent e)
	{
		// Code Cleanup
		Block block = e.getState().getBlock();
		EntityPlayer player = e.getPlayer();
		World world = e.getWorld();
		BlockPos pos = e.getPos();
		TileEntity tile = world.getTileEntity(pos);
		EnumHand hand = player.getActiveHand();
		ItemStack item = player.getHeldItem(hand);

		if (!checkValid(block, player, world, pos, tile, hand, item))
		{
			debug("Invalid parameters!");
			return;
		}

		if (world.isRemote)
		{
			debug("World is remote");
			return;
		}

		if(!world.getGameRules().getBoolean("doTileDrops"))
		{
			// Spams debug console
			debug("DoTileDrops is off");
			return;
		}

		int toollevel = item.getItem().getHarvestLevel(item, "pickaxe", player, e.getState());
		Enchantment SILKTOUCH = Enchantment.getEnchantmentByLocation("minecraft:silk_touch");
		if(EnchantmentHelper.getEnchantmentLevel(SILKTOUCH, player.getHeldItem(hand)) <= 0
				||  toollevel < BlockUtil.pickaxeLevel(e.getState()) )
		{
			debug("Not valid tool");
			return;
		}

		if(block instanceof BlockMobSpawner || tile instanceof TileEntityMobSpawner)
		{
			breakSpawner(block, world, pos, tile, e);
		}
	}

	@SubscribeEvent
	public void blockPlace(BlockEvent.EntityPlaceEvent e)
	{
		IBlockState state = e.getPlacedBlock();
		Entity entity = e.getEntity();
		BlockPos blockPos = e.getPos();
		World world = entity.getEntityWorld();
		TileEntity tile = world.getTileEntity(blockPos);

		EnumHand hand;
		ItemStack item;
		
		// Debug purposes only
		// logger.debug("Blockstate: " + state + "\nEntity: " + entity + "\nBlockPos: " + blockPos + "\nWorld: " + world + "\nTileEntity: " + tile);
		
		if (entity instanceof EntityLiving)
		{
			hand = ((EntityLiving) entity).getActiveHand();
			item = ((EntityLiving) entity).getHeldItem(hand);
		} else { 
			return; 
		}
		
		// Debug purposes only
		// logger.debug("hand: " + hand + "\nItemStack: " + item);
		// logger.debug(Blocks.MOB_SPAWNER.getLocalizedName() + state.getBlock().getLocalizedName());


		if (Objects.equals(Blocks.MOB_SPAWNER.getRegistryName(), state.getBlock().getRegistryName()))
		{
			if (tile instanceof TileEntityMobSpawner)
			{
				TileEntityMobSpawner tileEntityMobSpawner = (TileEntityMobSpawner) tile;
				NBTTagCompound nbtBlock = item.getSubCompound("BlockEntityTag");
				
				// Debug purposes only
				debug("NBTItem: " + nbtBlock + "\nTile Entity Mob Spawner: " + tileEntityMobSpawner);

				if (nbtBlock != null)
				{
					NBTTagCompound nbtTileEntityNew = new NBTTagCompound();
					debug("NBTNew (1): " + nbtTileEntityNew);

					nbtTileEntityNew = tileEntityMobSpawner.writeToNBT(nbtTileEntityNew);
					debug("NBTNew (2): " + nbtTileEntityNew);

					NBTTagCompound nbtTileEntityOld = nbtTileEntityNew.copy();
					debug("NBTOld: " + nbtTileEntityNew);

					nbtTileEntityNew.merge(nbtBlock);
					debug("NBTNew (3): " + nbtTileEntityNew);

					nbtTileEntityNew.setInteger("x", blockPos.getX());
					nbtTileEntityNew.setInteger("y", blockPos.getY());
					nbtTileEntityNew.setInteger("z", blockPos.getZ());

					debug("NBTNew (4): " + nbtTileEntityNew);

					if (!nbtTileEntityNew.equals(nbtTileEntityOld))
					{
						debug("NBTNew (5): " + nbtTileEntityNew);
						tile.readFromNBT(nbtTileEntityNew);
						tile.markDirty();
					}
				}
			}
		}
	}

	private boolean checkValid(Block block, EntityPlayer player, World world, BlockPos pos, TileEntity tile, EnumHand hand, ItemStack item)
	{
		boolean result = true;
		
		if(tile == null)
		{
			logger.debug("Tile entity is null");
			result = false;
		}
		
		if (block == null)
		{
			logger.error("Block is null");
			result = false;
		}

		if (world == null)
		{
			logger.error("World is null");
			result = false;
		}

		if (pos == null)
		{
			logger.error("Position is null");
			result = false;
		}

		if (player == null)
		{
			logger.error("Player is null");
			result = false;
		}

		if(hand == null)
		{
			logger.error("Active hand is null");
			result = false;
		}

		if(item == null)
		{
			logger.error("Held item is null");
			result = false;
		}

		if(tile == null)
		{
			debug("Tile entity is null");
			result = false;
		}
		return result;
	}

	private void breakSpawner(Block block, World world, BlockPos pos, TileEntity tile, BlockEvent.BreakEvent e)
	{
		e.setCanceled(true);
		debug("Monster Spawner!");
		TileEntityMobSpawner lEntityMobSpawner = (TileEntityMobSpawner) tile;
		NBTTagCompound tileData = new NBTTagCompound();
		tileData = lEntityMobSpawner.getSpawnerBaseLogic().writeToNBT(tileData);
		debug("tile data: " + tileData);
		int meta = block.getMetaFromState(e.getState());
		debug("meta: " + meta);

		if(meta < 0)
		{
			debug("meta reduced to 0");
			meta = 0;
		}

		ItemStack stack = new ItemStack(block,1, meta);
		world.setBlockToAir(pos);
		stack.setTagInfo("BlockEntityTag", tileData);
		BlockUtil.blockDrop(world, pos, stack);
	}

	private void debug(Object object)
	{
		if (canDebug)
		{
			logger.debug(object);
		}
	}
}

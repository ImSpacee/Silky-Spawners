package eviqnn.silkyspawners;

import eviqnn.silkyspawners.util.BlockUtil;
import eviqnn.silkyspawners.util.Debug;
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
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.Logger;

import java.util.Objects;


@Mod(modid = References.MOD_ID, name = References.NAME, version = References.VERSION, acceptableRemoteVersions = "*")
public class Main {

	@Instance
	public static Main instance;

	public static Logger logger;

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event)
	{
		Debug.config = event.getSuggestedConfigurationFile();
		logger = event.getModLog();
		Debug.readProperties();
		Debug.debug("Pre Initialization!");
	}

	@EventHandler
	public static void init(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(instance);
		Debug.debug("Initialization!");
	}

	@EventHandler
	public static void postinit(FMLPostInitializationEvent event)
	{
		Debug.debug("Post Initialization!");
	}

	@EventHandler
	public static void loadcomplete(FMLLoadCompleteEvent event)
	{
		Debug.debug("Load Complete!");
		logger.info("SilkySpawners has loaded with no errors.");
	}

	@SubscribeEvent
	public void blockBreak(BlockEvent.BreakEvent e)
	{
		Debug.debug("Block Break!");
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
			// Debug purposes only
			Debug.debug("Invalid parameters!");
			return;
		}

		if (world.isRemote)
		{
			// Debug purposes only
			// Spams debug console on client
			Debug.debug("World is remote");
			return;
		}

		if(!world.getGameRules().getBoolean("doTileDrops"))
		{
			// Debug purposes only
			// Spams debug console
			Debug.debug("DoTileDrops is off");
			return;
		}

		int toollevel = item.getItem().getHarvestLevel(item, "pickaxe", player, e.getState());
		Enchantment SILKTOUCH = Enchantment.getEnchantmentByLocation("minecraft:silk_touch");
		if(EnchantmentHelper.getEnchantmentLevel(SILKTOUCH, player.getHeldItem(hand)) <= 0
				||  toollevel < BlockUtil.pickaxeLevel(e.getState()) )
		{
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
		Debug.debug("Block Place!");
		IBlockState state = e.getPlacedBlock();
		Entity entity = e.getEntity();
		BlockPos blockPos = e.getPos();
		World world = entity.getEntityWorld();
		TileEntity tile = world.getTileEntity(blockPos);

		EnumHand hand;
		ItemStack item;

		// Debug purposes only
		Debug.debug("Blockstate: " + state + "\nEntity: " + entity + "\nBlockPos: " + blockPos + "\nWorld: " + world + "\nTileEntity: " + tile);

		if (entity instanceof EntityLiving)
		{
			hand = ((EntityLiving) entity).getActiveHand();
			item = ((EntityLiving) entity).getHeldItem(hand);
		} else if (entity instanceof EntityPlayer)
		{
			hand = ((EntityPlayer) entity).getActiveHand();
			item = ((EntityPlayer) entity).getHeldItem(hand);
		} else {
			return;
		}

		// Debug purposes only
		Debug.debug("hand: " + hand + "\nItemStack: " + item);
		Debug.debug(Blocks.MOB_SPAWNER.getLocalizedName() + state.getBlock().getLocalizedName());

		if (Objects.equals(Blocks.MOB_SPAWNER.getRegistryName(), state.getBlock().getRegistryName()))
		{
			if (tile instanceof TileEntityMobSpawner)
			{
				TileEntityMobSpawner TileEntityMobSpawner = (TileEntityMobSpawner) tile;
				NBTTagCompound nbtBlock = item.getSubCompound("BlockEntityTag");

				// Debug purposes only
				Debug.debug("NBTItem: " + nbtBlock + "\nTile Entity Mob Spawner: " + TileEntityMobSpawner);

				if (nbtBlock != null)
				{
					NBTTagCompound nbtTileEntityNew = new NBTTagCompound();
					// Debug purposes only
					Debug.debug("NBTNew (1): " + nbtTileEntityNew);

					nbtTileEntityNew = TileEntityMobSpawner.writeToNBT(nbtTileEntityNew);
					// Debug purposes only
					Debug.debug("NBTNew (2): " + nbtTileEntityNew);

					NBTTagCompound nbtTileEntityOld = nbtTileEntityNew.copy();
					// Debug purposes only
					Debug.debug("NBTOld: " + nbtTileEntityNew);

					nbtTileEntityNew.merge(nbtBlock);
					// Debug purposes only
					Debug.debug("NBTNew (3): " + nbtTileEntityNew);

					nbtTileEntityNew.setInteger("x", blockPos.getX());
					nbtTileEntityNew.setInteger("y", blockPos.getY());
					nbtTileEntityNew.setInteger("z", blockPos.getZ());
					// Debug purposes only
					Debug.debug("NBTNew (4): " + nbtTileEntityNew);

					if (!nbtTileEntityNew.equals(nbtTileEntityOld))
					{
						// Debug purposes only
						Debug.debug("NBTNew (5): " + nbtTileEntityNew);
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
			Debug.debug("Tile entity is null");
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

		return result;
	}

	private void breakSpawner(Block block, World world, BlockPos pos, TileEntity tile, BlockEvent.BreakEvent e)
	{
		e.setCanceled(true);
		Debug.debug("Monster Spawner!");
		TileEntityMobSpawner lEntityMobSpawner = (TileEntityMobSpawner) tile;
		NBTTagCompound tileData = new NBTTagCompound();
		tileData = lEntityMobSpawner.getSpawnerBaseLogic().writeToNBT(tileData);
		Debug.debug("tile data: " + tileData);
		int meta = block.getMetaFromState(e.getState());
		Debug.debug("meta: " + meta);

		if(meta < 0)
		{
			Debug.debug("meta reduced to 0");
			meta = 0;
		}

		ItemStack stack = new ItemStack(block,1, meta);
		world.setBlockToAir(pos);
		stack.setTagInfo("BlockEntityTag", tileData);
		NBTTagCompound display = new NBTTagCompound();
		MobSpawnerBaseLogic tileMobSpawn = ((TileEntityMobSpawner) tile).getSpawnerBaseLogic();
		Object spawnDataObj = ObfuscationReflectionHelper.getPrivateValue(MobSpawnerBaseLogic.class, tileMobSpawn, "spawnData");
		Debug.debug("Spawn Data: " + spawnDataObj);
		Debug.debug("Spawn Data Class: " + spawnDataObj.getClass());
		if (spawnDataObj instanceof WeightedSpawnerEntity)
		{
			WeightedSpawnerEntity spawnData = (WeightedSpawnerEntity) spawnDataObj;
			String entityIDstr = spawnData.getNbt().getString("id");
			String entityIDEntity = ResourceLocation.splitObjectName(entityIDstr)[1];
			entityIDEntity = entityIDEntity.replace("_", " ");
			entityIDEntity = WordUtils.capitalizeFully(entityIDEntity);
			display.setString("Name", "§r" + entityIDEntity + "§r" + " Spawner");
		}
		else
		{
			display.setString("Name", "§rERROR Spawner");
		}
		stack.setTagInfo("display", display);
		BlockUtil.blockDrop(world, pos, stack);
	}
}

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

import java.util.Objects;


@Mod(modid = References.MOD_ID, name = References.NAME, version = References.VERSION, acceptableRemoteVersions = "*")
public class Main {

	@Instance
	public static Main instance;

	public static Logger logger;

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
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
			logger.debug("Invalid parameters!");
			return;
		}

		if (world.isRemote)
		{
			// Disabled as it had spammed debug console on client
			//logger.debug("World is remote");
			return;
		}

		if(!world.getGameRules().getBoolean("doTileDrops"))
		{
			// Disabled as it had spammed debug console
			// logger.debug("DoTileDrops is off");
			return;
		}

		int toollevel = item.getItem().getHarvestLevel(item, "pickaxe", player, e.getState());
		Enchantment SILKTOUCH = Enchantment.getEnchantmentByLocation("minecraft:silk_touch");
		if(EnchantmentHelper.getEnchantmentLevel(SILKTOUCH, player.getHeldItem(hand)) <= 0
				||  toollevel < BlockUtil.pickaxeLevel(e.getState()) )
		{
			logger.debug("Not valid tool");
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
		IBlockState lState = e.getPlacedBlock();
		logger.debug("State: " + lState);

		Entity lEntity = e.getEntity();
		logger.debug("Entity: " + lEntity);

		BlockPos lBlockPos = e.getPos();
		logger.debug("BlockPos: " + lBlockPos);

		World world = lEntity.getEntityWorld();
		logger.debug("World: " + world);

		if (world == null)
		{
			return;
		}

		EnumHand hand;
		ItemStack item;
		if (lEntity instanceof EntityLiving)
		{
			hand = ((EntityLiving) lEntity).getActiveHand();
			item = ((EntityLiving) lEntity).getHeldItem(hand);
		}
		else if (lEntity instanceof EntityPlayer)
		{
			hand = ((EntityPlayer) lEntity).getActiveHand();
			item = ((EntityPlayer) lEntity).getHeldItem(hand);
		}
		else
		{
			return;
		}

		logger.debug("Hand: " + hand);

		logger.debug("Item: " + item);

		TileEntity lTileEntity = world.getTileEntity(lBlockPos);
		logger.debug("Tile Entity: " + lTileEntity);

		logger.debug(Blocks.MOB_SPAWNER.getLocalizedName());
		logger.debug(lState.getBlock().getLocalizedName());

		if (Objects.equals(Blocks.MOB_SPAWNER.getRegistryName(), lState.getBlock().getRegistryName()))
		{
			if (lTileEntity instanceof TileEntityMobSpawner)
			{
				TileEntityMobSpawner lTileEntityMobSpawner = (TileEntityMobSpawner) lTileEntity;
				logger.debug("Tile Entity Mob Spawner: " + lTileEntityMobSpawner);

				NBTTagCompound nbtBlock = item.getSubCompound("BlockEntityTag");
				logger.debug("NBTItem: " + nbtBlock);

				if (nbtBlock != null)
				{
					NBTTagCompound nbtTileEntityNew = new NBTTagCompound();
					logger.debug("NBTNew (1): " + nbtTileEntityNew);

					nbtTileEntityNew = lTileEntityMobSpawner.writeToNBT(nbtTileEntityNew);
					logger.debug("NBTNew (2): " + nbtTileEntityNew);

					NBTTagCompound nbtTileEntityOld = nbtTileEntityNew.copy();
					logger.debug("NBTOld: " + nbtTileEntityNew);

					nbtTileEntityNew.merge(nbtBlock);
					logger.debug("NBTNew (3): " + nbtTileEntityNew);

					nbtTileEntityNew.setInteger("x", lBlockPos.getX());
					nbtTileEntityNew.setInteger("y", lBlockPos.getY());
					nbtTileEntityNew.setInteger("z", lBlockPos.getZ());

					logger.debug("NBTNew (4): " + nbtTileEntityNew);

					if (!nbtTileEntityNew.equals(nbtTileEntityOld))
					{
						logger.debug("NBTNew (5): " + nbtTileEntityNew);
						lTileEntity.readFromNBT(nbtTileEntityNew);
						lTileEntity.markDirty();
					}
				}
			}
		}
	}

	private boolean checkValid(Block block, EntityPlayer player, World world, BlockPos pos, TileEntity tile, EnumHand hand, ItemStack item)
	{
		boolean result = true;
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
			logger.debug("Tile entity is null");
			result = false;
		}


		return result;
	}

	private void breakSpawner(Block block, World world, BlockPos pos, TileEntity tile, BlockEvent.BreakEvent e)
	{
		e.setCanceled(true);
		logger.debug("Monster Spawner!");
		TileEntityMobSpawner lEntityMobSpawner = (TileEntityMobSpawner) tile;
		NBTTagCompound tileData = new NBTTagCompound();
		tileData = lEntityMobSpawner.getSpawnerBaseLogic().writeToNBT(tileData);
		logger.debug("tile data: " + tileData);
		int meta = block.getMetaFromState(e.getState());
		logger.debug("meta: " + meta);

		if(meta < 0)
		{
			logger.debug("meta reduced to 0");
			meta = 0;
		}

		ItemStack stack = new ItemStack(block,1, meta);
		world.setBlockToAir(pos);
		stack.setTagInfo("BlockEntityTag", tileData);
		BlockUtil.blockDrop(world, pos, stack);
	}
}
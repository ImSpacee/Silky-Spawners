package com.eviqnn.silkyspawners;

import com.eviqnn.silkyspawners.proxy.CommonProxy;
import com.eviqnn.silkyspawners.util.BlockUtil;
import com.eviqnn.silkyspawners.util.References;

import net.minecraft.block.Block;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;


@Mod(modid = References.MOD_ID, name = References.NAME, version = References.VERSION)
public class Main {
	
	@Instance
	public static Main instance;

	public static Logger logger;
	
	@SidedProxy(clientSide = References.CLIENT_PROXY_CLASS, serverSide = References.COMMON_PROXY_CLASS)
	public static CommonProxy proxy;
	
	@EventHandler
	public static void PreInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
	}
	
	@EventHandler
	public static void init(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new Main());
		System.out.println("SilkySpawners has loaded with no errors.");
	}
	
	@EventHandler
	public static void Postinit(FMLPostInitializationEvent event)
	{
		
	}
	
	@SubscribeEvent
    public void BlockDrop(BlockEvent.BreakEvent e)
    {
		Block block = e.getState().getBlock();
		EntityPlayer player = e.getPlayer();
		World world = e.getWorld();
		BlockPos pos = e.getPos();
		TileEntity tile = world.getTileEntity(pos);
		if (block == null)
		{
			logger.error("Block is null");
			return;
		}
		if (world == null)
		{
			logger.error("World is null");
			return;
		}
		if (pos == null)
		{
			logger.error("Position is null");
			return;
		}
		if (player == null)
		{
			logger.error("Player is null");
			return;
		}
		if (world.isRemote)
		{
			logger.debug("World is remote");
			return;
		}
    	if(!world.getGameRules().getBoolean("doTileDrops"))
	    {
		    logger.debug("DoTileDrops is off");
	    	return;
	    }
	    EnumHand hand = player.getActiveHand();
    	if(hand == null)
	    {
		    logger.error("Active hand is null");
		    return;
	    }
    	ItemStack item = player.getHeldItem(hand);
    	if(item == null)
	    {
		    logger.error("Held item is null");
		    return;
	    }
    	if(tile == null)
	    {
		    logger.debug("Tile entity is null");
		    return;
	    }
    	int toollevel = item.getItem().getHarvestLevel(item, "pickaxe", player, e.getState());
    	Enchantment SILKTOUCH = Enchantment.getEnchantmentByLocation("minecraft:silk_touch");
    	if(EnchantmentHelper.getEnchantmentLevel(SILKTOUCH, player.getHeldItem(hand)) <= 0
			    ||  toollevel < BlockUtil.PickaxeLevel(e.getState()) )
	    {
	    	logger.debug("Not valid tool");
	    	return;
	    }

    	if(block instanceof BlockMobSpawner || tile instanceof TileEntityMobSpawner)
    	{
		    logger.debug("Monster Spawner!");
		    TileEntityMobSpawner lEntityMobSpawner = (TileEntityMobSpawner) tile;
		    NBTTagCompound tileData = new NBTTagCompound();
		    tileData = lEntityMobSpawner.getSpawnerBaseLogic().writeToNBT(tileData);
		    logger.debug(tileData);
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
		    BlockUtil.BlockDrop(world,pos,stack);
    		e.setCanceled(true);
    	}
    }
}

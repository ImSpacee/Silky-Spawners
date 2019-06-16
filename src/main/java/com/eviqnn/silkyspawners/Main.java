package com.eviqnn.silkyspawners;

import com.eviqnn.silkyspawners.proxy.CommonProxy;
import com.eviqnn.silkyspawners.util.BlockUtil;
import com.eviqnn.silkyspawners.util.References;

import net.minecraft.block.Block;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
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

@Mod(modid = References.MOD_ID, name = References.NAME, version = References.VERSION)
public class Main {
	
	@Instance
	public static Main instance;
	
	@SidedProxy(clientSide = References.CLIENT_PROXY_CLASS, serverSide = References.COMMON_PROXY_CLASS)
	public static CommonProxy proxy;
	
	@EventHandler
	public static void PreInit(FMLPreInitializationEvent event)
	{
		
	}
	
	@EventHandler
	public static void init(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new Main());
	}
	
	@EventHandler
	public static void Postinit(FMLPostInitializationEvent event)
	{
		
	}
	
	@SubscribeEvent
    public void BlockDrop(BlockEvent.BreakEvent e)
    {
    	if(e.getState().getBlock() == null || e.getWorld() == null || e.getPos() == null || e.getWorld().isRemote || e.getPlayer() == null || !e.getWorld().getGameRules().getBoolean("doTileDrops"))
    		return;
    	if(e.getPlayer().getActiveHand() == null)
    		return;
    	if(e.getPlayer().getHeldItem(e.getPlayer().getActiveHand()) == null)
    		return;
    	if(e.getWorld().getTileEntity(e.getPos()) == null)
			return;
    	int toollevel = e.getPlayer().getHeldItem(e.getPlayer().getActiveHand()).getItem().getHarvestLevel(e.getPlayer().getHeldItem(e.getPlayer().getActiveHand()), "pickaxe",e.getPlayer(),e.getState());
    	if(e.getPlayer().getHeldItem(e.getPlayer().getActiveHand()) == null || EnchantmentHelper.getEnchantmentLevel(Enchantment.getEnchantmentByLocation("minecraft:silk_touch"), e.getPlayer().getHeldItem(e.getPlayer().getActiveHand())) <= 0 ||  toollevel < BlockUtil.PickaxeLevel(e.getState()) )
    		return;

    	if(e.getState().getBlock() instanceof BlockMobSpawner || e.getWorld().getTileEntity(e.getPos()) instanceof TileEntityMobSpawner)
    	{
    		int meta = e.getState().getBlock().getMetaFromState(e.getState());
    		if(meta < 0)
    			meta = 0;
    		ItemStack stack = new ItemStack(e.getState().getBlock(),1,meta);
    		BlockUtil.BlockDrop(e.getWorld(),e.getPos(),stack);
    		e.getWorld().setBlockToAir(e.getPos());
    		e.setCanceled(true);
    	}
    }
}

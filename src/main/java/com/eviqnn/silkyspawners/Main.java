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
    	if(block == null || world == null || pos == null || world.isRemote || player == null || !world.getGameRules().getBoolean("doTileDrops"))
    		return;
    	if(player.getActiveHand() == null)
    		return;
    	if(player.getHeldItem(player.getActiveHand()) == null)
    		return;
    	if(tile == null)
			return;
    	int toollevel = player.getHeldItem(player.getActiveHand()).getItem().getHarvestLevel(player.getHeldItem(player.getActiveHand()), "pickaxe",player,e.getState());
    	if(player.getHeldItem(player.getActiveHand()) == null || EnchantmentHelper.getEnchantmentLevel(Enchantment.getEnchantmentByLocation("minecraft:silk_touch"), player.getHeldItem(player.getActiveHand())) <= 0 ||  toollevel < BlockUtil.PickaxeLevel(e.getState()) )
    		return;

    	if(block instanceof BlockMobSpawner || tile instanceof TileEntityMobSpawner)
    	{
    		int meta = block.getMetaFromState(e.getState());
    		if(meta < 0)
    			meta = 0;
    		ItemStack stack = new ItemStack(block,1,meta);
    		BlockUtil.BlockDrop(world,pos,stack);
    		world.setBlockToAir(pos);
    		NBTTagCompound nbt = new NBTTagCompound();
    		setSpawnID(stack,world,tile,nbt);
    		e.setCanceled(true);
    	}
    }
	
	public void setSpawnID(ItemStack stack, World world, TileEntity tile, NBTTagCompound nbt)
	{
		NBTTagCompound data = nbt.getCompoundTag("SpawnData");
		String name = data.getString("id");
		nbt.setString("SpawnData", name);
	}
}

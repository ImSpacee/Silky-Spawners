package com.eviqnn.silkyspawners.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockUtil {
	
	public static void BlockDrop(World world, BlockPos pos, ItemStack stack)
	{
		 if (!world.isRemote && world.getGameRules().getBoolean("doTileDrops") && !world.restoringBlockSnapshots)
	     {
	        float f = 0.7F;
	        double d = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
	        int x = pos.getX();
	        int y = pos.getY();
	        int z = pos.getZ();
	        new EntityItem(world, (double)x + d, (double)y + d, (double)z + d, stack).setPickupDelay(10);
	        world.spawnEntity(new EntityItem(world, (double)x + d, (double)y + d, (double)z + d, stack));
	    }
	}
	
    public static int PickaxeLevel(IBlockState state) 
    {
    	return state.getBlock().getHarvestLevel(state);
	}

}

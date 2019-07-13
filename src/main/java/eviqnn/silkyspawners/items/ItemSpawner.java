package eviqnn.silkyspawners.items;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;

public class ItemSpawner extends ItemBlock
{
	public ItemSpawner()
	{
		super(Blocks.MOB_SPAWNER);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		NBTTagCompound nbtBlock = stack.getSubCompound("BlockEntityTag");
		String EntityName = "Monster";
		if (nbtBlock != null) {
			NBTTagCompound spawnData = nbtBlock.getCompoundTag("SpawnData");
			if (spawnData != null) {
				String id = spawnData.getString("id");
				EntityName = ResourceLocation.splitObjectName(id)[1];
				EntityName = EntityName.replace("_", " ");
				EntityName = WordUtils.capitalizeFully(EntityName);
			}
		}
		return EntityName + " Spawner";
	}
}

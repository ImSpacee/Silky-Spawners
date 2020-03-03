package eviqnn.silkyspawners.registry;

import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import org.apache.commons.lang3.text.WordUtils;

public class ItemSpawner extends ItemBlock
{
    ItemSpawner()
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
            String id = spawnData.getString("id");
            ResourceLocation resourceLocation = new ResourceLocation(id);
            String translationName = EntityList.getTranslationName(resourceLocation);
            if (translationName != null)
            {
               EntityName = I18n.translateToLocal("entity." + translationName + ".name");
            }
        }
        return EntityName + " Spawner";
    }
}

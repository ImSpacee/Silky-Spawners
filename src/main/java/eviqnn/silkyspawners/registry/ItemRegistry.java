package eviqnn.silkyspawners.registry;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ItemRegistry
{
    public static Item itemSpawner;

    public static void init()
    {
        registerItems();
    }

    private static void registerItems()
    {
        itemSpawner = registerItem(new ItemSpawner(), new ResourceLocation("minecraft", "mob_spawner"));
    }

    private static Item registerItem(Item item, ResourceLocation name)
    {
        item.setRegistryName(name);
        ForgeRegistries.ITEMS.register(item);
        return item;
    }
}

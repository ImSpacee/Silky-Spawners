package eviqnn.silkyspawners.items;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class Items
{
	public static Item itemSpawner;

	public static void init()
	{
		registerItems();
	}

	public static void registerItems()
	{
		itemSpawner = registerItem(new ItemSpawner(), new ResourceLocation("minecraft", "mob_spawner"));
	}

	public static Item registerItem(Item item, ResourceLocation name)
	{
		item.setRegistryName(name);
		ForgeRegistries.ITEMS.register(item);
		return item;
	}

}

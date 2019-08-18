package eviqnn.silkyspawners;

import eviqnn.silkyspawners.proxy.CommonProxy;
import eviqnn.silkyspawners.registry.ItemRegistry;
import eviqnn.silkyspawners.util.BlockUtil;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

@Mod(
        modid = SilkySpawners.ID,
        name = SilkySpawners.NAME,
        version = SilkySpawners.VERSION,
        acceptedMinecraftVersions = "[1.12.2]"
)

public class SilkySpawners {

    static final String ID = "silkyspawners";
    static final String NAME = "silkyspawners";
    static final String VERSION = "1.3";

    @SidedProxy(clientSide = "eviqnn.silkyspawners.proxy.ClientProxy", serverSide = "eviqnn.silkyspawners.proxy.ServerProxy")
    private static CommonProxy proxy;

    @Instance
    private static SilkySpawners instance;

    static Logger logger;

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        proxy.preInit(event);
        SilkyConfig.readConfig();
        ItemRegistry.init();
    }

    @EventHandler
    public static void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(instance);
    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent e)
    {
        SilkyConfig.debug("Block Break!");
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
            SilkyConfig.debug("Invalid parameters!");
            return;
        }

        if (world.isRemote)
        {
            // Debug purposes only
            // Spams debug console on client
            SilkyConfig.debug("World is remote");
            return;
        }

        if(!world.getGameRules().getBoolean("doTileDrops"))
        {
            // Debug purposes only
            // Spams debug console
            SilkyConfig.debug("DoTileDrops is off");
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
        SilkyConfig.debug("Block Place!");
        IBlockState state = e.getPlacedBlock();
        Entity entity = e.getEntity();
        BlockPos blockPos = e.getPos();
        assert entity != null;
        World world = entity.getEntityWorld();
        TileEntity tile = world.getTileEntity(blockPos);

        EnumHand hand;
        ItemStack item;

        // Debug purposes only
        SilkyConfig.debug("Blockstate: " + state + "\nEntity: " + entity + "\nBlockPos: " + blockPos + "\nWorld: " + world + "\nTileEntity: " + tile);

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
        SilkyConfig.debug("hand: " + hand + "\nItemStack: " + item);
        SilkyConfig.debug(Blocks.MOB_SPAWNER.getLocalizedName() + state.getBlock().getLocalizedName());

        if (Objects.equals(Blocks.MOB_SPAWNER.getRegistryName(), state.getBlock().getRegistryName()))
        {
            if (tile instanceof TileEntityMobSpawner)
            {
                TileEntityMobSpawner TileEntityMobSpawner = (TileEntityMobSpawner) tile;
                NBTTagCompound nbtBlock = item.getSubCompound("BlockEntityTag");

                // Debug purposes only
                SilkyConfig.debug("NBTItem: " + nbtBlock + "\nTile Entity Mob Spawner: " + TileEntityMobSpawner);

                if (nbtBlock != null)
                {
                    NBTTagCompound nbtTileEntityNew = new NBTTagCompound();
                    // Debug purposes only
                    SilkyConfig.debug("NBTNew (1): " + nbtTileEntityNew);

                    nbtTileEntityNew = TileEntityMobSpawner.writeToNBT(nbtTileEntityNew);
                    // Debug purposes only
                    SilkyConfig.debug("NBTNew (2): " + nbtTileEntityNew);

                    NBTTagCompound nbtTileEntityOld = nbtTileEntityNew.copy();
                    // Debug purposes only
                    SilkyConfig.debug("NBTOld: " + nbtTileEntityNew);

                    nbtTileEntityNew.merge(nbtBlock);
                    // Debug purposes only
                    SilkyConfig.debug("NBTNew (3): " + nbtTileEntityNew);

                    nbtTileEntityNew.setInteger("x", blockPos.getX());
                    nbtTileEntityNew.setInteger("y", blockPos.getY());
                    nbtTileEntityNew.setInteger("z", blockPos.getZ());
                    // Debug purposes only
                    SilkyConfig.debug("NBTNew (4): " + nbtTileEntityNew);

                    if (!nbtTileEntityNew.equals(nbtTileEntityOld))
                    {
                        // Debug purposes only
                        SilkyConfig.debug("NBTNew (5): " + nbtTileEntityNew);
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
            SilkyConfig.debug("Tile entity is null");
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
        SilkyConfig.debug("Monster Spawner!");
        TileEntityMobSpawner lEntityMobSpawner = (TileEntityMobSpawner) tile;
        NBTTagCompound tileData = new NBTTagCompound();
        tileData = lEntityMobSpawner.getSpawnerBaseLogic().writeToNBT(tileData);
        SilkyConfig.debug("tile data: " + tileData);
        int meta = block.getMetaFromState(e.getState());
        SilkyConfig.debug("meta: " + meta);

        if(meta < 0)
        {
            SilkyConfig.debug("meta reduced to 0");
            meta = 0;
        }
        NBTTagCompound itemNBT = new NBTTagCompound();
        itemNBT.setTag("BlockEntityTag", tileData);
        ItemStack stack = new ItemStack(ItemRegistry.itemSpawner, 1, meta, itemNBT);
        world.setBlockToAir(pos);
        stack.setTagInfo("BlockEntityTag", tileData);
        NBTTagCompound display = new NBTTagCompound();
        MobSpawnerBaseLogic tileMobSpawn = ((TileEntityMobSpawner) tile).getSpawnerBaseLogic();
        Object spawnDataObj = ObfuscationReflectionHelper.getPrivateValue(MobSpawnerBaseLogic.class, tileMobSpawn, "field_98282_f"); // what entity the spawner will have
        SilkyConfig.debug("Spawn Data: " + spawnDataObj);
        SilkyConfig.debug("Spawn Data Class: " + spawnDataObj.getClass());
        //if (spawnDataObj instanceof WeightedSpawnerEntity)
        //{
        //WeightedSpawnerEntity spawnData = (WeightedSpawnerEntity) spawnDataObj;
        //String entityIDstr = spawnData.getNbt().getString("id");
        //String entityIDEntity = ResourceLocation.splitObjectName(entityIDstr)[1];
        //entityIDEntity = entityIDEntity.replace("_", " ");
        //entityIDEntity = WordUtils.capitalizeFully(entityIDEntity);
        //display.setString("Name", "§r" + entityIDEntity + " Spawner");
        //}
        //else
        //{
        //display.setString("Name", "§rERROR Spawner");
        //}
        stack.setTagInfo("display", display);
        BlockUtil.blockDrop(world, pos, stack);
    }
}

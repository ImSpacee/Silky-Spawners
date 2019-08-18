package eviqnn.silkyspawners.proxy;

import eviqnn.silkyspawners.SilkyConfig;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

@Mod.EventBusSubscriber
public class CommonProxy
{

    public static Configuration config;

    public void preInit(FMLPreInitializationEvent e)
    {
        File directory = e.getModConfigurationDirectory();
        config = new Configuration(new File(directory.getPath(), "silkyspawners.cfg"));
        SilkyConfig.readConfig();
    }

    public void postInit(FMLPostInitializationEvent e)
    {
        if (config.hasChanged())
        {
            config.save();
        }
    }
}


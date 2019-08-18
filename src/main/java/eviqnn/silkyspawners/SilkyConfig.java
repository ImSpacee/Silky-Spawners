package eviqnn.silkyspawners;

import eviqnn.silkyspawners.proxy.CommonProxy;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

public class SilkyConfig
{
    public static final String CATEGORY_GENERAL = "General";

    public static boolean canDebug = true;

    public static void readConfig()
    {
        Configuration cfg = CommonProxy.config;
        try
        {
            initGenConfig(cfg);
        } catch (Exception e1) {
            SilkySpawners.logger.log(Level.ERROR, "An issue occured loading SilkySpawners Configuration File!", e1);
        } finally {
            if (cfg.hasChanged())
            {
                cfg.save();
            }
        }
    }

    private static void initGenConfig(Configuration cfg)
    {
        cfg.addCustomCategoryComment(CATEGORY_GENERAL, "general config");
        // cfg.getBoolean() will get the value in the config if it is already specified there. If not it will create the value.
        canDebug = cfg.getBoolean("canDebug", CATEGORY_GENERAL, canDebug, "Disable/Enable for Debug Logging");
    }

    public static void debug(Object object)
    {
        if (canDebug == true)
        {
            SilkySpawners.logger.debug(object);
        }
    }
}

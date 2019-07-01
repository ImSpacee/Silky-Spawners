package eviqnn.silkyspawners.util;

import eviqnn.silkyspawners.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Debug
{
	private static boolean canDebug = true;

	public static File config;

	public static void readProperties()
	{
		debug("Reading properties");
		try {
			Properties lProperties = new Properties();
			if (config.exists())
			{
				debug("config exists");
				lProperties.load(new FileInputStream(config));
				debug("loaded properties");
				lProperties.list(System.out);
				String debug = lProperties.getProperty("debug", "false");
				lProperties.setProperty("debug", debug);
				FileOutputStream out = new FileOutputStream(config);
				lProperties.store(out, "Set \"debug\" to true for debug info");
				out.close();
				canDebug = Boolean.parseBoolean(debug);
			}
			else
			{
				lProperties.setProperty("debug", "false");
				FileOutputStream out = new FileOutputStream(config);
				lProperties.store(out, "Set \"debug\" to true for debug info");
				canDebug = false;
			}
		} catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	public static void debug(Object object)
	{
		if (canDebug)
		{
			Main.logger.debug(object);
		}
	}
}

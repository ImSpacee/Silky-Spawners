package eviqnn.silkyspawners.util;

import eviqnn.silkyspawners.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Debug
{
	public static boolean canDebug = false;

	public static File config;

	public static void readProperties()
	{
		try {
			Properties lProperties = new Properties();
			if (config.exists())
			{
				lProperties.load(new FileInputStream(config));
				String debug = lProperties.getProperty("debug", "false");
				lProperties.setProperty("debug", debug);
				FileOutputStream out = new FileOutputStream(config);
				lProperties.store(out, "Set \"debug\" to true for debug info");
				out.close();
				canDebug = Boolean.getBoolean(debug);
			}
			else
			{
				lProperties.setProperty("debug", "false");
				FileOutputStream out = new FileOutputStream(config);
				lProperties.store(out, "Set \"debug\" to true for debug info");
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

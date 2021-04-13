package net.wurstclient.altmanager;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import net.wurstclient.altmanager.libs.AltManager;
import net.wurstclient.altmanager.mixinterface.IMinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public enum WiAltManager {
	INSTANCE;

	public static final MinecraftClient MC = MinecraftClient.getInstance();
	public static final IMinecraftClient IMC = (IMinecraftClient)MC;

	private AltManager altManager;
	private Path altManagerFolder;

	public void initialize() {
		FabricLoader fabricLoader = FabricLoader.getInstance();
		ModContainer modContainter = fabricLoader.getModContainer("wi_altmanager").get();
		Version version = modContainter.getMetadata().getVersion();

		altManagerFolder = createAltManagerFolder();

		Path altsFile = altManagerFolder.resolve("alts.encrypted_json");
		Path encFolder = createEncryptionFolder();
		altManager = new AltManager(altsFile, encFolder);

		System.out.println("Starting WI AltManager v" + version.getFriendlyString());
	}

	private Path createEncryptionFolder()
	{
		Path encFolder =
				Paths.get(System.getProperty("user.home"), ".WI encryption")
						.normalize();

		try
		{
			Files.createDirectories(encFolder);
			if(Util.getOperatingSystem() == Util.OperatingSystem.WINDOWS)
				Files.setAttribute(encFolder, "dos:hidden", true);

			Path readme = encFolder.resolve("READ ME I AM VERY IMPORTANT.txt");
			String readmeText = "DO NOT SHARE THESE FILES WITH ANYONE!\r\n"
					+ "They are encryption keys that protect your alt list file from being read by someone else.\r\n"
					+ "If someone is asking you to send these files, they are 100% trying to scam you.\r\n"
					+ "\r\n"
					+ "DO NOT EDIT, RENAME OR DELETE THESE FILES! (unless you know what you're doing)\r\n"
					+ "If you do, Wurst's Alt Manager can no longer read your alt list and will replace it with a blank one.\r\n"
					+ "In other words, YOUR ALT LIST WILL BE DELETED.";
			Files.write(readme, readmeText.getBytes("UTF-8"),
					StandardOpenOption.CREATE);

		}catch(IOException e)
		{
			throw new RuntimeException(
					"Couldn't create '.Wurst encryption' folder.", e);
		}

		return encFolder;
	}

	private Path createAltManagerFolder()
	{
		Path dotMinecraftFolder = MC.runDirectory.toPath().normalize();
		Path altManagerFolder = dotMinecraftFolder.resolve("altmanager");

		try
		{
			Files.createDirectories(altManagerFolder);

		}catch(IOException e)
		{
			throw new RuntimeException(
					"Couldn't create .minecraft/altManager folder.", e);
		}

		return altManagerFolder;
	}

	public Path getAltManagerFolder()
	{
		return altManagerFolder;
	}

	public AltManager getAltManager()
	{
		return altManager;
	}
}

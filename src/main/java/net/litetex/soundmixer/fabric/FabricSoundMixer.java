package net.litetex.soundmixer.fabric;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.litetex.soundmixer.SoundMixer;
import net.litetex.soundmixer.config.Config;
import net.litetex.soundmixer.fabric.compat.SoundControllerConfigImport;


public class FabricSoundMixer implements ClientModInitializer
{
	private static final Logger LOG = LoggerFactory.getLogger(FabricSoundMixer.class);
	
	private final Gson gson = new GsonBuilder()
		.setPrettyPrinting()
		.create();
	
	@Override
	public void onInitializeClient()
	{
		final Path configDir = FabricLoader.getInstance().getConfigDir().resolve("sound-mixer");
		final Path configFile = configDir.resolve("config.json");
		
		SoundMixer.setInstance(new SoundMixer(
			this.loadConfig(configFile),
			config -> this.saveConfig(configFile, config)
		));
	}
	
	private Config loadConfig(final Path configFile)
	{
		if(Files.exists(configFile))
		{
			try
			{
				return this.gson.fromJson(Files.readString(configFile), Config.class);
			}
			catch(final Exception ex)
			{
				LOG.warn("Failed to read config file", ex);
			}
		}
		
		final Config defaultConfig = Config.createDefault();
		SoundControllerConfigImport.tryFill(defaultConfig);
		this.saveConfig(configFile, defaultConfig);
		return defaultConfig;
	}
	
	private void saveConfig(final Path configFile, final Config config)
	{
		try
		{
			Files.createDirectories(configFile.getParent());
			Files.writeString(
				configFile,
				this.gson.toJson(config));
		}
		catch(final IOException ioe)
		{
			throw new UncheckedIOException("Failed to save config", ioe);
		}
	}
}

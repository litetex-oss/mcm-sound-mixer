package net.litetex.soundmixer.fabric.compat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.litetex.soundmixer.SoundMixer;
import net.litetex.soundmixer.config.Config;


public final class SoundControllerConfigImport
{
	private static final Logger LOG = LoggerFactory.getLogger(SoundControllerConfigImport.class);
	
	public static void tryFill(final Config config)
	{
		final Path path = FabricLoader.getInstance().getConfigDir().resolve("soundcontroller.json");
		if(!Files.exists(path))
		{
			return;
		}
		
		try
		{
			// As of v4
			
			final JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
			Optional.ofNullable(root.get("subtitlesEnabled"))
				.map(JsonElement::getAsBoolean)
				.ifPresent(config::setShowSubtitleIds);
			
			Optional.ofNullable(root.get("sounds"))
				.map(JsonElement::getAsJsonArray)
				.map(JsonArray::asList)
				.stream()
				.flatMap(List::stream)
				.map(JsonElement::getAsJsonObject)
				.forEach(jsonObject -> {
					Optional.ofNullable(jsonObject.get("soundId"))
						.map(JsonElement::getAsString)
						.ifPresent(soundId -> {
							Optional.ofNullable(jsonObject.get("volume"))
								.map(JsonElement::getAsFloat)
								.filter(volume -> volume != SoundMixer.DEFAULT_VOLUME)
								.ifPresent(volume -> config.getSoundIdVolumes().put(
									soundId,
									Math.clamp(
										volume,
										SoundMixer.MIN_VOLUME, SoundMixer.MAX_VOLUME)));
						});
				});
		}
		catch(final Exception ex)
		{
			LOG.warn("Failed to import from {}", path, ex);
		}
	}
	
	private SoundControllerConfigImport()
	{
	}
}

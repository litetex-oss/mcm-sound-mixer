package net.litetex.soundmixer;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.litetex.soundmixer.config.Config;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;


public class SoundMixer
{
	public static final float MIN_VOLUME = 0.0f;
	public static final float DEFAULT_VOLUME = 1.0f;
	public static final float MAX_VOLUME = 2.0f;
	
	static final String MOD_ID = "sound-mixer";
	
	private static SoundMixer instance;
	
	public static SoundMixer instance()
	{
		return instance;
	}
	
	public static void setInstance(final SoundMixer instance)
	{
		SoundMixer.instance = instance;
	}
	
	private boolean showIds;
	
	private final SortedMap<Identifier, Float> soundIdVolumes;
	private final Set<Identifier> mutedSounds;
	
	private boolean requireConfigSave;
	private final Config config;
	private final Consumer<Config> saveConfigFunc;
	
	public SoundMixer(
		final Config config,
		final Consumer<Config> saveConfigFunc
	)
	{
		this.config = config;
		this.saveConfigFunc = saveConfigFunc;
		
		this.showIds = config.isShowIds();
		
		this.soundIdVolumes = config.getSoundIdVolumes().entrySet()
			.stream()
			.filter(e -> e.getValue() != DEFAULT_VOLUME
				&& e.getValue() >= MIN_VOLUME
				&& e.getValue() <= MAX_VOLUME)
			.map(e -> {
				try
				{
					return Map.entry(Identifier.parse(e.getKey()), e.getValue());
				}
				catch(final Exception ex)
				{
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (_, r) -> r, TreeMap::new));
		
		this.mutedSounds = this.soundIdVolumes.entrySet()
			.stream()
			.filter(e -> e.getValue() == 0f)
			.map(Map.Entry::getKey)
			.collect(Collectors.toSet());
	}
	
	public void upsertSound(final Identifier id, final float value)
	{
		this.requireConfigSave = true;
		
		if(value == DEFAULT_VOLUME)
		{
			if(this.soundIdVolumes.remove(id) != null)
			{
				this.mutedSounds.remove(id);
			}
			return;
		}
		
		final Float previousValue = this.soundIdVolumes.put(id, value);
		if(value == 0.0f)
		{
			this.mutedSounds.add(id);
		}
		else if(previousValue != null)
		{
			this.mutedSounds.remove(id);
		}
	}
	
	public boolean isMuted(final SoundInstance sound)
	{
		return this.isMuted(sound.getIdentifier());
	}
	
	public boolean isMuted(final Identifier id)
	{
		return this.mutedSounds.contains(id);
	}
	
	public float getAdjustedVolume(final SoundInstance sound, final float baseVolume)
	{
		return this.getAdjustedVolume(sound.getIdentifier(), baseVolume);
	}
	
	public float getAdjustedVolume(final Identifier id, final float baseVolume)
	{
		final Float adjusted = this.soundIdVolumes.get(id);
		return adjusted == null
			? baseVolume
			: baseVolume * adjusted;
	}
	
	public Map<Identifier, Float> allSoundIdVolumes()
	{
		return this.soundIdVolumes;
	}
	
	public boolean isShowIds()
	{
		return this.showIds;
	}
	
	public void setShowIds(final boolean showIds)
	{
		this.showIds = showIds;
		this.requireConfigSave = true;
	}
	
	public void saveConfigIfRequired()
	{
		if(!this.requireConfigSave)
		{
			return;
		}
		
		this.config.setShowIds(this.showIds);
		this.config.getSoundIdVolumes().clear();
		this.config.getSoundIdVolumes().putAll(this.soundIdVolumes.entrySet()
			.stream()
			.collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue)));
		
		this.saveConfigFunc.accept(this.config);
	}
	
	public static Identifier identifier(final String path)
	{
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}

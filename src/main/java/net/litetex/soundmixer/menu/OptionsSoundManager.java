package net.litetex.soundmixer.menu;

import java.util.Map;

import com.google.common.cache.CacheBuilder;

import net.litetex.soundmixer.SoundMixer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;


public class OptionsSoundManager implements AutoCloseable
{
	private final Map<Identifier, SimpleSoundInstance> idSoundInstances = CacheBuilder.newBuilder()
		.weakKeys()
		.weakValues()
		.<Identifier, SimpleSoundInstance>build()
		.asMap();
	
	private final SoundManager soundManager;
	
	public OptionsSoundManager(final SoundManager soundManager)
	{
		this.soundManager = soundManager;
	}
	
	public void toggleSound(final Identifier soundId)
	{
		final SimpleSoundInstance existingSound = this.idSoundInstances.remove(soundId);
		if(existingSound != null && this.tryStopSound(existingSound))
		{
			this.idSoundInstances.remove(soundId);
			return;
		}
		
		final SoundMixer soundMixer = SoundMixer.instance();
		if(soundMixer.isMuted(soundId))
		{
			return;
		}
		
		this.idSoundInstances.put(
			soundId,
			this.createAndPlaySound(
				soundId,
				soundMixer.getAdjustedVolume(soundId, SoundMixer.DEFAULT_VOLUME)));
	}
	
	private boolean tryStopSound(final SimpleSoundInstance soundInstance)
	{
		if(this.soundManager.isActive(soundInstance))
		{
			this.soundManager.stop(soundInstance);
			return true;
		}
		return false;
	}
	
	private SimpleSoundInstance createAndPlaySound(final Identifier soundId, final float volume)
	{
		final LocalPlayer player = Minecraft.getInstance().player;
		final SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(soundId);
		
		final SimpleSoundInstance soundInstance = player != null
			? new SimpleSoundInstance(
			soundEvent, SoundSource.MASTER,
			volume,
			1.0f, // Pitch
			RandomSource.create(),
			player.getX(), player.getY(), player.getZ())
			: SimpleSoundInstance.forUI(soundEvent, 1.0f, volume);
		
		this.soundManager.play(soundInstance);
		return soundInstance;
	}
	
	@Override
	public void close()
	{
		this.idSoundInstances.values().forEach(this::tryStopSound);
		this.idSoundInstances.clear();
	}
}

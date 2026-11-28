package net.litetex.soundmixer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.litetex.soundmixer.SoundMixer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;


@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin
{
	@WrapMethod(
		method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)"
			+ "Lnet/minecraft/client/sounds/SoundEngine$PlayResult;")
	SoundEngine.PlayResult play(
		final SoundInstance instance,
		final Operation<SoundEngine.PlayResult> original)
	{
		return SoundMixer.instance().isMuted(instance)
			? SoundEngine.PlayResult.NOT_STARTED
			: original.call(instance);
	}
	
	@WrapOperation(
		method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)"
			+ "Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(FLnet/minecraft/sounds/SoundSource;)F"))
	float modifyH(
		final SoundEngine instance,
		final float volume,
		final SoundSource source,
		final Operation<Float> original,
		final SoundInstance sound)
	{
		return SoundMixer.instance().getAdjustedVolume(sound, original.call(instance, volume, source));
	}
	
	@WrapMethod(method = "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F")
	float modifyGetAdjustedVolume(final SoundInstance sound, final Operation<Float> original)
	{
		return SoundMixer.instance().getAdjustedVolume(sound, original.call(sound));
	}
}

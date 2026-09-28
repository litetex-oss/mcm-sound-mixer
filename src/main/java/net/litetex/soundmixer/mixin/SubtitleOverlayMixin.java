package net.litetex.soundmixer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.litetex.soundmixer.SoundMixer;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;


@Mixin(SubtitleOverlay.class)
public abstract class SubtitleOverlayMixin
{
	@WrapOperation(
		method = "onPlaySound",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/sounds/WeighedSoundEvents;getSubtitle()"
				+ "Lnet/minecraft/network/chat/Component;"))
	Component replaceSubtitleText(
		final WeighedSoundEvents instance,
		final Operation<Component> original,
		final SoundInstance sound)
	{
		return SoundMixer.instance().isShowSubtitleIds()
			? Component.translationArg(sound.getIdentifier())
			: original.call(instance);
	}
}

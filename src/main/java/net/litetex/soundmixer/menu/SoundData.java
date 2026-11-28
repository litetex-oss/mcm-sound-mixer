package net.litetex.soundmixer.menu;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import net.litetex.soundmixer.shared.external.com.google.common.base.Suppliers;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;


public record SoundData(
	Identifier identifier,
	String idString,
	Supplier<String> idLowerCase,
	Supplier<String> translation,
	Supplier<String> translationLowerCase
)
{
	public static SoundData create(final Identifier id, final SoundManager soundManager)
	{
		final String idString = id.toString();
		
		final Supplier<String> translationSupplier = Suppliers.memoize(() ->
			Optional.ofNullable(soundManager.getSoundEvent(id))
				.map(WeighedSoundEvents::getSubtitle)
				.map(Component::getString)
				.orElse(idString));
		return new SoundData(
			id,
			idString,
			Suppliers.memoize(() -> idString.toLowerCase(Locale.ROOT)),
			translationSupplier,
			Suppliers.memoize(() -> translationSupplier.get().toLowerCase(Locale.ROOT))
		);
	}
}

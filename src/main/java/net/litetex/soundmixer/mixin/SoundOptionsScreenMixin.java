package net.litetex.soundmixer.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.litetex.soundmixer.menu.AllSoundOptionsScreen;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;


@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin extends OptionsSubScreen
{
	public SoundOptionsScreenMixin(final Screen screen, final Options options, final Component component)
	{
		super(screen, options, component);
	}
	
	@Override
	protected void addFooter()
	{
		final LinearLayout footerLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		
		footerLayout.addChild(Button.builder(
				AllSoundOptionsScreen.TITLE,
				_ -> this.minecraft.setScreenAndShow(new AllSoundOptionsScreen(this, this.options)))
			.build());
		footerLayout.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> this.onClose()).build());
	}
}

package net.litetex.soundmixer.menu;

import java.util.List;

import net.litetex.soundmixer.SoundMixer;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;


public class VolumeEntry extends Entry<VolumeEntry>
{
	private static final int BUTTON_SIZE = 20;
	private static final int PADDING_AFTER_SLIDER = 6;
	private static final int PADDING_BETWEEN_BUTTONS = 4;
	private static final int NON_VARIABLE_END_WIDTH =
		BUTTON_SIZE * 2 + PADDING_AFTER_SLIDER + PADDING_BETWEEN_BUTTONS;
	
	private static final int MIN_PERCENT = getPercentageValue(SoundMixer.MIN_VOLUME);
	private static final int MAX_PERCENT = getPercentageValue(SoundMixer.MAX_VOLUME);
	
	private static final Identifier RESET_ICON_TEXTURE = SoundMixer.identifier("icon/reset");
	private static final Identifier SPEAKER_ICON_TEXTURE = SoundMixer.identifier("icon/speaker");
	
	private boolean showIdentifier;
	
	private final SoundManager soundManager;
	
	private final OptionInstance<Integer> volumeOption;
	
	private final OptionInstance.OptionInstanceSliderButton<Integer> sliderVolume;
	private final TogglePlayButtonIcon btnTogglePlaySound;
	private final SpriteIconButton btnReset;
	
	@SuppressWarnings("unchecked")
	public VolumeEntry(
		final SoundData soundData,
		final boolean showIdentifier,
		final float initialVolume,
		final SoundManager soundManager,
		final Options options,
		final OptionsSoundManager optionsSoundManager)
	{
		this.showIdentifier = showIdentifier;
		
		this.soundManager = soundManager;
		
		this.volumeOption = new OptionInstance<>(
			soundData.idString(),
			_ -> this.showIdentifier || soundData.idString().equals(soundData.translation().get())
				? null
				: Tooltip.create(Component.literal(soundData.idString())),
			(initialPrefix, value) -> {
				final Component prefix = this.showIdentifier
					? initialPrefix
					: Component.literal(soundData.translation().get());
				return value == 0
					? Component.translatable("options.generic_value", prefix, CommonComponents.OPTION_OFF)
					: Component.translatable("options.percent_value", prefix, value);
			},
			new OptionInstance.IntRange(MIN_PERCENT, MAX_PERCENT),
			getPercentageValue(initialVolume),
			value -> {
				final float volume = value / 100f;
				SoundMixer.instance().upsertSound(soundData.identifier(), volume);
				this.updateBtnResetActive(volume);
				
				this.updateExistingSoundVolume(soundData.identifier());
			});
		
		// Volume slider (widget, created from options)
		this.sliderVolume = (OptionInstance.OptionInstanceSliderButton<Integer>)
			this.volumeOption.createButton(options, 0, 0, 1);
		
		this.btnTogglePlaySound = new TogglePlayButtonIcon(_ ->
			optionsSoundManager.toggleSound(soundData.identifier()));
		
		this.btnReset = SpriteIconButton.builder(
				Component.literal("Reset"),
				_ -> {
					this.volumeOption.set(getPercentageValue(SoundMixer.DEFAULT_VOLUME));
					this.sliderVolume.resetValue();
				},
				true)
			.withTootip()
			.size(BUTTON_SIZE, BUTTON_SIZE)
			.sprite(RESET_ICON_TEXTURE, 16, 16)
			.build();
		
		this.updateBtnResetActive(initialVolume);
	}
	
	private void updateBtnResetActive(final float volume)
	{
		this.btnReset.active = volume != SoundMixer.DEFAULT_VOLUME;
	}
	
	private static int getPercentageValue(final double value)
	{
		return (int)Math.round(value * 100);
	}
	
	public void updateShowIdentifier(final boolean showIdentifier)
	{
		this.showIdentifier = showIdentifier;
		this.sliderVolume.updateMessage();
	}
	
	@Override
	public void setX(final int i)
	{
		super.setX(i);
		this.updateElementPositions();
	}
	
	@Override
	public void setY(final int i)
	{
		super.setY(i);
		this.updateElementPositions();
	}
	
	private void updateElementPositions()
	{
		this.sliderVolume.setPosition(this.getContentX(), this.getY());
		this.btnTogglePlaySound.setPosition(this.sliderVolume.getRight() + PADDING_AFTER_SLIDER, this.getY());
		this.btnReset.setPosition(this.btnTogglePlaySound.getRight() + PADDING_BETWEEN_BUTTONS, this.getY());
	}
	
	@Override
	public void setWidth(final int i)
	{
		super.setWidth(i);
		this.updateElementWidths();
	}
	
	private void updateElementWidths()
	{
		this.sliderVolume.setWidth(this.getWidth() - NON_VARIABLE_END_WIDTH);
	}
	
	@Override
	public void extractContent(
		final GuiGraphicsExtractor context,
		final int mouseX,
		final int mouseY,
		final boolean hovered,
		final float tickDelta)
	{
		this.sliderVolume.extractRenderState(context, mouseX, mouseY, tickDelta);
		this.btnTogglePlaySound.extractRenderState(context, mouseX, mouseY, tickDelta);
		this.btnReset.extractRenderState(context, mouseX, mouseY, tickDelta);
	}
	
	@Override
	public List<? extends GuiEventListener> children()
	{
		return List.of(this.sliderVolume, this.btnTogglePlaySound, this.btnReset);
	}
	
	@Override
	public List<? extends NarratableEntry> narratables()
	{
		return List.of(this.sliderVolume, this.btnTogglePlaySound, this.btnReset);
	}
	
	// Like SoundManager.refreshCategoryVolume(SoundSource.MASTER) but more efficient
	private void updateExistingSoundVolume(final Identifier id)
	{
		final SoundEngine soundEngine = this.soundManager.soundEngine;
		if(!soundEngine.loaded)
		{
			return;
		}
		
		soundEngine.instanceToChannel.forEach((soundInstance, channelHandle) -> {
			if(id.equals(soundInstance.getIdentifier()))
			{
				final float newVolume = soundEngine.calculateVolume(soundInstance);
				channelHandle.execute(channel -> channel.setVolume(newVolume));
			}
		});
	}
	
	static class TogglePlayButtonIcon extends SpriteIconButton.CenteredIcon
	{
		static final Component TOGGLE_SOUND_COMPONENT = Component.literal("Toggle sound");
		
		TogglePlayButtonIcon(final OnPress onPress)
		{
			super(
				BUTTON_SIZE,
				BUTTON_SIZE,
				TOGGLE_SOUND_COMPONENT,
				16,
				16,
				0,
				0,
				new WidgetSprites(SPEAKER_ICON_TEXTURE),
				onPress,
				TOGGLE_SOUND_COMPONENT,
				null,
				false);
		}
		
		@Override
		public void playDownSound(final SoundManager soundManager)
		{
			// Don't play it
		}
	}
}

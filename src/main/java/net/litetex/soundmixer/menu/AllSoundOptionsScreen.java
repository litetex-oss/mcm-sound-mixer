package net.litetex.soundmixer.menu;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.litetex.soundmixer.SoundMixer;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;


@SuppressWarnings("checkstyle:MagicNumber")
public class AllSoundOptionsScreen extends OptionsSubScreen
{
	public static final Component TITLE = Component.literal("Individual Sounds");
	
	private static final Comparator<Map.Entry<SoundInstance, Integer>> SOUND_DEL_TIME_REVERSE_COMPARATOR =
		Map.Entry.<SoundInstance, Integer>comparingByValue().reversed();
	private static final Identifier UI_BUTTON_CLICK =
		Identifier.fromNamespaceAndPath("minecraft", "ui.button.click");
	
	private final Map<Identifier, SoundData> cachedSoundData = HashMap.newHashMap(1_000);
	private final Set<Identifier> allSoundEventIdsSorted;
	
	private final EditBox searchField;
	private final CycleButton<Filter> btnFilter;
	@SuppressWarnings("checkstyle:IllegalIdentifierName")
	private final VolumeList volumeList;
	private final CycleButton<Boolean> btnShowIds;
	
	private Filter filter = Filter.ALL;
	
	private final SoundManager soundManager;
	private final OptionsSoundManager optionsSoundManager;
	private final SoundMixer soundMixer = SoundMixer.instance();
	
	@SuppressWarnings("checkstyle:MagicNumber")
	public AllSoundOptionsScreen(final Screen parent, final Options options)
	{
		super(parent, options, TITLE);
		
		this.soundManager = this.minecraft.getSoundManager();
		this.optionsSoundManager = new OptionsSoundManager(this.soundManager);
		this.allSoundEventIdsSorted = this.soundManager.getAvailableSounds()
			.stream()
			.sorted()
			.collect(Collectors.toCollection(LinkedHashSet::new));
		
		this.searchField = new EditBox(
			this.font,
			28,
			35,
			1,
			20,
			Component.literal("Search for sound..."));
		this.searchField.setResponder(_ -> this.refreshItems());
		
		this.btnFilter = CycleButton.builder(
				f -> Component.literal(
					switch(f)
					{
						case ALL -> "All";
						case MODIFIED -> "Modified";
						case ACTIVE -> "Active";
					}),
				this.filter)
			.withValues(Filter.values())
			.create(
				Component.literal("Show"),
				(_, newValue) -> {
					this.filter = newValue;
					this.refreshItems();
				}
			);
		this.btnFilter.setY(35);
		this.btnFilter.setWidth(120);
		
		this.volumeList = new VolumeList(this.minecraft);
		
		this.btnShowIds = CycleButton.onOffBuilder(this.soundMixer.isShowIds())
			.withTooltip(_ -> Tooltip.create(
				Component.literal("Instead of existing sound translations.\n"
					+ "Also affects the in-game subtitle overlay.")))
			.create(
				Component.literal("Always show ids"),
				(_, enabled) -> {
					this.soundMixer.setShowIds(enabled);
					this.volumeList.updateShowIdentifier(enabled);
				});
	}
	
	@Override
	protected void addContents()
	{
		this.addRenderableWidget(this.searchField);
		this.addRenderableWidget(this.btnFilter);
		this.addRenderableWidget(this.btnShowIds);
		
		this.addRenderableWidget(this.volumeList);
		this.refreshItems();
		
		this.setInitialFocus(this.searchField);
	}
	
	@Override
	protected void addOptions()
	{
	}
	
	private void refreshItems()
	{
		Stream<Map.Entry<SoundData, Float>> items = this.getItemsForCurrentFilter()
			.map(e -> Map.entry(
				this.cachedSoundData.computeIfAbsent(e.getKey(), id -> SoundData.create(id, this.soundManager)),
				e.getValue()));
		
		final boolean showSubtitleIds = SoundMixer.instance().isShowIds();
		
		if(!this.searchField.getValue().isEmpty())
		{
			final String searchFieldLowerCase = this.searchField.getValue().toLowerCase(Locale.ROOT);
			
			items = items.filter(e -> {
				final SoundData key = e.getKey();
				return (showSubtitleIds ? key.idLowerCase() : key.translationLowerCase())
					.get().contains(searchFieldLowerCase);
			});
		}
		
		this.volumeList.rebuildItems(
			items,
			showSubtitleIds,
			this.options,
			this.optionsSoundManager);
	}
	
	private Stream<Map.Entry<Identifier, Float>> getItemsForCurrentFilter()
	{
		if(this.filter == Filter.MODIFIED)
		{
			return this.soundMixer.allSoundIdVolumes().entrySet().stream();
		}
		
		final Stream<Identifier> ids = this.filter == Filter.ACTIVE
			? this.soundManager.soundEngine.soundDeleteTime.entrySet().stream()
			.sorted(SOUND_DEL_TIME_REVERSE_COMPARATOR)
			.map(Map.Entry::getKey)
			.map(SoundInstance::getIdentifier)
			// This should never be shown
			.filter(id -> !UI_BUTTON_CLICK.equals(id))
			.distinct()
			: this.allSoundEventIdsSorted.stream();
		
		return ids.map(id -> Map.entry(id, this.soundMixer.getAdjustedVolume(id, SoundMixer.DEFAULT_VOLUME)));
	}
	
	@Override
	public void removed()
	{
		this.optionsSoundManager.close();
		SoundMixer.instance().saveConfigIfRequired();
	}
	
	@Override
	protected void repositionElements()
	{
		super.repositionElements();
		
		this.volumeList.updateSizeAndPosition(
			this.width,
			Math.max(this.layout.getContentHeight() - 28, 0),
			this.volumeList.getX(),
			this.layout.getHeaderHeight() + 28);
		
		this.searchField.setWidth(this.volumeList.getRowWidth() - 8 - this.btnFilter.getWidth());
		
		this.btnFilter.setX(this.searchField.getRight() + 8);
	}
	
	@Override
	protected void addFooter()
	{
		final LinearLayout footerLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		
		footerLayout.addChild(this.btnShowIds);
		footerLayout.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> this.onClose()).build());
	}
	
	enum Filter
	{
		ALL,
		MODIFIED,
		ACTIVE
	}
}

package net.litetex.soundmixer.menu;

import java.util.Map;
import java.util.stream.Stream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;


@Environment(EnvType.CLIENT)
public class VolumeList extends ContainerObjectSelectionList<VolumeEntry>
{
	private static final int ROW_HEIGHT = 25;
	
	public VolumeList(final Minecraft client)
	{
		super(client, 1, 1, 1, ROW_HEIGHT);
		this.centerListVertically = false;
	}
	
	@Override
	public int getRowWidth()
	{
		return Math.max(this.getWidth() - 50, 100);
	}
	
	public void rebuildItems(
		final Stream<Map.Entry<SoundData, Float>> items,
		final boolean showIdentifier,
		final Options options,
		final OptionsSoundManager optionsSoundManager)
	{
		this.clearEntries();
		this.setScrollAmount(0);
		
		items
			.map(e -> new VolumeEntry(
				e.getKey(),
				showIdentifier,
				e.getValue(),
				this.minecraft.getSoundManager(),
				options,
				optionsSoundManager))
			.forEach(this::addEntry);
	}
	
	public void updateShowIdentifier(final boolean showIdentifier)
	{
		this.children().forEach(c -> c.updateShowIdentifier(showIdentifier));
	}
}

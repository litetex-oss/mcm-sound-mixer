package net.litetex.soundmixer.config;

import java.util.Map;
import java.util.TreeMap;


public class Config
{
	private boolean showSubtitleIds;
	private Map<String, Float> soundIdVolumes = new TreeMap<>();
	
	public static Config createDefault()
	{
		return new Config();
	}
	
	public boolean isShowSubtitleIds()
	{
		return this.showSubtitleIds;
	}
	
	public void setShowSubtitleIds(final boolean showSubtitleIds)
	{
		this.showSubtitleIds = showSubtitleIds;
	}
	
	public Map<String, Float> getSoundIdVolumes()
	{
		return this.soundIdVolumes;
	}
	
	public void setSoundIdVolumes(final Map<String, Float> soundIdVolumes)
	{
		this.soundIdVolumes = soundIdVolumes;
	}
}

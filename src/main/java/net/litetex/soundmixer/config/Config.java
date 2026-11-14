package net.litetex.soundmixer.config;

import java.util.Map;
import java.util.TreeMap;


public class Config
{
	private boolean showIds;
	private Map<String, Float> soundIdVolumes = new TreeMap<>();
	
	public static Config createDefault()
	{
		return new Config();
	}
	
	public boolean isShowIds()
	{
		return this.showIds;
	}
	
	public void setShowIds(final boolean showIds)
	{
		this.showIds = showIds;
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

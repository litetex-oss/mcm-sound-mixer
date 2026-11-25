package net.litetex.soundmixer.fabric.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.litetex.soundmixer.menu.AllSoundOptionsScreen;
import net.minecraft.client.Minecraft;


public class ModMenuCompatibility implements ModMenuApi
{
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return s -> new AllSoundOptionsScreen(s, Minecraft.getInstance().options);
	}
}

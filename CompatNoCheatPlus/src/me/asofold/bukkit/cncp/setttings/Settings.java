package me.asofold.bukkit.cncp.setttings;

import java.util.LinkedHashSet;
import java.util.Set;

import me.asofold.bukkit.cncp.config.compatlayer.CompatConfig;
import me.asofold.bukkit.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bukkit.cncp.config.compatlayer.NewConfig;

public class Settings {
	public Set<String> forceEnableLater = new LinkedHashSet<String>();
	public Set<String> loadPlugins = new LinkedHashSet<String>();
	
	public static CompatConfig getDefaultConfig(){
		CompatConfig cfg = new NewConfig(null);
		cfg.set("plugins.force-enable-later", ConfigUtil.asList(new String[]{ "NoCheatPlus" }));
		cfg.set("plugins.ensure-enable", ConfigUtil.asList(new String[]{ "WorldGuard" }));
		return cfg;
	}
	
	public static boolean addDefaults(CompatConfig cfg){
		return ConfigUtil.forceDefaults(getDefaultConfig(), cfg);
	}
	
	public boolean fromConfig(CompatConfig cfg){
		// plugins to force enabling after this plugin.
		ConfigUtil.readStringSetFromList(cfg, "plugins.force-enable-later", forceEnableLater,  true, true, false);
		ConfigUtil.readStringSetFromList(cfg, "plugins.ensure-enable", loadPlugins,  true, true, false);
		return true;
	}

	public void clear() {
		forceEnableLater.clear();
	}
	
}

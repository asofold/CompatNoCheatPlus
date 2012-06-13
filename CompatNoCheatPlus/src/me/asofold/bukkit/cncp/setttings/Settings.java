package me.asofold.bukkit.cncp.setttings;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import me.asofold.bukkit.cncp.config.compatlayer.CompatConfig;
import me.asofold.bukkit.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bukkit.cncp.config.compatlayer.NewConfig;

public class Settings {
	public Set<String> forceEnableLater = new HashSet<String>();
	
	
	public static CompatConfig getDefaultConfig(){
		CompatConfig cfg = new NewConfig(null);
		List<String> tempList = new LinkedList<String>();
		tempList.add("NoCheatPlus");
		cfg.set("plugins.force-enable-later", tempList);
		return cfg;
	}
	
	public static boolean addDefaults(CompatConfig cfg){
		return ConfigUtil.forceDefaults(getDefaultConfig(), cfg);
	}
	
	public boolean fromConfig(CompatConfig cfg){
		// plugins to force enabling after this plugin.
		ConfigUtil.readStringSetFromList(cfg, "plugins.force-enable-later", forceEnableLater,  true, true, false);
		return true;
	}

	public void clear() {
		forceEnableLater.clear();
	}
	
}

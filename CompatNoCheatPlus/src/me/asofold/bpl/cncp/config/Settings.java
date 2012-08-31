package me.asofold.bpl.cncp.config;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bpl.cncp.config.compatlayer.NewConfig;

public class Settings {
	public int configVersion = 1;
	
	public Set<String> forceEnableLater = new LinkedHashSet<String>();
	public Set<String> loadPlugins = new LinkedHashSet<String>();
	
	public static Set<String> preventAddHooks = new HashSet<String>();
	
	public static CompatConfig getDefaultConfig(){
		CompatConfig cfg = new NewConfig(null);
		Settings ref = new Settings();
		cfg.set("plugins.force-enable-later", new LinkedList<String>()); // ConfigUtil.asList(new String[]{ "NoCheatPlus" }));
		cfg.set("plugins.ensure-enable", ConfigUtil.asList(new String[]{ "WorldGuard" }));
		cfg.set("hooks.prevent-add", new LinkedList<String>());
		cfg.set("configversion", ref.configVersion);
		return cfg;
	}
	
	public static boolean addDefaults(CompatConfig cfg){
		boolean changed = false;
		if (cfg.getInt("configversion", 0) == 0){
			cfg.remove("plugins");
			cfg.set("configversion", new Settings().configVersion); // hum.
			changed = true;
		}
		if (ConfigUtil.forceDefaults(getDefaultConfig(), cfg)) changed = true;
		return changed;
	}
	
	public boolean fromConfig(CompatConfig cfg){
//		Settings ref = new Settings();
		
		// plugins to force enabling after this plugin.
		ConfigUtil.readStringSetFromList(cfg, "plugins.force-enable-later", forceEnableLater,  true, true, false);
		ConfigUtil.readStringSetFromList(cfg, "plugins.ensure-enable", loadPlugins,  true, true, false);
		
		// General
		ConfigUtil.readStringSetFromList(cfg, "hooks.prevent-add", preventAddHooks, true, true, false);
		return true;
	}

	public void clear() {
		forceEnableLater.clear();
	}
	
}

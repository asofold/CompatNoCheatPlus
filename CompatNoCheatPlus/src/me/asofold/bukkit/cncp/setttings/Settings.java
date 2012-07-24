package me.asofold.bukkit.cncp.setttings;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import me.asofold.bukkit.cncp.config.compatlayer.CompatConfig;
import me.asofold.bukkit.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bukkit.cncp.config.compatlayer.NewConfig;

public class Settings {
	public Set<String> forceEnableLater = new LinkedHashSet<String>();
	public Set<String> loadPlugins = new LinkedHashSet<String>();
	public Set<String> exemptPlayerClassNames = new HashSet<String>();
	public boolean exemptAllPlayerClassNames = true;
	public String playerClassName = "CraftPlayer";
	public boolean exemptSuperClass = true;
	
	public static CompatConfig getDefaultConfig(){
		CompatConfig cfg = new NewConfig(null);
		Settings ref = new Settings();
		cfg.set("plugins.force-enable-later", ConfigUtil.asList(new String[]{ "NoCheatPlus" }));
		cfg.set("plugins.ensure-enable", ConfigUtil.asList(new String[]{ "WorldGuard" }));
		cfg.set("hooks.player-class.exempt-names", new LinkedList<String>());
		cfg.set("hooks.player-class.exempt-all", ref.exemptAllPlayerClassNames);
		cfg.set("hooks.player-class.class-name", ref.playerClassName);
		cfg.set("hooks.player-class.super-class", ref.exemptSuperClass);
		return cfg;
	}
	
	public static boolean addDefaults(CompatConfig cfg){
		return ConfigUtil.forceDefaults(getDefaultConfig(), cfg);
	}
	
	public boolean fromConfig(CompatConfig cfg){
		Settings ref = new Settings();
		// plugins to force enabling after this plugin.
		ConfigUtil.readStringSetFromList(cfg, "plugins.force-enable-later", forceEnableLater,  true, true, false);
		ConfigUtil.readStringSetFromList(cfg, "plugins.ensure-enable", loadPlugins,  true, true, false);
		// Generic player class name hook:
		ConfigUtil.readStringSetFromList(cfg, "hooks.player-class.exempt-names", exemptPlayerClassNames, true, true, false);
		exemptAllPlayerClassNames = cfg.getBoolean("hooks.player-class.exempt-all", ref.exemptAllPlayerClassNames);
		playerClassName = cfg.getString("hooks.player-class.class-name", ref.playerClassName);
		exemptSuperClass = cfg.getBoolean("hooks.player-class.super-class", ref.exemptSuperClass);
		return true;
	}

	public void clear() {
		forceEnableLater.clear();
	}
	
}

package me.asofold.bpl.cncp.config;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.checks.CheckType;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bpl.cncp.config.compatlayer.NewConfig;

public class Settings {
	public static final int configVersion = 2;
	
	public Set<String> forceEnableLater = new LinkedHashSet<String>();
	public Set<String> loadPlugins = new LinkedHashSet<String>();
	public Set<CheckType> extemptChecks = new LinkedHashSet<CheckType>();
	
	public static Set<String> preventAddHooks = new HashSet<String>();
	
	public static CompatConfig getDefaultConfig(){
		CompatConfig cfg = new NewConfig(null);
		cfg.set("plugins.force-enable-later", new LinkedList<String>()); // ConfigUtil.asList(new String[]{ "NoCheatPlus" }));
		cfg.set("plugins.ensure-enable", new LinkedList<String>()); // ConfigUtil.asList(new String[]{ "WorldGuard" }));
		cfg.set("plugins.bedrock-extempt-checks", ConfigUtil.asList(new String[]{ 
		        "BLOCKINTERACT_VISIBLE",
		        "BLOCKINTERACT_DIRECTION",
		        "BLOCKINTERACT_REACH",
		        "BLOCKBREAK_DIRECTION",
		        "BLOCKBREAK_NOSWING",
		        "BLOCKBREAK_REACH",
		        "BLOCKPLACE_NOSWING",
		        "BLOCKPLACE_DIRECTION",
		        "BLOCKPLACE_REACH",
		        "BLOCKPLACE_SCAFFOLD",
		        "FIGHT_DIRECTION",
		        })); 
		cfg.set("hooks.prevent-add", new LinkedList<String>());
		cfg.set("configversion", configVersion);
		return cfg;
	}
	
	public static boolean addDefaults(CompatConfig cfg){
		boolean changed = false;
		if (cfg.getInt("configversion", 0) == 0){
			cfg.remove("plugins");
			cfg.set("configversion", configVersion);
			changed = true;
		}
		if (cfg.getInt("configversion", 0) <= 1){
			if (cfg.getDouble("hooks.set-speed.fly-speed", 0.1) != 0.1){
				changed = true;
				cfg.set("hooks.set-speed.fly-speed", 0.1);
				Bukkit.getLogger().warning("[cncp] Reset fly-speed for the set-speed hook to 0.1 (default) as a safety measure.");
			}
			if (cfg.getDouble("hooks.set-speed.walk-speed", 0.2) != 0.2){
				changed = true;
				cfg.set("hooks.set-speed.walk-speed", 0.2);
				Bukkit.getLogger().warning("[cncp] Reset walk-speed for the set-speed hook to 0.2 (default) as a safety measure.");
			}
		}
		if (ConfigUtil.forceDefaults(getDefaultConfig(), cfg)) changed = true;
		if (cfg.getInt("configversion", 0) != configVersion){
			cfg.set("configversion", configVersion);
			changed = true;
		}
		return changed;
	}
	
	public boolean fromConfig(CompatConfig cfg){
//		Settings ref = new Settings();
		
		// plugins to force enabling after this plugin.
		ConfigUtil.readStringSetFromList(cfg, "plugins.force-enable-later", forceEnableLater,  true, true, false);
		ConfigUtil.readStringSetFromList(cfg, "plugins.ensure-enable", loadPlugins,  true, true, false);

		ConfigUtil.readCheckTypeSetFromList(cfg, "plugins.bedrock-extempt-checks", extemptChecks, true, true, true);

		// General
		ConfigUtil.readStringSetFromList(cfg, "hooks.prevent-add", preventAddHooks, true, true, false);
		return true;
	}

	public void clear() {
		forceEnableLater.clear();
	}
	
}

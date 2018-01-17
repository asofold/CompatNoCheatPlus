package me.asofold.bpl.cncp.config;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.bukkit.Bukkit;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bpl.cncp.config.compatlayer.NewConfig;

public class Settings {
    public static final int configVersion = 3;

    public static Set<String> preventAddHooks = new HashSet<String>();

    public static CompatConfig getDefaultConfig(){
        CompatConfig cfg = new NewConfig(null);
        cfg.set("plugins.force-enable-later", new LinkedList<String>()); // ConfigUtil.asList(new String[]{ "NoCheatPlus" }));
        cfg.set("plugins.ensure-enable", new LinkedList<String>()); // ConfigUtil.asList(new String[]{ "WorldGuard" }));
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


        // General
        ConfigUtil.readStringSetFromList(cfg, "hooks.prevent-add", preventAddHooks, true, true, false);
        return true;
    }

    public void clear() {
        // TODO: clear something !?
    }

}

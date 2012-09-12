package me.asofold.bpl.cncp.hooks.generic;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bpl.cncp.hooks.AbstractHook;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPHook;

public final class HookPlayerClass extends AbstractHook implements ConfigurableHook {
	
	protected final Set<String> classNames = new HashSet<String>();
	
	protected boolean exemptAll = true;
	
	protected boolean checkSuperClass = true;
	
	protected Object ncpHook = null;
	
	protected boolean enabled = true;
	
	/**
	 * Normal class name.
	 */
	protected String playerClassName = "CraftPlayer";
	
	public HookPlayerClass(){
		this.classNames.addAll(classNames);
	}

	@Override
	public final String getHookName() {
		return "PlayerClass(default)";
	}

	@Override
	public final String getHookVersion() {
		return "1.1";
	}

	@Override
	public NCPHook getNCPHook() {
		if (ncpHook == null){
			ncpHook = new NCPHook() {
				@Override
				public boolean onCheckFailure(CheckType checkType, Player player) {
					if (exemptAll && !player.getClass().getSimpleName().equals(playerClassName)) return true;
					else {
						if (classNames.isEmpty()) return false;
						final Class<?> clazz = player.getClass();
						final String name = clazz.getSimpleName();
						if (classNames.contains(name)) return true;
						else if (checkSuperClass){
							while (true){
								final Class<?> superClass = clazz.getSuperclass();
								if (superClass  == null) return false;
								else{
									final String superName = superClass.getSimpleName();
									if (superName.equals("Object")) return false;
									else if (classNames.contains(superName)){
										return true;
									}
								}
							} 
						}
					}
					return false; // ECLIPSE
				}
				
				@Override
				public String getHookVersion() {
					return "2.0";
				}
				
				@Override
				public String getHookName() {
					return "PlayerClass(cncp)";
				}
			};
		}
		return (NCPHook) ncpHook;
	}

	@Override
	public void applyConfig(CompatConfig cfg, String prefix) {
		enabled = cfg.getBoolean(prefix + "player-class.enabled", true);
		ConfigUtil.readStringSetFromList(cfg, prefix + "player-class.exempt-names", classNames, true, true, false);
		exemptAll = cfg.getBoolean(prefix + "player-class.exempt-all",  true);
		playerClassName = cfg.getString(prefix + "player-class.class-name", "CraftPlayer");
		checkSuperClass = cfg.getBoolean(prefix + "player-class.super-class", true);
	}

	@Override
	public boolean updateConfig(CompatConfig cfg, String prefix) {
		CompatConfig defaults = CompatConfigFactory.getConfig(null);
		defaults.set(prefix + "player-class.enabled", true);
		defaults.set(prefix + "player-class.exempt-names", new LinkedList<String>());
		defaults.set(prefix + "player-class.exempt-all", true);
		defaults.set(prefix + "player-class.class-name", "CraftPlayer");
		defaults.set(prefix + "player-class.super-class", true);
		return ConfigUtil.forceDefaults(defaults, cfg);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}

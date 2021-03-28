package me.asofold.bpl.cncp.hooks.generic;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bpl.cncp.hooks.AbstractHook;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * Exempting players by class names for some class.
 * @author mc_dev
 *
 */
public abstract class ClassExemptionHook extends AbstractHook implements ConfigurableHook{

	protected final ExemptionManager man = new ExemptionManager();
	
	protected final List<String> defaultClasses = new LinkedList<String>();
	protected final LinkedHashSet<String> classes = new LinkedHashSet<String>();
	
	protected boolean defaultEnabled = true;
	protected boolean enabled = true;
	
	protected final String configPrefix;
	
	public ClassExemptionHook(String configPrefix){
		this.configPrefix = configPrefix;
	}
	
	public void setClasses(final Collection<String> classes){
		this.classes.clear();
		this.classes.addAll(classes);
	}
	
	/**
	 * Check if a player is to be exempted and exempt for the check type.
	 * @param player
	 * @param clazz
	 * @param checkType
	 * @return If exempted.
	 */
	public boolean checkExempt(final Player player, final Class<?> clazz, final CheckType checkType){
		if (!classes.contains(clazz.getSimpleName())) return false;
		return man.addExemption(player, checkType);
	}
	
	/**
	 * 
	 * @param player
	 * @param checkType
	 * @return If the player is still exempted.
	 */
	public boolean checkUnexempt(final Player player, final Class<?> clazz, final CheckType checkType){
		if (!classes.contains(clazz.getSimpleName())) return false;
		return man.removeExemption(player, checkType);
	}

	@Override
	public void applyConfig(CompatConfig cfg, String prefix) {
		enabled = cfg.getBoolean(prefix + configPrefix + "enabled",  defaultEnabled);
		ConfigUtil.readStringSetFromList(cfg, prefix + configPrefix + "exempt-names", classes, true, true, false);
	}

	@Override
	public boolean updateConfig(CompatConfig cfg, String prefix) {
		CompatConfig defaults = CompatConfigFactory.getConfig(null);
		defaults.set(prefix + configPrefix + "enabled",  defaultEnabled);
		defaults.set(prefix + configPrefix + "exempt-names", defaultClasses);
		return ConfigUtil.forceDefaults(defaults, cfg);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}

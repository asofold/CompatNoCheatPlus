package me.asofold.bpl.cncp.hooks.generic;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

/**
 * Exempting players by class names for some class.
 * @author mc_dev
 *
 */
public abstract class ClassExemptionHook extends ExemptionHook implements ConfigurableHook{

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
		addExemption(player.getName());
		exempt(player, checkType);
		return true;
	}
	
	/**
	 * 
	 * @param player
	 * @param checkType
	 * @return If the player is still exempted.
	 */
	public boolean checkUnexempt(final Player player, final Class<?> clazz, final CheckType checkType){
		if (!classes.contains(clazz.getSimpleName())) return false;
		if (removeExemption(player.getName())) return true;
		else {
			unexempt(player, checkType);
			return false;
		}
	}
	
	/**
	 * Hides the API access from listeners potentially.
	 * @param player
	 * @param checkType
	 */
	public void exempt(final Player player, final CheckType checkType){
		NCPExemptionManager.exemptPermanently(player, checkType);
	}
	
	/**
	 * Hides the API access from listeners potentially.
	 * @param player
	 * @param checkType
	 */
	public void unexempt(final Player player, final CheckType checkType){
		NCPExemptionManager.unexempt(player, checkType);
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

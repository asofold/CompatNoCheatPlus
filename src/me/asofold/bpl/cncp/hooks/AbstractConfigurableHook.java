package me.asofold.bpl.cncp.hooks;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bpl.cncp.hooks.generic.ConfigurableHook;

/**
 * Reads an enabled flag from the config (true by default). The prefix given will be something like "hooks.", so add your hook name to the path like:<br>
 * prefix + "myhook.extra-config"
 * @author mc_dev
 *
 */
public class AbstractConfigurableHook extends AbstractHook implements
		ConfigurableHook {
	
	protected final String configPrefix;
	protected final String hookName, hookVersion;
	
	protected boolean enabled = true;
	
	public AbstractConfigurableHook(String hookName, String hookVersion, String configPrefix){
		this.configPrefix = configPrefix;
		this.hookName = hookName;
		this.hookVersion = hookVersion;
	}

	@Override
	public String getHookName() {
		return hookName;
	}

	@Override
	public String getHookVersion() {
		return hookVersion;
	}

	@Override
	public void applyConfig(CompatConfig cfg, String prefix) {
		enabled = cfg.getBoolean(prefix + configPrefix + "enabled", true);
	}

	@Override
	public boolean updateConfig(CompatConfig cfg, String prefix) {
		CompatConfig defaults = CompatConfigFactory.getConfig(null);
		defaults.set(prefix + configPrefix + "enabled",  true);
		return ConfigUtil.forceDefaults(defaults, cfg);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	

}

package me.asofold.bpl.cncp.hooks.generic;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;

/**
 * A hook that is using configuration.
 * @author mc_dev
 *
 */
public interface ConfigurableHook {
	
	/**
	 * Adjust internals to the given configuration.
	 * @param cfg
	 * @param prefix
	 */
	public void applyConfig(CompatConfig cfg, String prefix);
	
	/**
	 * Update the given configuration with defaults where / if necessary (no blunt overwrite, it is a users config). 
	 * @param cfg
	 * @param prefix
	 * @return If the configuration was changed.
	 */
	public boolean updateConfig(CompatConfig cfg, String prefix);
	
	/**
	 * If the hook is enabled by configuration or not.
	 * @return
	 */
	public boolean isEnabled();
}

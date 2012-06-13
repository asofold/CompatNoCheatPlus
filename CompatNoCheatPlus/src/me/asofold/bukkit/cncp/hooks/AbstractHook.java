package me.asofold.bukkit.cncp.hooks;

import org.bukkit.event.Listener;

/**
 * Extend this to make sure that future changes will not break your hooks.<br>
 * Probable changes:<br>
 * - Adding onReload method.
 * @author mc_dev
 *
 */
public abstract class AbstractHook implements Hook{

	@Override
	public String[][] getCheckSpec() {
		// Handle all CheckEvents (improbable).
		return null;
	}

	@Override
	public Listener[] getListeners() {
		// No listeners (improbable).
		return null;
	}

}

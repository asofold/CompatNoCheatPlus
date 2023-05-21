package me.asofold.bpl.cncp.hooks;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPHook;

/**
 * Extend this to make sure that future changes will not break your hooks.<br>
 * Probable changes:<br>
 * - Adding onReload method.
 * @author mc_dev
 *
 */
public abstract class AbstractHook implements Hook{

	@Override
	public CheckType[] getCheckTypes() {
		// Handle all CheckEvents (improbable).
		return null;
	}

	@Override
	public Listener[] getListeners() {
		// No listeners to register with cncp.
		return null;
	}
	
	
	
	@Override
	public NCPHook getNCPHook() {
		// No hooks to add to NCP.
		return null;
	}

	/**
	 * Throw a runtime exception if the plugin is not present.
	 * @param pluginName
	 */
	protected void assertPluginPresent(String pluginName){
		if (Bukkit.getPluginManager().getPlugin(pluginName) == null) throw new RuntimeException("Assertion, " + getHookName() + ": Plugin " + pluginName + " is not present.");
	}

}

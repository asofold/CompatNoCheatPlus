package me.asofold.bpl.cncp.hooks;

import org.bukkit.event.Listener;

import fr.neatmonster.nocheatplus.checks.CheckEvent;

/**
 * Interface for hooking into another plugin.<br>
 * 
 * @author mc_dev
 *
 */
public interface Hook{
	/**
	 * Must not cause exceptions.
	 * @return
	 */
	public String getHookName();
	
	/**
	 * Must not cause exceptions.
	 * @return
	 */
	public String getHookVersion();
	
	/**
	 * Get the specification for which checks to call this.<br>
	 * <hr>
	 * The return value should be an array of arrays, each of which specifies the group name and the check names within that group.<br>
	 * You can return null to register for ALL checks.<br>
	 * You can just register the group name to get all checks for that group.<br>
	 * <hr>
	 * Currently group and check names are processed with trim().toLowerCase() !
	 * @return (see description)
	 */
	public String[][] getCheckSpec();
	
	/**
	 * Get listener instances to be registered with cncp.
	 * @return null if unused.
	 */
	public Listener[] getListeners();
	
	/**
	 * This will only be called if the event has not yet been cancelled !<br>
	 * Cancel the event, and no further hooks will process this.
	 * @param group Name of check group
	 * @param check Name of check .
	 * @param event
	 */
	public void processEvent(final String group, final String check, final CheckEvent event);
	
}

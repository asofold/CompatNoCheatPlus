package me.asofold.bpl.cncp.hooks;

import me.asofold.bpl.cncp.hooks.ncp.NCPHook;

import org.bukkit.event.Listener;

/**
 * Interface for hooking into another plugin.<br>
 * NOTE: You also have to implement the methods described in NCPHook.<br>
 * If you do not use listeners or don't want the prevent-hooks configuration feature 
 * to take effect, you can also register directly with NCPHookManager.
 * 
 * @author mc_dev
 *
 */
public interface Hook extends NCPHook{
	
	/**
	 * The check ids to register for, see NCPHookManager for reference, you can use ALL,   
	 */
	public Integer[] getCheckSpec();
	
	/**
	 * Get listener instances to be registered with cncp.
	 * @return null if unused.
	 */
	public Listener[] getListeners();
	
}

package me.asofold.bpl.cncp.hooks;

import org.bukkit.event.Listener;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPHook;

/**
 * Interface for hooking into another plugin.<br>
 * NOTE: You also have to implement the methods described in NCPHook.<br>
 * If you do not use listeners or don't want the prevent-hooks configuration feature 
 * to take effect, you can also register directly with NCPHookManager.
 * 
 * @author mc_dev
 *
 */
public interface Hook{
	
	/**
	 * 
	 * @return
	 */
	public String getHookName();
	
	/**
	 * 
	 * @return
	 */
	public String getHookVersion();
	
	/**
	 * The check types to register for, see NCPHookManager for reference, you can use ALL,   
	 */
	public CheckType[] getCheckTypes();
	
	/**
	 * Get listener instances to be registered with cncp.
	 * @return null if unused.
	 */
	public Listener[] getListeners();
	
	/**
	 * Get the hook to be registered with NCP, the registration will take place after NoCheatPlus has been enabled.
	 * @return Should always return the same hook or null if not used.
	 */
	public NCPHook getNCPHook();
	
}

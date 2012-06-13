package me.asofold.bukkit.cncp.setttings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.asofold.bukkit.cncp.hooks.Hook;

/**
 * Check group specific lists of hooks.
 * @author mc_dev
 *
 */
public final class GroupHooks {
	
	/**
	 * Hooks that want all.
	 */
	public final ArrayList<Hook> all = new ArrayList<Hook>();
	
	/**
	 * Hooks that want a certain check.<br>
	 */
	public final Map<String , ArrayList<Hook>> byCheck = new HashMap<String, ArrayList<Hook>>(10);
}

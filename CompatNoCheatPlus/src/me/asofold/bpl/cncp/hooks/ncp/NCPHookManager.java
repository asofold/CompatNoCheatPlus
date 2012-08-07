package me.asofold.bpl.cncp.hooks.ncp;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


/**
 * After-check-failure hook manager to be integrated into NoCheatPlus.<br>
 * 
 * 
 * Questions: sync for registering, or extra map for chat ? [maybe setLocked flag with sync object just for changing and query from external thread]
 * 
 * @author mc_dev
 *
 */
public final class NCPHookManager {	
	
	
	/* ----------------------------------------------------------------------------*
	/*
	 * Ids for groups and checks:
	 * 
	 * TODO: could change to Enums with int values.
	 * 
	 * A check group starts at thousands and covers all
	 * ids till next thousand - 1, for instance 1000 to 1999.
	 * 
	 * This is important for also being able to check the check group for the check.
	 */
	
	// GENERAL
	/**
	 * Use to register for all checkfailures.
	 */
	public static final Integer ALL 					= 0;
	/**
	 * Do not use to register, only for internals / compatibility issues,
	 * it might be passed to NCPHook.onCheckFailure.
	 */
	public static final Integer UNKNOWN 				= -1;
	
	// MOVING
	public static final Integer MOVING 					= 1000;
	public static final Integer MOVING_NOFALL 			= 1001;
	public static final Integer MOVING_SURVIVALFLY 		= 1002;
	public static final Integer MOVING_CREATIVEFLY 		= 1003;
	
	// FIGHT
	public static final Integer FIGHT 					= 2000;
	public static final Integer FIGHT_SPEED 			= 2001;
	public static final Integer FIGHT_ANGLE 			= 2002;
	
	// BLOCKBREAK
	public static final Integer BLOCKBREAK 				= 3000;
	public static final Integer BLOCKBREAK_FASTBREAK	= 3001;
	public static final Integer BLOCKBREAK_NOSWING		= 3002;
	public static final Integer BLOCKBREAK_DIRECTION	= 3003;
	
	// BLOCKPLACE
	
	// ...
	
	
	/* ----------------------------------------------------------------------- */
	/* Internal data: */
	
	/**
	 * Ids given to hooks.
	 */
	private static int maxHookId = 0;
	
	/**
	 * Hook id to hook.
	 */
	private final static Map<Integer, NCPHook> allHooks = new HashMap<Integer, NCPHook>();
	
	/**
	 * Mapping the check ids to the hooks.
	 */
	private static final Map<Integer, List<NCPHook>> hooksByChecks = new HashMap<Integer, List<NCPHook>>();
	
	/* ----------------------------------------------------------------- */
	/* Internals: manage hooks internally */
	
	private static Integer getNewHookId(){
		maxHookId ++;
		return maxHookId;
	}
	
	/**
	 * for registration purposes only.
	 * @param hook
	 * @return Unique id associated with that hook (returns an existing id if hook is already present).
	 */
	private static Integer getId(NCPHook hook){
		if (hook == null) throw new NullPointerException("Hooks must not be null."); // just in case.
		Integer id = null;
		for (Integer refId : allHooks.keySet()){
			if (hook == allHooks.get(refId)){
				id = refId;
				break;
			}
		}
		if (id == null){
			id = getNewHookId();
			allHooks.put(id, hook);
		}
		return id;
	}
	
	/**
	 * Get the Id of the group for a given check id. (If the given id is a group id it will return the same value but a different object).
	 * @param checkId
	 * @return
	 */
	private final static Integer getGroupId(final Integer checkId){
		return (checkId.intValue() / 1000) * 1000;
	}
	
	/**
	 * Add hook to the hooksByChecks mappings, for the check id and if different group id. 
	 * assumes that the hook already has been registered in the allHooks map.
	 * @param checkId
	 * @param hook
	 */
	private static void addToMappings(Integer checkId, NCPHook hook) {
		Integer groupId = getGroupId(checkId);
		addToMapping(checkId, hook);
		if (checkId.intValue() != groupId.intValue()) addToMapping(groupId, hook);
	}
	
	/**
	 * 
	 * @param checkId
	 * @param hook
	 */
	private static void addToMapping(Integer checkId, NCPHook hook) {
		List<NCPHook> hooks = hooksByChecks.get(checkId);
		if (hooks == null){
			hooks = new ArrayList<NCPHook>();
			hooks.add(hook);
			hooksByChecks.put(checkId, hooks);
		}
		else if (!hooks.contains(hook)) hooks.add(hook);
	}

	/**
	 * Remove from internal mappings, both allHooks and hooksByChecks.
	 * @param hook
	 * @param hookId 
	 */
	private static void removeFromMappings(NCPHook hook, Integer hookId) {
		allHooks.remove(hookId);
		List<Integer> rem = new LinkedList<Integer>();
		for (Integer checkId : hooksByChecks.keySet()){
			List<NCPHook> hooks = hooksByChecks.get(checkId);
			if (hooks.remove(hook)){
				if (hooks.isEmpty()) rem.add(checkId);
			}
		}  
		for (Integer checkId : rem){
			hooksByChecks.remove(checkId);
		}
	}
	
	private static final String getHookDescription(final NCPHook hook){
		return hook.getHookName() + " [" + hook.getHookVersion() + "]";
	}
	
	private static final void logHookAdded(NCPHook hook){
		Bukkit.getLogger().info("[NoCheatPlus/Compat] Added hook: " + getHookDescription(hook));
	}
	
	private static final void logHookRemoved(NCPHook hook){
		Bukkit.getLogger().info("[NoCheatPlus/Compat] Removed hook: " + getHookDescription(hook));
	}
	
	private static final void logHookFailure(final Integer groupId, final Integer checkId, final Player player, final NCPHook hook, final Throwable t){
		// TODO: might accumulate failure rate and only log every so and so seconds or disable hook if spamming (leads to ncp spam though)?
		final StringBuilder builder = new StringBuilder(1024);
		builder.append("[NoCheatPlus/Compat] Hook " + getHookDescription(hook) + " encountered an unexpected exception:\n");
		builder.append("Processing: ");
		if (checkId.intValue() != groupId.intValue())  builder.append("Group " + groupId + " ");
		builder.append("Check " + checkId);
		builder.append(" Player " + player.getName());
		builder.append("\n");
		builder.append("Exception (" + t.getClass().getSimpleName() + "): " + t.getMessage() + "\n");
		for (StackTraceElement el : t.getStackTrace()){
			builder.append(el.toString());
		}
		
		Bukkit.getLogger().severe(builder.toString());
	}
	
	private static final boolean applyHooks(final Integer groupId, final Integer checkId, final Player player, final List<NCPHook> hooks) {
		for (int i = 0; i < hooks.size(); i ++){
			final NCPHook hook = hooks.get(i);
			try{
				if (hook.onCheckFailure(groupId, checkId, player)) return true;
			}
			catch (Throwable t){
				// TODO: maybe distinguish some exceptions here (Interrupted ?).
				logHookFailure(groupId, checkId, player, hook, t);
			}
		}
		return false;
	}
	
	/* ------------------------------------------------------------------------------------- */
	/* Internal API for NCP */
	/*
	 * Internally NCP will now instead of events call hooks: 
	 */
	
	/**
	 * This is called by checks when players fail them.
	 * @param checkId Id for the check, should be taken from the constants defined in this class.
	 * @param player The player that fails the check.
	 * @return if to cancel the VL processing.
	 */
	public static final boolean shouldCancelVLProcessing(final Integer checkId, final Player player){
		
		// checks for hooks registered for all events, only for the group and specifically for the check.
		// A Paradigm could be to return true as soon as one hook has returned true.
		final Integer groupId = getGroupId(checkId);
		
		// Most specific:
		final List<NCPHook> hooksCheck = hooksByChecks.get(checkId);
		if (hooksCheck != null){
			if (applyHooks(groupId, checkId, player, hooksCheck)) return true;
		}
		
		// Group:
		if (checkId.intValue() != groupId.intValue()){
			final List<NCPHook> hooksGroup= hooksByChecks.get(groupId);
			if (hooksCheck != null){
				if (applyHooks(groupId, checkId, player, hooksGroup)) return true;
			}
		}
		
		// general (all):
		final List<NCPHook> hooksAll = hooksByChecks.get(ALL);
		if (hooksAll != null){
			if (applyHooks(groupId, checkId, player, hooksAll)) return true;
		}
		
		return false;
	}
	
	
	/* ----------------------------------------------------------------- */
	/* External API for adding hooks  etc. */

	/**
	 * Register a hook for a specific check id (all, group, or an individual check).
	 * @param checkId 
	 * @param hook
	 * @return An id to identify the hook, will return the existing id if the hook was already present somewhere. 
	 */
	public static Integer addHook(Integer checkId, NCPHook hook){
		Integer hookId = getId(hook);
		addToMappings(checkId, hook);
		logHookAdded(hook);
		return hookId;
	}

	/**
	 * Register a hook for several individual checks ids (all, group, or an individual checks).
	 * @param checkIds Array of ids to register the hook for. If you pass null this hok will be registered for all checks.
	 * @param hook
	 * @return
	 */
	public static Integer addHook(Integer[] checkIds, NCPHook hook){
		if (checkIds == null) checkIds = new Integer[]{NCPHookManager.ALL};
		Integer hookId = getId(hook);
		for (Integer checkId : checkIds){
			addToMappings(checkId, hook);
		}
		logHookAdded(hook);
		return hookId;
	}

	/**
	 * Remove a hook.
	 * @param hook
	 * @return hook id if present, null otherwise.
	 */
	public static Integer removeHook(NCPHook hook){
		Integer hookId = null;
		for (Integer refId : allHooks.keySet()){
			if (hook == allHooks.get(refId)){
				hookId = refId;
				break;
			}
		}
		if (hookId == null) return null;
		removeFromMappings(hook, hookId);
		logHookRemoved(hook);
		return hookId;
	}


	/**
	 * Remove a hook by its hook id (returned on adding hooks).
	 * @param hookId if present, null otherwise.
	 * @return
	 */
	public static NCPHook removeHook(Integer hookId){
		NCPHook hook = allHooks.get(hookId);
		if (hook == null) return null;
		removeFromMappings(hook, hookId);
		logHookRemoved(hook);
		return hook;
	}
	
	/**
	 * Remove a hook by its name (case sensitive, exact match).
	 * @param hookName
	 * @return
	 */
	public static NCPHook removeHook(String hookName){
		NCPHook hook = getHookByName(hookName);
		if (hook == null) return null;
		removeHook(hook);
		return hook;
	}
	
	public static Collection<NCPHook> removeAllHooks(){
		Collection<NCPHook> hooks = getAllHooks();
		for (NCPHook hook : hooks){
			removeHook(hook);
		}
		return hooks;
	}
	
	/**
	 * Get the hook by the hook name.
	 * @param hookName case sensitive (exact match).
	 * @return NCPHook if found, null otherwise.
	 */
	public static NCPHook getHookByName(String hookName){
		for (Integer refId : allHooks.keySet()){
			NCPHook hook = allHooks.get(refId);
			if (hook.getHookName().equals(hookName)) return hook;
		}
		return null;
	}
	
	/**
	 * Get a collection of all hooks.
	 * @return
	 */
	public static Collection<NCPHook> getAllHooks(){
		List<NCPHook> hooks = new LinkedList<NCPHook>();
		hooks.addAll(allHooks.values());
		return hooks;
	}
	
}

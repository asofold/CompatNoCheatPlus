package me.asofold.bpl.cncp.hooks.ncp;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	 * Mapping the check types to the hooks.
	 */
	private static final Map<CheckType, List<NCPHook>> hooksByChecks = new HashMap<CheckType, List<NCPHook>>();
	
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
	 * Add hook to the hooksByChecks mappings, for the check type and if present, group type. 
	 * assumes that the hook already has been registered in the allHooks map.
	 * @param checkType
	 * @param hook
	 */
	private static void addToMappings(CheckType checkType, NCPHook hook) {
		addToMapping(checkType, hook);
		if (checkType.group != null) addToMapping(checkType.group, hook);
	}
	
	/**
	 * Add to the mapping for given check type (only).
	 * @param checkId
	 * @param hook
	 */
	private static void addToMapping(CheckType checkType, NCPHook hook) {
		List<NCPHook> hooks = hooksByChecks.get(checkType);
		if (hooks == null){
			hooks = new ArrayList<NCPHook>();
			hooks.add(hook);
			hooksByChecks.put(checkType, hooks);
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
		List<CheckType> rem = new LinkedList<CheckType>();
		for (CheckType checkId : hooksByChecks.keySet()){
			List<NCPHook> hooks = hooksByChecks.get(checkId);
			if (hooks.remove(hook)){
				if (hooks.isEmpty()) rem.add(checkId);
			}
		}  
		for (CheckType checkId : rem){
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
	
	private static final void logHookFailure(final CheckType checkType, final Player player, final NCPHook hook, final Throwable t){
		// TODO: might accumulate failure rate and only log every so and so seconds or disable hook if spamming (leads to ncp spam though)?
		final StringBuilder builder = new StringBuilder(1024);
		builder.append("[NoCheatPlus/Compat] Hook " + getHookDescription(hook) + " encountered an unexpected exception:\n");
		builder.append("Processing: ");
		if (checkType.group != null)  builder.append("Group " + checkType.group + " ");
		builder.append("Check " + checkType);
		builder.append(" Player " + player.getName());
		builder.append("\n");
		builder.append("Exception (" + t.getClass().getSimpleName() + "): " + t.getMessage() + "\n");
		for (StackTraceElement el : t.getStackTrace()){
			builder.append(el.toString());
		}
		
		Bukkit.getLogger().severe(builder.toString());
	}
	
	private static final boolean applyHooks(final CheckType checkType, final Player player, final List<NCPHook> hooks) {
		for (int i = 0; i < hooks.size(); i ++){
			final NCPHook hook = hooks.get(i);
			try{
				if (hook.onCheckFailure(checkType, player)) return true;
			}
			catch (Throwable t){
				// TODO: maybe distinguish some exceptions here (Interrupted ?).
				logHookFailure(checkType, player, hook, t);
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
	public static final boolean shouldCancelVLProcessing(final CheckType checkType, final Player player){
		
		// checks for hooks registered for all events, only for the group and specifically for the check.
		// A Paradigm could be to return true as soon as one hook has returned true.
		
		// Most specific:
		final List<NCPHook> hooksCheck = hooksByChecks.get(checkType);
		if (hooksCheck != null){
			if (applyHooks(checkType, player, hooksCheck)) return true;
		}
		
		// Group:
		if (checkType.group != null){
			final List<NCPHook> hooksGroup= hooksByChecks.get(checkType);
			if (hooksCheck != null){
				if (applyHooks(checkType, player, hooksGroup)) return true;
			}
		}
		
		// general (all):
		final List<NCPHook> hooksAll = hooksByChecks.get(CheckType.ALL);
		if (hooksAll != null){
			if (applyHooks(checkType, player, hooksAll)) return true;
		}
		
		return false;
	}
	
	
	/* ----------------------------------------------------------------- */
	/* External API for adding hooks  etc. */

	/**
	 * Register a hook for a specific check type (all, group, or an individual check).
	 * @param checkType 
	 * @param hook
	 * @return An id to identify the hook, will return the existing id if the hook was already present somewhere. 
	 */
	public static Integer addHook(CheckType checkType, NCPHook hook){
		Integer hookId = getId(hook);
		addToMappings(checkType, hook);
		logHookAdded(hook);
		return hookId;
	}

	/**
	 * Register a hook for several individual checks ids (all, group, or an individual checks).
	 * @param checkTypes Array of check types to register the hook for. If you pass null this hook will be registered for all checks.
	 * @param hook
	 * @return
	 */
	public static Integer addHook(CheckType[] checkTypes, NCPHook hook){
		if (checkTypes == null) checkTypes = new CheckType[]{CheckType.ALL};
		Integer hookId = getId(hook);
		for (CheckType checkType : checkTypes){
			addToMappings(checkType, hook);
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
	public static Collection<NCPHook> removeHook(String hookName){
		Collection<NCPHook> hooks = getHooksByName(hookName);
		if (hooks.isEmpty()) return null;
		removeHooks(hooks);
		return hooks;
	}
	
	/**
	 * Remove a collection of hooks.
	 * @param hooks
	 * @return A set of the removed hooks ids.
	 */
	public static Set<Integer> removeHooks( Collection<NCPHook> hooks){
		Set<Integer> ids = new HashSet<Integer>();
		for (NCPHook hook : hooks){
			Integer id = removeHook(hook);
			if (id != null) ids.add(id);
		}
		return ids;
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
	public static Collection<NCPHook> getHooksByName(String hookName){
		List<NCPHook> hooks = new LinkedList<NCPHook>();
		for (Integer refId : allHooks.keySet()){
			NCPHook hook = allHooks.get(refId);
			if (hook.getHookName().equals(hookName) && !hooks.contains(hook)) hooks.add(hook);
		}
		return hooks;
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

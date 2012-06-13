package me.asofold.bukkit.cncp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import me.asofold.bukkit.cncp.config.compatlayer.CompatConfig;
import me.asofold.bukkit.cncp.config.compatlayer.NewConfig;
import me.asofold.bukkit.cncp.hooks.Hook;
import me.asofold.bukkit.cncp.hooks.mcmmo.HookmcMMO;
import me.asofold.bukkit.cncp.setttings.GroupHooks;
import me.asofold.bukkit.cncp.setttings.Settings;
import me.asofold.bukkit.cncp.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;

/**
 * Quick attempt to provide compatibility to NoCheatPlus for some other plugins that change the vanilla game mechanichs, for instance by fast block breaking. 
 * @author mc_dev
 *
 */
public class CompatNoCheatPlus extends JavaPlugin implements Listener {
	
	/**
	 * Storage of hooks that are called for all events.
	 */
	private static final ArrayList<Hook> hooksAll = new ArrayList<Hook>(10);
	
	/**
	 * Storage of hooks that are called for certain groups and checks.
	 */
	private static final Map<String, GroupHooks> hooksGroups = new HashMap<String, GroupHooks>(20);
	
	private final Settings settings = new Settings();
	
	/**
	 * Flag if plugin is enabled.
	 */
	private static boolean enabled = false;
	
	/**
	 * Experimental: static method to enable this plugin, only enables if it is not already enabled.
	 * @return
	 */
	public static boolean enableCncp(){
		if (enabled) return true;
		return enablePlugin("CompatNoCheatPlus");
	}
	
	/**
	 * Static method to enable a plugin (might also be useful for hooks).
	 * @param plgName
	 * @return
	 */
	public static boolean enablePlugin(String plgName) {
		PluginManager pm = Bukkit.getPluginManager();
		Plugin plugin = pm.getPlugin(plgName);
		if (plugin == null) return false;
		if (pm.isPluginEnabled(plugin)) return true;
		pm.enablePlugin(plugin);
		return true;
	}
	
	/**
	 * Static method to disable a plugin (might also be useful for hooks).
	 * @param plgName
	 * @return
	 */
	public static boolean disablePlugin(String plgName){
		PluginManager pm = Bukkit.getPluginManager();
		Plugin plugin = pm.getPlugin(plgName);
		if (plugin == null) return false;
		if (!pm.isPluginEnabled(plugin)) return true;
		pm.disablePlugin(plugin);
		return true;
	}

	/**
	 * API to add a hook.
	 * @param hook
	 * @return
	 */
	public static boolean addHook(Hook hook){
		if (!enabled) return false;
		Listener[] listeners = hook.getListeners();
		if (listeners != null){
			// attempt to register events:
			PluginManager pm = Bukkit.getPluginManager();
			Plugin plg = pm.getPlugin("CompatNoCheatPlus");
			if (plg == null) return false;
			for (Listener listener : listeners) {
				pm.registerEvents(listener, plg);
			}
		}
		String[][] specs = hook.getCheckSpec();
		if (specs == null) hooksAll.add(hook);
		else{
			for (String[] spec : specs){
				String group = spec[0].trim().toLowerCase();
				GroupHooks gh = hooksGroups.get(group);
				if (gh == null){
					gh = new GroupHooks();
					hooksGroups.put(group, gh);
				}
				if (spec.length == 1) gh.all.add(hook);
				else{
					for (int i = 1; i < spec.length; i++){
						String check = spec[i].trim().toLowerCase();
						ArrayList<Hook> hooks = gh.byCheck.get(check);
						if (hooks == null){
							hooks = new ArrayList<Hook>(10);
							gh.byCheck.put(check, hooks);
						}
						hooks.add(hook);
					}
				}
			}
		}
		System.out.println("[cncp] Added hook: "+hook.getHookName() + " / " + hook.getHookVersion());
		return true;
	}
	

	public void clearHooks() {
		hooksAll.clear();
		hooksGroups.clear();
	}
	
	/**
	 * Add standard hooks if available.
	 */
	private void addAvailableHooks() {
		try{
			addHook(new HookmcMMO());
		}
		catch (Throwable t){}
	}
	
	@Override
	public void onEnable() {
		// cleanup
		clearHooks();
		// Settings:
		settings.clear();
		reloadSettings();
		// Register own listener:
		final PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		super.onEnable();
		enabled = true;
		// Add  Hooks:
		addAvailableHooks();
	}

	public boolean reloadSettings() {
		final Set<String> oldForceEnableLater = new HashSet<String>();
		oldForceEnableLater.addAll(settings.forceEnableLater);
		// Read and apply config to settings:
		File file = new File(getDataFolder() , "cncp.yml");
		CompatConfig cfg = new NewConfig(file);
		cfg.load();
		if (Settings.addDefaults(cfg)) cfg.save();
		settings.fromConfig(cfg);
		// Re-enable plugins that were not yet on the list:
		Server server = getServer();
		BukkitScheduler sched = server.getScheduler();
		for (String plgName : settings.forceEnableLater){
			if (!oldForceEnableLater.remove(plgName)) oldForceEnableLater.add(plgName);
		}
		if (!oldForceEnableLater.isEmpty()){
			System.out.println("[cncp] Schedule task to re-enable plugins later...");
			sched.scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					// (Later maybe re-enabling this plugin could be added.)
					// TODO: log levels !
					for (String plgName : oldForceEnableLater){
						try{
							if (disablePlugin(plgName)){
								if (enablePlugin(plgName)) System.out.println("[cncp] Re-enabled plugin: " + plgName);
								else System.out.println("[cncp] Could not re-enable plugin: "+plgName);
							}
							else{
								System.out.println("[cncp] Could not disable plugin (already disabled?): "+plgName);
							}
						}
						catch(Throwable t){
							// TODO: maybe log ?
						}
					}
				}
			}); 
		}
		return true;
	}

	@Override
	public void onDisable() {
		enabled = false;
		clearHooks();
		super.onDisable();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
	final void onCheckFail(final CheckEvent event){
		// Check hooks, most specific first:
		final Check check = event.getCheck();
		final String gn = check.getGroup();
		final String cn = check.getName();
		final GroupHooks gh = hooksGroups.get(gn.trim().toLowerCase());
		if (gh != null){
			final ArrayList<Hook> hooks = gh.byCheck.get(cn.trim().toLowerCase());
			if (hooks != null) applyHooks(gn, cn, event, hooks);
			if (event.isCancelled()) return;
			if (!gh.all.isEmpty()) applyHooks(gn, cn, event, gh.all);
		}
		if (event.isCancelled()) return;
		if (!hooksAll.isEmpty()) applyHooks(gn, cn, event, hooksAll);
	}

	private final void applyHooks(final String group, final String check, final CheckEvent event, final ArrayList<Hook> hooks) {
		for (int i = 0; i < hooks.size(); i++){
			if (event.isCancelled()) return;
			final Hook hook = hooks.get(i);
			try{
				hook.processEvent(group, check, event);
			}
			catch (final Throwable t){
				final Logger logger = getServer().getLogger();
				logger.warning("[cncp][" + group + "/" + check + "] Unexpected exception on for hook ("+hook.getHookName()+" / "+hook.getHookVersion()+"):");
				// TODO: maybe add more info about the CheckEvent ?
				logger.warning(Utils.toString(t));
			}
		}
	}

}

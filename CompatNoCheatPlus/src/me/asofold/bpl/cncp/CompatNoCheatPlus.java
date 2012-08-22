package me.asofold.bpl.cncp;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.NewConfig;
import me.asofold.bpl.cncp.hooks.Hook;
import me.asofold.bpl.cncp.hooks.generic.HookPlayerClass;
import me.asofold.bpl.cncp.hooks.generic.HookSetSpeed;
import me.asofold.bpl.cncp.setttings.Settings;
import me.asofold.bpl.cncp.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import fr.neatmonster.nocheatplus.hooks.NCPHook;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;

/**
 * Quick attempt to provide compatibility to NoCheatPlus (by NeatMonster) for some other plugins that change the vanilla game mechanichs, for instance by fast block breaking. 
 * @author mc_dev
 *
 */
public class CompatNoCheatPlus extends JavaPlugin implements Listener {
	
	
	private final Settings settings = new Settings();
	
	private static final Set<Hook> registeredHooks = new HashSet<Hook>();
	
	private final HookPlayerClass hookPlayerClass = new HookPlayerClass();
	
	private HookSetSpeed hookSetSpeed = null;
	
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
	 * API to add a hook. Adds the hook AND registers listeners if enabled. Also respects the configuration for preventing hooks.<br>
	 * If you want to not register the listeners use NCPHookManager.
	 * @param hook
	 * @return
	 */
	public static boolean addHook(Hook hook){
		if (Settings.preventAddHooks.contains(hook.getHookName())){
			Bukkit.getLogger().info("[cncp] Prevented adding hook: "+hook.getHookName() + " / " + hook.getHookVersion());
			return false;
		}
		if (enabled) registerListeners(hook);
		boolean added = checkAddNCPHook(hook); // Add if plugin is present, otherwise queue for adding.
		Bukkit.getLogger().info("[cncp] Registered hook"+(added?"":"(NCPHook might get added later)")+": "+hook.getHookName() + " / " + hook.getHookVersion());
		return true;
	}
	
	/**
	 * If already added to NCP
	 * @param hook
	 * @return
	 */
	private static boolean checkAddNCPHook(Hook hook) {
		registeredHooks.add(hook);
		PluginManager pm =  Bukkit.getPluginManager();
		Plugin plugin = pm.getPlugin("NoCheatPlus");
		if (plugin == null || !pm.isPluginEnabled(plugin))
			return false;
		NCPHook ncpHook = hook.getNCPHook();
		if (ncpHook != null)
			NCPHookManager.addHook(hook.getCheckTypes(), ncpHook);
		return true;
	}

	/**
	 * Conveniently register the listeners, do not use if you add/added the hook with addHook. 
	 * @param hook
	 * @return
	 */
	public  static boolean registerListeners(Hook hook) {
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
		return true;
	}

	/**
	 * Add standard hooks if available.
	 */
	private void addAvailableHooks() {
		// Set speed
		if (settings.setSpeedEnabled){
			try{
				hookSetSpeed = new me.asofold.bpl.cncp.hooks.generic.HookSetSpeed();
				hookSetSpeed.setFlySpeed(settings.flySpeed);
				hookSetSpeed.setWalkSpeed(settings.walkSpeed);
//				hookSetSpeed.setAllowFlightPerm(settings.allowFlightPerm);
				hookSetSpeed.init();
				addHook(hookSetSpeed);
			}
			catch (Throwable t){}
		}
		// Citizens 2
		try{
			addHook(new me.asofold.bpl.cncp.hooks.citizens2.HookCitizens2());
		}
		catch (Throwable t){}
		// Player class (NPCs, Citizens 1)
		if (settings.playerClassEnabled)
			addHook(hookPlayerClass);
		// mcMMO
		try{
			addHook(new me.asofold.bpl.cncp.hooks.mcmmo.HookmcMMO());
		}
		catch (Throwable t){}
	}
	
	@Override
	public void onEnable() {
		enabled = false; // make sure
		// (no cleanup)
		
		// Settings:
		settings.clear();
		loadSettings();
		// Register own listener:
		final PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		super.onEnable();
		
		// Add  Hooks:
		addAvailableHooks(); // add before enable is set to not yet register listeners.
		enabled = true;
		
		// register all listeners:
		for (Hook hook : registeredHooks){
			registerListeners(hook);
		}
	}

	public boolean loadSettings() {
		final Set<String> oldForceEnableLater = new LinkedHashSet<String>();
		oldForceEnableLater.addAll(settings.forceEnableLater);
		// Read and apply config to settings:
		File file = new File(getDataFolder() , "cncp.yml");
		CompatConfig cfg = new NewConfig(file);
		cfg.load();
		boolean changed = false;
		if (cfg.getInt("configversion", 0) == 0){
			cfg.remove("plugins");
			changed = true;
		}
		if (Settings.addDefaults(cfg)) changed = true;
		if (changed) cfg.save();
		settings.fromConfig(cfg);
		// Set hookPlayerClass properties
		hookPlayerClass.setClassNames(settings.exemptPlayerClassNames);
		hookPlayerClass.setExemptAll(settings.exemptAllPlayerClassNames);
		hookPlayerClass.setPlayerClassName(settings.playerClassName);
		hookPlayerClass.setCheckSuperClass(settings.exemptSuperClass);
		// Set hookSetSpeed properties (for future purposes):
		if (hookSetSpeed != null){
			hookSetSpeed.setFlySpeed(settings.flySpeed);
			hookSetSpeed.setWalkSpeed(settings.walkSpeed);
//			hookSetSpeed.setAllowFlightPerm(settings.allowFlightPerm);
		}
		// Re-enable plugins that were not yet on the list:
		Server server = getServer();
		Logger logger = server.getLogger();
		for (String plgName : settings.loadPlugins){
			try{
				if (CompatNoCheatPlus.enablePlugin(plgName)){
					System.out.println("[cncp] Ensured that the following plugin is enabled: " + plgName);
				}
			}
			catch (Throwable t){
				logger.severe("[cncp] Failed to enable the plugin: " + plgName);
				logger.severe(Utils.toString(t));
			}
		}
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
		// remove all registered cncp hooks:
		unregisterHooks();
		super.onDisable();
	}
	
	private int unregisterHooks() {
		int n = 0;
		for (Hook hook : registeredHooks){
			NCPHook ncpHook = hook.getNCPHook();
			if (ncpHook != null){
				NCPHookManager.removeHook(ncpHook);
				n ++;
			}
		}
		getLogger().info("[cncp] Removed "+n+" registered hooks from NoCheatPlus.");
		return n;
	}
	
	private int registerHooks() {
		int n = 0;
		for (Hook hook : registeredHooks){
			// TODO: try catch
			NCPHook ncpHook = hook.getNCPHook();
			if (ncpHook == null) continue;
			NCPHookManager.addHook(hook.getCheckTypes(), ncpHook);
			n ++;
		}
		getLogger().info("[cncp] Added "+n+" registered hooks to NoCheatPlus.");
		return n;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	void onPluginEnable(PluginEnableEvent event){
		Plugin plugin = event.getPlugin();
		if (!plugin.getName().equals("NoCheatPlus")) return;
		if (registeredHooks.isEmpty()) return;
		registerHooks();
	}

	@EventHandler(priority = EventPriority.NORMAL)
	void onPluginDisable(PluginDisableEvent event){
		Plugin plugin = event.getPlugin();
		if (!plugin.getName().equals("NoCheatPlus")) return;
		if (registeredHooks.isEmpty()) return;
		unregisterHooks();
	}

}

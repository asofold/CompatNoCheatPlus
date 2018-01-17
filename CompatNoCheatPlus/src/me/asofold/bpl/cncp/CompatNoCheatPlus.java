package me.asofold.bpl.cncp;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.components.registry.feature.IDisableListener;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder;
import fr.neatmonster.nocheatplus.hooks.NCPHook;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import me.asofold.bpl.cncp.config.Settings;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.NewConfig;
import me.asofold.bpl.cncp.hooks.Hook;
import me.asofold.bpl.cncp.hooks.generic.ConfigurableHook;
import me.asofold.bpl.cncp.hooks.generic.HookBlockBreak;
import me.asofold.bpl.cncp.hooks.generic.HookBlockPlace;
import me.asofold.bpl.cncp.hooks.generic.HookEntityDamageByEntity;
import me.asofold.bpl.cncp.hooks.generic.HookInstaBreak;
import me.asofold.bpl.cncp.hooks.generic.HookPlayerClass;
import me.asofold.bpl.cncp.hooks.generic.HookPlayerInteract;
import me.asofold.bpl.cncp.utils.TickTask2;

/**
 * Quick attempt to provide compatibility to NoCheatPlus (by NeatMonster) for
 * some other plugins that change the vanilla game mechanichs, for instance by
 * fast block breaking.
 * 
 * @author asofold
 *
 */
public class CompatNoCheatPlus extends JavaPlugin {

    //TODO: Adjust, once NCP has order everywhere (generic + ncp-specific?).

    public static final String tagEarlyFeature = "cncp.feature.early";
    public static final String beforeTagEarlyFeature = ".*nocheatplus.*|.*NoCheatPlus.*";

    public static final String tagLateFeature = "cncp.feature.late";
    public static final String afterTagLateFeature = "cncp.system.early.*|.*nocheatplus.*|.*NoCheatPlus.*)";

    public static final String tagEarlySystem = "cncp.system.early";
    public static final String beforeTagEarlySystem = beforeTagEarlyFeature; // ...

    public static final RegistrationOrder defaultOrderSystemEarly = new RegistrationOrder(
            tagEarlySystem, 
            beforeTagEarlySystem,
            null);

    public static final RegistrationOrder defaultOrderFeatureEarly = new RegistrationOrder(
            tagEarlyFeature, 
            beforeTagEarlyFeature,
            "cncp.system.early.*");

    public static final RegistrationOrder defaultOrderFeatureLate = new RegistrationOrder(
            tagLateFeature, 
            null, 
            afterTagLateFeature);

    private final Settings settings = new Settings();

    /** Hooks registered with cncp */
    private static final Set<Hook> registeredHooks = new HashSet<Hook>();

    private final List<Hook> builtinHooks = new LinkedList<Hook>();

    /**
     * Flag if plugin is enabled.
     */
    private static boolean enabled = false;

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
     * Get the plugin instance.
     * @return
     */
    public static CompatNoCheatPlus getInstance(){
        return CompatNoCheatPlus.getPlugin(CompatNoCheatPlus.class);
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
        registeredHooks.add(hook);
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
        if (!enabled) {
            return false;
        }
        Listener[] listeners = hook.getListeners();
        if (listeners != null){
            // attempt to register events:
            Plugin plg = CompatNoCheatPlus.getPlugin(CompatNoCheatPlus.class);
            if (plg == null) {
                return false;
            }
            for (Listener listener : listeners) {
                NCPAPIProvider.getNoCheatPlusAPI().getEventRegistry().register(listener, 
                        defaultOrderFeatureEarly, plg);
            }
        }
        return true;
    }

    // ----

    /**
     * Called before loading settings, adds available hooks into a list, so they will be able to read config.
     */
    private void setupBuiltinHooks() {
        builtinHooks.clear();
        // Might-fail hooks:
        // Set speed
        try{
            builtinHooks.add(new me.asofold.bpl.cncp.hooks.generic.HookSetSpeed());
        }
        catch (Throwable t){}
        // Citizens 2
        try{
            builtinHooks.add(new me.asofold.bpl.cncp.hooks.citizens2.HookCitizens2());
        }
        catch (Throwable t){}
        // mcMMO
        try{
            builtinHooks.add(new me.asofold.bpl.cncp.hooks.mcmmo.HookmcMMO());
        }
        catch (Throwable t){}
        //        // MagicSpells
        //        try{
        //            builtinHooks.add(new me.asofold.bpl.cncp.hooks.magicspells.HookMagicSpells());
        //        }
        //        catch (Throwable t){}
        // Simple generic hooks
        for (Hook hook : new Hook[]{
                new HookPlayerClass(),
                new HookBlockBreak(),
                new HookBlockPlace(),
                new HookInstaBreak(),
                new HookEntityDamageByEntity(),
                new HookPlayerInteract()
        }){
            builtinHooks.add(hook);
        }
    }

    /**
     * Add standard hooks if enabled.
     */
    private void addAvailableHooks() {

        // Add built in hooks:
        for (Hook hook : builtinHooks){
            boolean add = true;
            if (hook instanceof ConfigurableHook){
                if (!((ConfigurableHook)hook).isEnabled()) add = false;
            }
            if (add){
                try{
                    addHook(hook);
                }
                catch (Throwable t){}
            }
        }
    }

    @Override
    public void onEnable() {
        enabled = false; // make sure
        // (no cleanup)

        // Settings:
        settings.clear();
        setupBuiltinHooks();
        loadSettings();
        // Register own listener:
        //NCPAPIProvider.getNoCheatPlusAPI().getEventRegistry().register(this, defaultOrderSystemEarly, this);
        super.onEnable();

        // Add  Hooks:
        addAvailableHooks(); // add before enable is set to not yet register listeners.
        enabled = true;

        // register all listeners:
        for (Hook hook : registeredHooks){
            registerListeners(hook);
        }

        // Register to remove hooks when NCP is disabling.
        NCPAPIProvider.getNoCheatPlusAPI().addComponent(new IDisableListener(){
            @Override
            public void onDisable() {
                // Remove all registered cncp hooks:
                unregisterNCPHooks();
            }
        });
        if (!registeredHooks.isEmpty()) {
            registerHooks();
        }

        // Start ticktask 2
        // TODO: Replace by using TickTask ? Needs order (depend is used now)?
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new TickTask2(), 1, 1);

        // Finished.
        getLogger().info(getDescription().getFullName() + " is enabled. Some hooks might get registered with NoCheatPlus later on.");
    }

    public boolean loadSettings() {
        // Read and apply config to settings:
        File file = new File(getDataFolder() , "cncp.yml");
        CompatConfig cfg = new NewConfig(file);
        cfg.load();
        boolean changed = false;
        // General settings:
        if (Settings.addDefaults(cfg)) {
            changed = true;
        }
        settings.fromConfig(cfg);
        // Settings for builtin hooks:
        for (Hook hook : builtinHooks){
            if (hook instanceof ConfigurableHook){
                try{
                    ConfigurableHook cfgHook = (ConfigurableHook) hook;
                    if (cfgHook.updateConfig(cfg, "hooks.")) changed = true;
                    cfgHook.applyConfig(cfg, "hooks.");
                }
                catch (Throwable t){
                    getLogger().severe("[cncp] Hook failed to process config ("+hook.getHookName() +" / " + hook.getHookVersion()+"): " + t.getClass().getSimpleName() + ": "+t.getMessage());
                    t.printStackTrace();
                }
            }
        }
        // save back config if changed:
        if (changed) {
            cfg.save();
        }

        return true;
    }

    @Override
    public void onDisable() {
        unregisterNCPHooks(); // Just in case.
        enabled = false;
        super.onDisable();
    }

    protected int unregisterNCPHooks() {
        // TODO: Clear list here !? Currently done externally...
        int n = 0;
        for (Hook hook : registeredHooks) {
            String hookDescr = null;
            try {
                NCPHook ncpHook = hook.getNCPHook();
                if (ncpHook != null){
                    hookDescr = ncpHook.getHookName() + ": " + ncpHook.getHookVersion();
                    NCPHookManager.removeHook(ncpHook);
                    n ++;
                }
            } catch (Throwable e)
            {
                if (hookDescr != null) {
                    // Some error with removing a hook.
                    getLogger().log(Level.WARNING, "Failed to unregister hook: " + hookDescr, e);
                }
            }
        }
        getLogger().info("[cncp] Removed "+n+" registered hooks from NoCheatPlus.");
        registeredHooks.clear();
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

    /* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permission has already been checked.
        sendInfo(sender);
        return true;
    }

    /**
     * Send general version and hooks info.
     * @param sender
     */
    private void sendInfo(CommandSender sender) {
        List<String> infos = new LinkedList<String>();
        infos.add("---- Version infomation ----");
        // Server
        infos.add("#### Server ####");
        infos.add(getServer().getVersion());
        // Core plugins (NCP + cncp)
        infos.add("#### Core plugins ####");
        infos.add(getDescription().getFullName());
        String temp = getOtherVersion("NoCheatPlus");
        infos.add(temp.isEmpty() ? "NoCheatPlus is missing or not yet enabled." : temp);
        infos.add("#### Typical plugin dependencies ####");
        for (String pluginName : new String[]{
                "mcMMO", "Citizens", "MachinaCraft", "MagicSpells", 
                // TODO: extend
        }){
            temp = getOtherVersion(pluginName);
            if (!temp.isEmpty()) infos.add(temp);
        }
        // Hooks
        infos.add("#### Registered hooks (cncp) ###");
        for (final Hook hook : registeredHooks){
            temp = hook.getHookName() + ": " + hook.getHookVersion();
            if (hook instanceof ConfigurableHook){
                temp += ((ConfigurableHook) hook).isEnabled() ? " (enabled)" : " (disabled)";
            }
            infos.add(temp);
        }
        // TODO: Registered hooks (ncp) ?
        infos.add("#### Registered hooks (ncp) ####");
        for (final NCPHook hook : NCPHookManager.getAllHooks()){
            infos.add(hook.getHookName() + ": " + hook.getHookVersion());
        }
        final String[] a = new String[infos.size()];
        infos.toArray(a);
        sender.sendMessage(a);
    }

    /**
     * 
     * @param pluginName Empty string or "name: version".
     */
    private String getOtherVersion(String pluginName){
        Plugin plg = getServer().getPluginManager().getPlugin(pluginName);
        if (plg == null) return "";
        PluginDescriptionFile pdf = plg.getDescription();
        return pdf.getFullName();
    }

}

package me.asofold.bpl.cncp.hooks.GravityTubes;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import fr.neatmonster.nocheatplus.hooks.NCPHook;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bpl.cncp.hooks.AbstractHook;
import me.asofold.bpl.cncp.hooks.generic.ConfigurableHook;

public class HookGravityTubes extends AbstractHook implements Listener, ConfigurableHook {

    public static interface HookFacade{
        public void onMoveLowest(PlayerMoveEvent event);
    }

    protected HookFacade ncpHook = null;

    protected boolean enabled = true;

    protected String configPrefix = "gravitytubes.";

    public HookGravityTubes(){
        assertPluginPresent("GravityTubes");
    }

    @Override
    public String getHookName() {
        return "GravityTubes(default)";
    }

    @Override
    public String getHookVersion() {
        return "1.0";
    }

    @Override
    public Listener[] getListeners() {
        return new Listener[]{
            this
        };
    }

    @Override
    public NCPHook getNCPHook() {
        if (ncpHook == null){
            ncpHook = new HookFacadeImpl();
        }
        return (NCPHook) ncpHook;
    }

    @EventHandler(priority=EventPriority.LOWEST)
    //@RegisterMethodWithOrder(tag = CompatNoCheatPlus.tagEarlyFeature, beforeTag = CompatNoCheatPlus.beforeTagEarlyFeature)
    public final void onMoveLowest(final PlayerMoveEvent event){
        ncpHook.onMoveLowest(event);
    }

    @Override
    public void applyConfig(CompatConfig cfg, String prefix) {
        enabled = cfg.getBoolean(prefix + configPrefix + "enabled",  true);
    }

    @Override
    public boolean updateConfig(CompatConfig cfg, String prefix) {
        CompatConfig defaults = CompatConfigFactory.getConfig(null);
        defaults.set(prefix + configPrefix + "enabled",  true);
        return ConfigUtil.forceDefaults(defaults, cfg);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

package me.asofold.bpl.cncp.hooks.CMI;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import com.Zrips.CMI.CMI;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import fr.neatmonster.nocheatplus.hooks.NCPHook;
import me.asofold.bpl.cncp.CompatNoCheatPlus;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bpl.cncp.hooks.AbstractHook;
import me.asofold.bpl.cncp.hooks.generic.ConfigurableHook;
import me.asofold.bpl.cncp.hooks.generic.ExemptionManager;
import me.asofold.bpl.cncp.utils.TickTask2;

public class HookCMI extends AbstractHook implements Listener, ConfigurableHook {

    protected Object ncpHook = null;

    protected boolean enabled = true;

    protected String configPrefix = "cmi.";

    protected final ExemptionManager exMan = new ExemptionManager();
    
    protected final CheckType[] exemptBreakMany = new CheckType[]{
            CheckType.BLOCKBREAK, CheckType.COMBINED_IMPROBABLE,
    };

    protected final CheckType[] exemptPlaceMany = new CheckType[]{
            CheckType.BLOCKPLACE, CheckType.COMBINED_IMPROBABLE,
    };
    
    public HookCMI(){
        assertPluginPresent("CMI");
    }

    @Override
    public String getHookName() {
        return "CMI(default)";
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
            ncpHook = new NCPHook() {
                @Override
                public String getHookName() {
                    return "CMI(cncp)";
                }

                @Override
                public String getHookVersion() {
                    return "1.0";
                }

                @Override
                public boolean onCheckFailure(CheckType checkType, Player player, IViolationInfo info) {
                    return false;
                }
            };
        }
        return (NCPHook) ncpHook;
    }

    @EventHandler(priority=EventPriority.MONITOR)
    //@RegisterMethodWithOrder(tag = CompatNoCheatPlus.tagLateFeature, afterTag = CompatNoCheatPlus.afterTagLateFeature)
    public final void onMirrorBreakMonitor(final BlockBreakEvent event){
        removeExemption(event.getPlayer(), exemptBreakMany);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    //@RegisterMethodWithOrder(tag = CompatNoCheatPlus.tagEarlyFeature, beforeTag = CompatNoCheatPlus.beforeTagEarlyFeature)
    public final void onMirrorBreakLowest(final BlockBreakEvent event){
        if (CMI.getInstance().getMirrorManager().isMirroring(event.getPlayer())) {
            addExemption(event.getPlayer(), exemptBreakMany);
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    //@RegisterMethodWithOrder(tag = CompatNoCheatPlus.tagLateFeature, afterTag = CompatNoCheatPlus.afterTagLateFeature)
    public final void onMirrorPlaceMonitor(final BlockPlaceEvent event){
        removeExemption(event.getPlayer(), exemptPlaceMany);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    //@RegisterMethodWithOrder(tag = CompatNoCheatPlus.tagEarlyFeature, beforeTag = CompatNoCheatPlus.beforeTagEarlyFeature)
    public final void onMirrorPlaceLowest(final BlockPlaceEvent event){
        if (CMI.getInstance().getMirrorManager().isMirroring(event.getPlayer())) {
            addExemption(event.getPlayer(), exemptPlaceMany);
        }
    }

    public void addExemption(final Player player, final CheckType[] types){
        for (final CheckType type : types){
            exMan.addExemption(player, type);
            TickTask2.addUnexemptions(player, types);
        }
    }

    public void removeExemption(final Player player, final CheckType[] types){
        for (final CheckType type : types){
            exMan.removeExemption(player, type);
        }
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

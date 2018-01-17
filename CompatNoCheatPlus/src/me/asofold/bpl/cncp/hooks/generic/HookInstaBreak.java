package me.asofold.bpl.cncp.hooks.generic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import me.asofold.bpl.cncp.CompatNoCheatPlus;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bpl.cncp.hooks.AbstractHook;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterMethodWithOrder;
import fr.neatmonster.nocheatplus.utilities.TickTask;

public class HookInstaBreak extends AbstractHook implements ConfigurableHook, Listener {

    public static interface InstaExemption{
        public void addExemptNext(CheckType[] types);
        public Set<CheckType> getExemptNext();
    }

    public static class StackEntry{
        public final CheckType[] checkTypes;
        public final int tick;
        public final Player player;
        public boolean used = false;
        public StackEntry(final Player player , final CheckType[] checkTypes){
            this.player = player;
            this.checkTypes = checkTypes;
            tick = TickTask.getTick();
        }
        public boolean isOutdated(final int tick){
            return tick != this.tick;
        }
    }

    protected static InstaExemption runtime = null;

    public static void addExemptNext(final CheckType[] types){
        runtime.addExemptNext(types);
    }

    protected final ExemptionManager exMan = new ExemptionManager();

    protected boolean enabled = true;

    protected final List<StackEntry> stack = new LinkedList<StackEntry>();

    @Override
    public String getHookName() {
        return "InstaBreak(default)";
    }

    @Override
    public String getHookVersion() {
        return "1.0";
    }

    @Override
    public void applyConfig(CompatConfig cfg, String prefix) {
        enabled = cfg.getBoolean(prefix + "insta-break.enabled", true);
    }

    @Override
    public boolean updateConfig(CompatConfig cfg, String prefix) {
        CompatConfig defaults = CompatConfigFactory.getConfig(null);
        defaults.set(prefix + "insta-break.enabled", true);
        return ConfigUtil.forceDefaults(defaults, cfg);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public CheckType[] getCheckTypes() {
        return null;
    }

    @Override
    public Listener[] getListeners() {
        runtime = new InstaExemption() {
            protected final Set<CheckType> types = new HashSet<CheckType>();
            @Override
            public final void addExemptNext(final CheckType[] types) {
                for (int i = 0; i < types.length; i++){
                    this.types.add(types[i]);
                }
            }
            @Override
            public Set<CheckType> getExemptNext() {
                return types;
            }
        };
        return new Listener[]{this};
    }

    protected CheckType[] fetchTypes(){
        final Set<CheckType> types = runtime.getExemptNext();
        final CheckType[] a = new CheckType[types.size()];
        if (!types.isEmpty()) types.toArray(a);
        types.clear();
        return a;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)	
    @RegisterMethodWithOrder(tag = CompatNoCheatPlus.tagLateFeature, afterTag = CompatNoCheatPlus.afterTagLateFeature)
    public void onBlockDamage(final BlockDamageEvent event){
        checkStack();
        if (!event.isCancelled() && event.getInstaBreak()){
            stack.add(new StackEntry(event.getPlayer(), fetchTypes()));
        }
        else{
            runtime.getExemptNext().clear();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    @RegisterMethodWithOrder(tag = CompatNoCheatPlus.tagEarlyFeature, beforeTag = CompatNoCheatPlus.beforeTagEarlyFeature)
    public void onBlockBreakLowest(final BlockBreakEvent event){
        checkStack();
        if (!stack.isEmpty()){
            final Player player = event.getPlayer();
            final StackEntry entry = stack.get(stack.size() - 1);
            if (player.equals(entry.player)) addExemption(entry);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    @RegisterMethodWithOrder(tag = CompatNoCheatPlus.tagLateFeature, afterTag = CompatNoCheatPlus.afterTagLateFeature)
    public void onBlockBreakMONITOR(final BlockBreakEvent event){
        if (!stack.isEmpty()){
            final Player player = event.getPlayer();
            final StackEntry entry = stack.get(stack.size() - 1);
            if (player.equals(entry.player)) removeExemption(stack.remove(stack.size() - 1));
        }
    }

    public void addExemption(final StackEntry entry){
        entry.used = true;
        for (int i = 0; i < entry.checkTypes.length; i++){
            exMan.addExemption(entry.player, entry.checkTypes[i]);
        }
    }

    public void removeExemption(final StackEntry entry){
        if (!entry.used) return;
        for (int i = 0; i < entry.checkTypes.length; i++){
            exMan.removeExemption(entry.player, entry.checkTypes[i]);
        }
    }

    public void checkStack(){
        if (stack.isEmpty()) return;
        Iterator<StackEntry> it = stack.iterator();
        final int tick = TickTask.getTick();
        while (it.hasNext()){
            final StackEntry entry = it.next();
            if (entry.isOutdated(tick)) it.remove();
            if (entry.used) removeExemption(entry);
        }
    }

}

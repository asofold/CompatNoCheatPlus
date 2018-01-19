package me.asofold.bpl.cncp.hooks.generic;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterMethodWithOrder;
import me.asofold.bpl.cncp.CompatNoCheatPlus;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;

/**
 * Wrap player interact events to exempt players from checks by comparison of event class names.
 * Uses mc_dev's format for exemption based upon class names.
 *
 */
public class HookPlayerInteract extends ClassExemptionHook implements Listener{

    public HookPlayerInteract() {
        super("player-interact.");
        defaultClasses.addAll(Arrays.asList(new String[]{
                // MagicSpells
                "MagicSpellsPlayerInteractEvent"
        }));
    }

    @Override
    public String getHookName() {
        return "Interact(default)";
    }

    @Override
    public String getHookVersion() {
        return "1.0";
    }

    @Override
    public Listener[] getListeners() {
        return new Listener[]{this};
    }

    @Override
    public void applyConfig(CompatConfig cfg, String prefix) {
        super.applyConfig(cfg, prefix);
        if (classes.isEmpty()) enabled = false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @RegisterMethodWithOrder(tag = CompatNoCheatPlus.tagEarlyFeature, beforeTag = CompatNoCheatPlus.beforeTagEarlyFeature)
    public final void onPlayerInteractLowest(final PlayerInteractEvent event){
        checkExempt(event.getPlayer(), event.getClass(), CheckType.BLOCKINTERACT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    @RegisterMethodWithOrder(tag = CompatNoCheatPlus.tagLateFeature, afterTag = CompatNoCheatPlus.afterTagLateFeature)
    public final void onPlayerInteractMonitor(final PlayerInteractEvent event){
        checkUnexempt(event.getPlayer(), event.getClass(), CheckType.BLOCKINTERACT);
    }

}

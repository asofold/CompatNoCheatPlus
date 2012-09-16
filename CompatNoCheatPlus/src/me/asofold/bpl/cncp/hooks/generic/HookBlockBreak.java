package me.asofold.bpl.cncp.hooks.generic;

import java.util.Arrays;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * Wrap block break events to exempt players from checks by comparison of event class names.
 * @author mc_dev
 *
 */
public class HookBlockBreak extends ClassExemptionHook implements Listener {

	public HookBlockBreak() {
		super("block-break.");
		defaultClasses.addAll(Arrays.asList(new String[]{
			// MachinaCraft
			"ArtificialBlockBreakEvent",
			// mcMMO
			"FakeBlockBreakEvent",
		}));
	}

	@Override
	public String getHookName() {
		return "BlockBreak(default)";
	}

	@Override
	public String getHookVersion() {
		return "1.1";
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
	final void onBlockBreakLowest(final BlockBreakEvent event){
		checkExempt(event.getPlayer(), event.getClass(), CheckType.BLOCKBREAK);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	final void onBlockBreakMonitor(final BlockBreakEvent event){
		checkUnexempt(event.getPlayer(), event.getClass(), CheckType.BLOCKBREAK);
	}

}

package me.asofold.bpl.cncp.hooks.generic;

import java.util.Arrays;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * Wrap block place events to exempt players from checks by comparison of event class names.
 * @author mc_dev
 *
 */
public class HookBlockPlace extends ClassExemptionHook implements Listener{

	public HookBlockPlace() {
		super("block-place.");
		defaultClasses.addAll(Arrays.asList(new String[]{
			// MachinaCraft
			"ArtificialBlockPlaceEvent",
			// MagicSpells
			"MagicSpellsBlockPlaceEvent"
		}));
	}

	@Override
	public String getHookName() {
		return "BlockPlace(default)";
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
	final void onBlockPlaceLowest(final BlockPlaceEvent event){
		checkExempt(event.getPlayer(), event.getClass(), CheckType.BLOCKPLACE);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	final void onBlockPlaceMonitor(final BlockPlaceEvent event){
		checkUnexempt(event.getPlayer(), event.getClass(), CheckType.BLOCKPLACE);
	}

}

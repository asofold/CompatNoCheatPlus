package me.asofold.bpl.cncp.hooks.generic;

import java.util.Arrays;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;

public class HookEntityDamageByEntity extends ClassExemptionHook implements
		Listener {

	public HookEntityDamageByEntity() {
		super("entity-damage-by-entity.");
		defaultClasses.addAll(Arrays.asList(new String[] {
		// CrackShot
		"WeaponDamageEntityEvent", }));
	}

	@Override
	public String getHookName() {
		return "EntityDamageByEntity(default)";
	}

	@Override
	public String getHookVersion() {
		return "0.0";
	}

	@Override
	public Listener[] getListeners() {
		return new Listener[] { this };
	}

	@Override
	public void applyConfig(CompatConfig cfg, String prefix) {
		super.applyConfig(cfg, prefix);
		if (classes.isEmpty())
			enabled = false;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	final void onDamageLowest(final EntityDamageByEntityEvent event) {
		final Entity damager = event.getDamager();
		if (damager instanceof Player) {
			checkExempt((Player) damager, event.getClass(), CheckType.FIGHT);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	final void onDamageMonitor(final EntityDamageByEntityEvent event) {
		final Entity damager = event.getDamager();
		if (damager instanceof Player) {
			checkUnexempt((Player) damager, event.getClass(), CheckType.FIGHT);
		}
	}

}

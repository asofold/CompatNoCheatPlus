package me.asofold.bpl.cncp.hooks.generic;

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

public class HookInstaBreak extends AbstractHook implements ConfigurableHook, Listener {

	protected final ExemptionManager exMan = new ExemptionManager();
	
	protected boolean enabled = true;
	
	protected boolean skipNext = false;
	
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
		return new Listener[]{this};
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDamage(final BlockDamageEvent event){
		if (event.getInstaBreak()) skipNext = true;
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onBlockBreakLowest(final BlockBreakEvent event){
		if (skipNext){
			final Player player = event.getPlayer();
			exMan.addExemption(player, CheckType.BLOCKBREAK_FASTBREAK);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onBlockBreakMONITOR(final BlockBreakEvent event){
		if (skipNext){
			final Player player = event.getPlayer();
			exMan.removeExemption(player, CheckType.BLOCKBREAK_FASTBREAK);
		}
		skipNext = false;
	}

}

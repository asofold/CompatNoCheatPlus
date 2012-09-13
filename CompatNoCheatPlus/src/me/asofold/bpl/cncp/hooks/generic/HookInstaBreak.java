package me.asofold.bpl.cncp.hooks.generic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	public static interface InstaExemption{
		public void addExemptNext(CheckType[] types);
		public Set<CheckType> getExemptNext();
	}
	
	protected static InstaExemption runtime = null;
	
	public static void addExemptNext(final CheckType[] types){
		runtime.addExemptNext(types);
	}

	protected final ExemptionManager exMan = new ExemptionManager();
	
	protected boolean enabled = true;
	
	protected List<CheckType[]> stack = new ArrayList<CheckType[]>();
	
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
	public void onBlockDamage(final BlockDamageEvent event){
		if (!event.isCancelled() && event.getInstaBreak()){
			stack.add(fetchTypes());
		}
		else runtime.getExemptNext().clear();
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onBlockBreakLowest(final BlockBreakEvent event){
		if (!stack.isEmpty()){
			final Player player = event.getPlayer();
			exMan.addExemption(player, CheckType.BLOCKBREAK_FASTBREAK);
			for (final CheckType type : stack.get(stack.size() - 1)){
				exMan.addExemption(player, type);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onBlockBreakMONITOR(final BlockBreakEvent event){
		if (!stack.isEmpty()){
			final Player player = event.getPlayer();
			exMan.removeExemption(player, CheckType.BLOCKBREAK_FASTBREAK);
			for (final CheckType type : stack.remove(stack.size() - 1)){
				exMan.removeExemption(player, type);
			}
		}
	}

}

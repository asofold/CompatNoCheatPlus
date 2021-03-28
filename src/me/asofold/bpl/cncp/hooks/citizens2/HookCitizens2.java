package me.asofold.bpl.cncp.hooks.citizens2;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bpl.cncp.hooks.AbstractHook;
import me.asofold.bpl.cncp.hooks.generic.ConfigurableHook;
import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import fr.neatmonster.nocheatplus.hooks.NCPHook;

public class HookCitizens2 extends AbstractHook implements ConfigurableHook{
	
	protected Object ncpHook = null;
	
	protected boolean enabled = true;
	
	protected String configPrefix = "citizens2.";
	
	public HookCitizens2(){
		assertPluginPresent("Citizens");
		CitizensAPI.getNPCRegistry(); // to let it fail for old versions.
	}

	@Override
	public String getHookName() {
		return "Citizens2(default)";
	}

	@Override
	public String getHookVersion() {
		return "2.2";
	}
	
	@Override
	public NCPHook getNCPHook() {
		if (ncpHook == null){
			ncpHook = new NCPHook() {
				@Override
				public final boolean onCheckFailure(final CheckType checkType,  final Player player, IViolationInfo info) {
					return CitizensAPI.getNPCRegistry().isNPC(player);
				}
				
				@Override
				public final String getHookVersion() {
					return "2.0";
				}
				
				@Override
				public final String getHookName() {
					return "Citizens2(cncp)";
				}
			};
		}
		return  (NCPHook) ncpHook;
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

package me.asofold.bpl.cncp.hooks.citizens2;

import me.asofold.bpl.cncp.hooks.AbstractHook;
import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPHook;

public class HookCitizens2 extends AbstractHook {
	
	private Object ncpHook = null;
	
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
		return "2.0.0";
	}
	
	@Override
	public NCPHook getNCPHook() {
		if (ncpHook == null){
			return new NCPHook() {
				@Override
				public boolean onCheckFailure(CheckType checkType, Player player) {
					return CitizensAPI.getNPCRegistry().isNPC(player);
				}
				
				@Override
				public String getHookVersion() {
					return "1.0";
				}
				
				@Override
				public String getHookName() {
					return "Citizens2(cncp)";
				}
			};
		}
		return  (NCPHook) ncpHook;
	}

}

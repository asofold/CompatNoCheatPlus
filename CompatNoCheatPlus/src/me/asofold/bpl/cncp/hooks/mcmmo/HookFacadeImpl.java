package me.asofold.bpl.cncp.hooks.mcmmo;

import java.util.HashMap;
import java.util.Map;

import me.asofold.bpl.cncp.hooks.mcmmo.HookmcMMO.HookFacade;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPHook;

public class HookFacadeImpl implements HookFacade, NCPHook {
	
	private final Map<CheckType, Integer> cancelChecksBlockBreak = new HashMap<CheckType, Integer>();
	private final Map<CheckType, Integer> cancelChecksBlockDamage = new HashMap<CheckType, Integer>();
	private final Map<CheckType, Integer> cancelChecksDamage = new HashMap<CheckType, Integer>();
	
	private String cancel = null;
	private long cancelTicks = 0;
	
	private final Map<CheckType, Integer> cancelChecks = new HashMap<CheckType, Integer>();
	
	public HookFacadeImpl(){
		cancelChecksBlockBreak.put(CheckType.BLOCKBREAK_NOSWING, 1);
		cancelChecksBlockBreak.put(CheckType.BLOCKBREAK_FASTBREAK, 2);
		cancelChecksBlockDamage.put(CheckType.BLOCKBREAK_FASTBREAK, 1);
		
		cancelChecksDamage.put(CheckType.FIGHT_ANGLE, 1);
		cancelChecksDamage.put(CheckType.FIGHT_SPEED, 1);
	}

	@Override
	public String getHookName() {
		return "mcMMO(cncp)";
	}

	@Override
	public String getHookVersion() {
		return "1.0.0";
	}

	@Override
	public final boolean onCheckFailure(CheckType checkType, final Player player) {
//		System.out.println("[cncp] Handle event: " + event.getEventName());
		if (cancel == null){
//			System.out.println("[cncp] Return on cancel == null: "+event.getPlayer().getName());
			return false;
		}
		
		final String name = player.getName();
		if (cancel.equals(name)){
			
			if (player.getTicksLived() != cancelTicks){
//				System.out.println("[cncp] No cancel (ticks/player): "+event.getPlayer().getName());
				cancel = null;
			}
			else{
				final Integer n = cancelChecks.get(checkType);
				if (n == null){
//					System.out.println("[cncp] Expired("+check+"): "+event.getPlayer().getName());
					return false;
				}
				else if (n > 0){
//					System.out.println("Check with n = "+n);
					if (n == 1) cancelChecks.remove(checkType);
					else cancelChecks.put(checkType,  n - 1);
				}
				// else: allow arbitrary numbers
//				System.out.println("[cncp] Cancel: "+event.getPlayer().getName());
				return true;
			}
		}
		return false;
	}
	
	private  final void setPlayer(final Player player, Map<CheckType, Integer> cancelChecks){
		cancel = player.getName();
		cancelTicks = player.getTicksLived();
		this.cancelChecks.clear();
		this.cancelChecks.putAll(cancelChecks);
	}

	@Override
	public final void setPlayerDamage(final Player player) {
		setPlayer(player, cancelChecksDamage);
	}

	@Override
	public final void setPlayerBlockDamage(final Player player) {
		setPlayer(player, cancelChecksBlockDamage);
	}

	@Override
	public final void setPlayerBlockBreak(final Player player) {
		setPlayer(player, cancelChecksBlockBreak);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("CompatNoCheatPlus"), new Runnable() {
			@Override
			public void run() {
				CheckType.removeData(player.getName(), CheckType.BLOCKBREAK_FASTBREAK);
			}
		});
	}



}

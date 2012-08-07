package me.asofold.bpl.cncp.hooks.mcmmo;

import java.util.HashMap;
import java.util.Map;

import me.asofold.bpl.cncp.hooks.AbstractHook;
import me.asofold.bpl.cncp.hooks.ncp.CheckType;
import me.asofold.bpl.cncp.utils.PluginGetter;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.events.fake.FakeBlockBreakEvent;
import com.gmail.nossr50.events.fake.FakeBlockDamageEvent;
import com.gmail.nossr50.events.fake.FakeEntityDamageByEntityEvent;

public final class HookmcMMO extends AbstractHook implements Listener {
	
	private static final Map<CheckType, Integer> cancelChecksBlockBreak = new HashMap<CheckType, Integer>();
	private static final Map<CheckType, Integer> cancelChecksBlockDamage = new HashMap<CheckType, Integer>();
	private static final Map<CheckType, Integer> cancelChecksDamage = new HashMap<CheckType, Integer>();
	static{
		cancelChecksBlockBreak.put(CheckType.BLOCKBREAK_NOSWING, 1);
		cancelChecksBlockBreak.put(CheckType.BLOCKBREAK_FASTBREAK, 2);
		cancelChecksBlockDamage.put(CheckType.BLOCKBREAK_FASTBREAK, 1);
		
		cancelChecksDamage.put(CheckType.FIGHT_ANGLE, 1);
		cancelChecksDamage.put(CheckType.FIGHT_SPEED, 1);
	}
	
	public HookmcMMO(){
		assertPluginPresent("mcMMO");
	}
	
	
	private final PluginGetter<mcMMO> fetch = new PluginGetter<mcMMO>("mcMMO");
	
	private String cancel = null;
	private long cancelTicks = 0;
	
	private final Map<CheckType, Integer> cancelChecks = new HashMap<CheckType, Integer>();
	

	
	@Override
	public String getHookName() {
		return "mcMMO(default)";
	}

	@Override
	public String getHookVersion() {
		return "0.1";
	}

	@Override
	public CheckType[] getCheckTypes() {
		return new CheckType[]{
				CheckType.BLOCKBREAK_FASTBREAK, CheckType.BLOCKBREAK_NOSWING,
				CheckType.FIGHT_ANGLE, CheckType.FIGHT_SPEED,
			};
	}
	
	@Override
	public Listener[] getListeners() {
		fetch.fetchPlugin();
		return new Listener[]{this, fetch};
	}
	
	private  final void setPlayer(final Entity entity, Map<CheckType, Integer> cancelChecks){
		if (entity instanceof Player){
			setPlayer((Player) entity, cancelChecks);
		}
		// no projectiles etc.
	}
	
	private  final void setPlayer(final Player player, Map<CheckType, Integer> cancelChecks){
		cancel = player.getName();
		cancelTicks = player.getTicksLived();
		this.cancelChecks.clear();
		this.cancelChecks.putAll(cancelChecks);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	final void onDamageLowest(final FakeEntityDamageByEntityEvent event){
		// TODO might change with API
		setPlayer(event.getDamager(), cancelChecksDamage);
//		System.out.println("Damage: "+cancel +" / "+event.getEntity().getLocation());
	}
	
//	@EventHandler(priority=EventPriority.MONITOR)
//	final void onDamageMonitor(final FakeEntityDamageByEntityEvent event){
//		cancel =  null;
//	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	final void onBlockBreakLowest(final FakeBlockBreakEvent event){
		setPlayer(event.getPlayer(), cancelChecksBlockBreak);
//		System.out.println("BlockBreak: "+cancel + " / " + event.getBlock());
	}
	
//	@EventHandler(priority=EventPriority.MONITOR)
//	final void onBlockBreakMonitor(final FakeBlockBreakEvent event){
//		cancel = null;
//	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	final void onBlockDamageLowest(final FakeBlockDamageEvent event){
		setPlayer(event.getPlayer(), cancelChecksBlockDamage);
//		System.out.println("BlockDamage: "+cancel + " / insta=" + event.getInstaBreak() + "/" + event.getBlock());
	}
	
//	@EventHandler(priority=EventPriority.MONITOR)
//	final void onBlockDamageMonitor(final FakeBlockDamageEvent event){
//		cancel = null;
//	}
	

	@Override
	public final boolean onCheckFailure(CheckType checkType, final Player player) {
//		System.out.println("[cncp] Handle event: " + event.getEventName());
		if (cancel == null){
//			System.out.println("[cncp] Return on cancel == null: "+event.getPlayer().getName());
			return false;
		}
		
		final String name = player.getName();
		if (cancel.equals(name)){
			
			if (player == null || player.getTicksLived() != cancelTicks){
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

}

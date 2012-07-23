package me.asofold.bukkit.cncp.hooks.mcmmo;

import java.util.HashMap;
import java.util.Map;

import me.asofold.bukkit.cncp.hooks.AbstractHook;
import me.asofold.bukkit.cncp.utils.PluginGetter;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.events.fake.FakeBlockBreakEvent;
import com.gmail.nossr50.events.fake.FakeBlockDamageEvent;
import com.gmail.nossr50.events.fake.FakeEntityDamageByEntityEvent;

import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

public final class HookmcMMO extends AbstractHook implements Listener {
	
	private static final Map<String, Integer> cancelChecksBlockBreak = new HashMap<String, Integer>();
	private static final Map<String, Integer> cancelChecksBlockDamage = new HashMap<String, Integer>();
	private static final Map<String, Integer> cancelChecksDamage = new HashMap<String, Integer>();
	static{
		cancelChecksBlockBreak.put("noswing", 1);
		cancelChecksBlockBreak.put("fastbreak", 2);
		cancelChecksBlockDamage.put("fastbreak", 1);
		
		cancelChecksDamage.put("angle", 1);
		cancelChecksDamage.put("speed", 1);
	}
	
	public HookmcMMO(){
		assertPluginPresent("mcMMO");
	}
	
	
	private final PluginGetter<mcMMO> fetch = new PluginGetter<mcMMO>("mcMMO");
	
	private String cancel = null;
	private long cancelTicks = 0;
	
	private final Map<String, Integer> cancelChecks = new HashMap<String, Integer>();
	

	
	@Override
	public String getHookName() {
		return "mcMMO(default)";
	}

	@Override
	public String getHookVersion() {
		return "0.0";
	}

	@Override
	public String[][] getCheckSpec() {
		return new String[][]{
				{"blockbreak", "fastbreak", "noswing"},
				{"fight", "angle", "speed"},
				};
	}
	
	@Override
	public Listener[] getListeners() {
		fetch.fetchPlugin();
		return new Listener[]{this, fetch};
	}
	
	private  final void setPlayer(final Entity entity, Map<String, Integer> cancelChecks){
		if (entity instanceof Player){
			setPlayer((Player) entity, cancelChecks);
		}
		// no projectiles etc.
	}
	
	private  final void setPlayer(final Player player, Map<String, Integer> cancelChecks){
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
	public void processEvent(final String group, final String check, final CheckEvent event) {
//		System.out.println("[cncp] Handle event: " + event.getEventName());
		if (cancel == null){
//			System.out.println("[cncp] Return on cancel == null: "+event.getPlayer().getName());
			return;
		}
		final NCPPlayer ncpPlayer = event.getPlayer();
		
		final String name = ncpPlayer.getName();
		if (cancel.equals(name)){
			final Player player = ncpPlayer.getBukkitPlayer();
			if (player == null || player.getTicksLived() != cancelTicks){
//				System.out.println("[cncp] No cancel (ticks/player): "+event.getPlayer().getName());
				cancel = null;
			}
			else{
				final Integer n = cancelChecks.get(check);
				if (n == null){
//					System.out.println("[cncp] Expired("+check+"): "+event.getPlayer().getName());
					return;
				}
				else if (n > 0){
//					System.out.println("Check with n = "+n);
					if (n == 1) cancelChecks.remove(check);
					else cancelChecks.put(check,  n - 1);
				}
				// else: allow arbitrary numbers
//				System.out.println("[cncp] Cancel: "+event.getPlayer().getName());
				event.setCancelled(true);
			}
		}
	}

}

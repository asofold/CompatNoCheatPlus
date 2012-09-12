package me.asofold.bpl.cncp.hooks.mcmmo;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bpl.cncp.hooks.AbstractHook;
import me.asofold.bpl.cncp.hooks.generic.ConfigurableHook;
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

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPHook;

public final class HookmcMMO extends AbstractHook implements Listener, ConfigurableHook {
	
	/**
	 * To let the listener access this.
	 * @author mc_dev
	 *
	 */
	public static interface HookFacade{
		public void damageLowest(Player player);
		public void damageMonitor(Player player);
		public void blockDamageLowest(Player player);
		public void blockDamageMonitor(Player player);
		/**
		 * If to cancel the event.
		 * @param player
		 * @return
		 */
		public boolean blockBreakLowest(Player player);
		public void blockBreakMontitor(Player player);
	}
	
	protected HookFacade ncpHook = null;
	
	protected boolean enabled = true;
	
	protected String configPrefix = "mcmmo.";
	

	public HookmcMMO(){
		assertPluginPresent("mcMMO");
	}
	
	
	protected final PluginGetter<mcMMO> fetch = new PluginGetter<mcMMO>("mcMMO");

	protected int blocksPerSecond = 30;
	

	
	@Override
	public String getHookName() {
		return "mcMMO(default)";
	}

	@Override
	public String getHookVersion() {
		return "2.0";
	}

	@Override
	public CheckType[] getCheckTypes() {
		return new CheckType[]{
				CheckType.BLOCKBREAK_FASTBREAK, CheckType.BLOCKBREAK_NOSWING, // old ones
				
//				CheckType.BLOCKBREAK_DIRECTION, CheckType.BLOCKBREAK_FREQUENCY,
//				CheckType.BLOCKBREAK_WRONGBLOCK, CheckType.BLOCKBREAK_REACH,
//				
//				CheckType.FIGHT_ANGLE, CheckType.FIGHT_SPEED, // old ones
//				
//				CheckType.FIGHT_DIRECTION, CheckType.FIGHT_NOSWING,
//				CheckType.FIGHT_REACH, 
			};
	}
	
	@Override
	public Listener[] getListeners() {
		fetch.fetchPlugin();
		return new Listener[]{this, fetch};
	}
	
	@Override
	public NCPHook getNCPHook() {
		if (ncpHook == null){
			ncpHook = new HookFacadeImpl(blocksPerSecond);
		}
		return (NCPHook) ncpHook;
	}
	
	///////////////////////////
	// Damage (fight)
	//////////////////////////
	
	@EventHandler(priority=EventPriority.LOWEST)
	final void onDamageLowest(final FakeEntityDamageByEntityEvent event){
		final Entity entity = event.getDamager();
		if (entity instanceof Player)
			ncpHook.damageLowest((Player) entity);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	final void onDamageMonitor(final FakeEntityDamageByEntityEvent event){
		final Entity entity = event.getDamager();
		if (entity instanceof Player)
			ncpHook.damageMonitor((Player) entity);
	}
	
	///////////////////////////
	// Block damage
	//////////////////////////
	
	@EventHandler(priority=EventPriority.LOWEST)
	final void onBlockDamageLowest(final FakeBlockDamageEvent event){
		ncpHook.blockDamageLowest(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	final void onBlockDamageMonitor(final FakeBlockDamageEvent event){
		ncpHook.blockDamageMonitor(event.getPlayer());
	}
	
	///////////////////////////
	// Block break
	//////////////////////////
	
	@EventHandler(priority=EventPriority.LOWEST)
	final void onBlockBreakLowest(final FakeBlockBreakEvent event){
		if (ncpHook.blockBreakLowest(event.getPlayer())){
			event.setCancelled(true);
//			System.out.println("Cancelled for frequency.");
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	final void onBlockBreakLMonitor(final FakeBlockBreakEvent event){
		ncpHook.blockBreakMontitor(event.getPlayer());
	}
	
	/////////////////////////////////
	// Config
	/////////////////////////////////
	
	@Override
	public void applyConfig(CompatConfig cfg, String prefix) {
		enabled = cfg.getBoolean(prefix + configPrefix + "enabled",  true);
		blocksPerSecond  = cfg.getInt(prefix + configPrefix + "clickspersecond", 30);
	}

	@Override
	public boolean updateConfig(CompatConfig cfg, String prefix) {
		CompatConfig defaults = CompatConfigFactory.getConfig(null);
		defaults.set(prefix + configPrefix + "enabled",  true);
		defaults.set(prefix + configPrefix + "clickspersecond", 30);
		return ConfigUtil.forceDefaults(defaults, cfg);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}

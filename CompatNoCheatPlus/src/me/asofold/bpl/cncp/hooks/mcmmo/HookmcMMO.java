package me.asofold.bpl.cncp.hooks.mcmmo;

import me.asofold.bpl.cncp.hooks.AbstractHook;
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

public final class HookmcMMO extends AbstractHook implements Listener {
	
	/**
	 * To let the listener access this.
	 * @author mc_dev
	 *
	 */
	public static interface HookFacade{
		public void setPlayerDamage(Player player);
		public void setPlayerBlockDamage(Player player);
		public void setPlayerBlockBreak(Player player);
	}
	
	private HookFacade ncpHook = null;
	


	public HookmcMMO(){
		assertPluginPresent("mcMMO");
	}
	
	
	private final PluginGetter<mcMMO> fetch = new PluginGetter<mcMMO>("mcMMO");
	

	
	@Override
	public String getHookName() {
		return "mcMMO(default)";
	}

	@Override
	public String getHookVersion() {
		return "1.2";
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
	
	@EventHandler(priority=EventPriority.LOWEST)
	final void onDamageLowest(final FakeEntityDamageByEntityEvent event){
		// TODO might change with API
		final Entity entity = event.getDamager();
		if (entity instanceof Player)
			ncpHook.setPlayerDamage((Player) entity);
	}
	
	
	@EventHandler(priority=EventPriority.LOWEST)
	final void onBlockBreakLowest(final FakeBlockBreakEvent event){
		ncpHook.setPlayerBlockBreak(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	final void onBlockDamageLowest(final FakeBlockDamageEvent event){
		ncpHook.setPlayerBlockDamage(event.getPlayer());
	}

	@Override
	public NCPHook getNCPHook() {
		if (ncpHook == null){
			ncpHook = new HookFacadeImpl();
		}
		return (NCPHook) ncpHook;
	}
	
	

}

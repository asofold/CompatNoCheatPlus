package me.asofold.bpl.cncp.hooks.generic;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bpl.cncp.hooks.AbstractHook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;

public class HookSetSpeed extends AbstractHook implements Listener, ConfigurableHook{
	
	private float flySpeed = 1.0f;
	
	private float walkSpeed = 1.0f;
	
	private boolean enabled = false;
	
//	private String allowFlightPerm = "cncp.allow-flight";
	
	public HookSetSpeed() throws SecurityException, NoSuchMethodException{
		Player.class.getDeclaredMethod("setFlySpeed", float.class);
	}
	
	public void init(){
		for (final Player player : Bukkit.getOnlinePlayers()){
			setSpeed(player);
		}
	}
	
	@Override
	public String getHookName() {
		return "SetSpeed(default)";
	}

	@Override
	public String getHookVersion() {
		return "2.0";
	}

	@Override
	public CheckType[] getCheckTypes() {
		return new CheckType[0];
	}

	@Override
	public Listener[] getListeners() {
		try{
			// Initialize here, at the end of enable.
			init();
		}
		catch (Throwable t){}
		return new Listener[]{this}	;
	}
	
	public final void setSpeed(final Player player){
//		if (allowFlightPerm.equals("") || player.hasPermission(allowFlightPerm)) player.setAllowFlight(true);
		player.setWalkSpeed(walkSpeed);
		player.setFlySpeed(flySpeed);
	}
	
	final void onPlayerJoin(final PlayerJoinEvent event){
		setSpeed(event.getPlayer());
	}

	@Override
	public void applyConfig(CompatConfig cfg, String prefix) {
		enabled = cfg.getBoolean(prefix + "set-speed.enabled", false);
		flySpeed = cfg.getDouble(prefix + "set-speed.fly-speed", 1.0).floatValue();
		walkSpeed = cfg.getDouble(prefix + "set-speed.walk-speed", 1.0).floatValue();
//		allowFlightPerm = cfg.getString(prefix + "set-speed.allow-flight-permission", ref.allowFlightPerm);
	}

	@Override
	public boolean updateConfig(CompatConfig cfg, String prefix) {
		CompatConfig defaults = CompatConfigFactory.getConfig(null);
		defaults.set(prefix + "set-speed.enabled", false);
		defaults.set(prefix + "set-speed.fly-speed", 1.0);
		defaults.set(prefix + "set-speed.walk-speed", 1.0);
//		cfg.set(prefix + "set-speed.allow-flight-permission", ref.allowFlightPerm);
		return ConfigUtil.forceDefaults(defaults, cfg);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

//	public String getAllowFlightPerm() {
//		return allowFlightPerm;
//	}

//	public void setAllowFlightPerm(String allowFlightPerm) {
//		this.allowFlightPerm = allowFlightPerm;
//	}

}

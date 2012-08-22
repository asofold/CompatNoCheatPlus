package me.asofold.bpl.cncp.hooks.generic;

import me.asofold.bpl.cncp.hooks.AbstractHook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;

public class HookSetSpeed extends AbstractHook implements Listener{
	
	private float flySpeed = 1.0f;
	
	private float walkSpeed = 1.0f;
	
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
		return "1.0";
	}

	@Override
	public CheckType[] getCheckTypes() {
		return new CheckType[0];
	}

	@Override
	public Listener[] getListeners() {
		return new Listener[]{this}	;
	}

	public float getFlySpeed() {
		return flySpeed;
	}

	public void setFlySpeed(float flySpeed) {
		this.flySpeed = flySpeed;
	}

	public float getWalkSpeed() {
		return walkSpeed;
	}

	public void setWalkSpeed(float walkSpeed) {
		this.walkSpeed = walkSpeed;
	}
	
	public final void setSpeed(final Player player){
//		if (allowFlightPerm.equals("") || player.hasPermission(allowFlightPerm)) player.setAllowFlight(true);
		player.setWalkSpeed(walkSpeed);
		player.setFlySpeed(flySpeed);
	}
	
	final void onPlayerJoin(final PlayerJoinEvent event){
		setSpeed(event.getPlayer());
	}

//	public String getAllowFlightPerm() {
//		return allowFlightPerm;
//	}

//	public void setAllowFlightPerm(String allowFlightPerm) {
//		this.allowFlightPerm = allowFlightPerm;
//	}

}

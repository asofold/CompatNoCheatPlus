package me.asofold.bpl.cncp.hooks.ncp;

/**
 * Type of checks and their groups (potentially parents).
 * @author mc_dev
 *
 */
public enum CheckType {
	
	// GENERAL
	/**
	 * Use to register for all checks.
	 */
	ALL,
	
	/**
	 * Do not use to register, only for internals / compatibility issues,
	 * it might be passed to NCPHook.onCheckFailure.
	 */
	UNKNOWN,
	
	// MOVING
	MOVING,
	MOVING_NOFALL(MOVING),
	MOVING_SURVIVALFLY(MOVING),
	MOVING_CREATIVEFLY(MOVING),
	MOVING_MOREPACKETS(MOVING),
	
	// FIGHT
	FIGHT,
	FIGHT_SPEED(FIGHT),
	FIGHT_NOSWING(FIGHT),
	FIGHT_DIRECTION(FIGHT),
	FIGHT_REACH(FIGHT),
	FIGHT_ANGLE(FIGHT),
	FIGHT_CRITICAL(FIGHT),
	FIGHT_GODMODE(FIGHT),
	FIGHT_INSTANTHEAL(FIGHT),
	FIGHT_KNOCKBACK(FIGHT),
	
	// BLOCKBREAK
	BLOCKBREAK,
	BLOCKBREAK_FASTBREAK(BLOCKBREAK),
	BLOCKBREAK_NOSWING(BLOCKBREAK),
	BLOCKBREAK_DIRECTION(BLOCKBREAK),
	BLOCKBREAK_REACH(BLOCKBREAK),
	
	// BLOCKPLACE
	BLOCKPLACE,
	BLOCKPLACE_FASTPLACE(BLOCKPLACE),
	BLOCKPLACE_NOSWING(BLOCKPLACE),
	BLOCKPLACE_DIRECTION(BLOCKPLACE),
	BLOCKPLACE_SPEED(BLOCKPLACE),
	BLOCKPLACE_REACH(BLOCKPLACE),
	
	// BLOCKINTERACT
	BLOCKINTERACT,
	BLOCKINTERACT_NOSWING(BLOCKINTERACT),
	BLOCKINTERACT_DIRECTION(BLOCKINTERACT),
	BLOCKINTERACT_REACH(BLOCKINTERACT),
	
	// INVENTORY
	INVENTORY,
	INVENTORY_DROP(INVENTORY),
	INVENTORY_INSTANTBOW(INVENTORY),
	INVENTORY_INSTANTEAT(INVENTORY),
	
	// CHAT
	CHAT,
	CHAT_ARRIVALS(CHAT),
	CHAT_NOPWNAGE(CHAT),
	
	; // end of members.
	
	/**
	 * The check group, null if it is a group itself.
	 */
	public final CheckType group;
	
	private CheckType(){
		group = null;
	}
	
	private CheckType(CheckType group){
		this.group = group;
	}
}

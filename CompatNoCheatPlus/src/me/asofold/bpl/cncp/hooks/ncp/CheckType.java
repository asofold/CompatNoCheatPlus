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
	
	// FIGHT
	FIGHT,
	FIGHT_SPEED(FIGHT),
	FIGHT_ANGLE(FIGHT),
	
	// BLOCKBREAK
	BLOCKBREAK,
	BLOCKBREAK_FASTBREAK(BLOCKBREAK),
	BLOCKBREAK_NOSWING(BLOCKBREAK),
	BLOCKBREAK_DIRECTION(BLOCKBREAK),
	
	// TODO: add more ....
	
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

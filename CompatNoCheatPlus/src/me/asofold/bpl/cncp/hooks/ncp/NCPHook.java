package me.asofold.bpl.cncp.hooks.ncp;

import org.bukkit.entity.Player;

/**
 * Compatibility hooks have to implement this.<br>
 * To set VL etc they have to make use of another API,
 * be it in NCPHookManager or probably rather better in NCP or some NCPAPI class.
 * @author mc_dev
 *
 */
public interface NCPHook{
	
	/**
	 * For logging purposes.
	 * @return
	 */
	public String getHookName();
	
	/**
	 * For logging purposes.
	 * @return
	 */
	public String getHookVersion();
	
	/**
	 * This is called on failure of a check.<br>
	 * This is the minimal interface, one should probably add specific information 
	 * like (target) locations and VL,  but with this a lot is possible already (cncp).<br>
	 * See AbstractNCPHook for future questions.
	 * @param checkType the check that failed.
	 * @param player The player that failed the check.
	 * @return If to cancel the check failure processing.
	 */
	public boolean onCheckFailure(CheckType checkType, Player player);
	
}



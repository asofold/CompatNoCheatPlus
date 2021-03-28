package me.asofold.bpl.cncp.hooks.generic;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

/**
 * Auxiliary methods and data structure to handle simple sort of exemption. <br>
 * NOTE: Not thread safe.
 * 
 * @deprecated: Buggy / outdated concept - should not rely on nested stuff, use TickTask2 to unexempt timely if in doubt.
 * 
 * @author mc_dev
 *
 */
public class ExemptionManager{
	
	public static enum Status{
		EXEMPTED,
		NOT_EXEMPTED,
		NEEDS_EXEMPTION,
		NEEDS_UNEXEMPTION,
	}
	
	public static class ExemptionInfo{
		public static class CheckEntry{
			public int skip = 0;
			public int exempt = 0;
		}
		/** Counts per type, for players being exempted already, to prevent unexempting. */
		public final Map<CheckType, CheckEntry> entries = new HashMap<CheckType, CheckEntry>();
		/**
		 * If empty, it can get removed.
		 * @return
		 */
		public boolean isEmpty(){
			return entries.isEmpty();
		}
		/**
		 *
		 * @param type
		 * @param isExempted If the player is currently exempted, to be filled in with NCPHookManager.isExempted(player, type)
		 * @return EXEMPTED or NEEDS_EXEMPTION. The latter means the player has to be exempted.
		 */
		public Status increase(final CheckType type, final boolean isExempted){
			final CheckEntry entry = entries.get(type);
			if (entry == null){
				final CheckEntry newEntry = new CheckEntry();
				entries.put(type, newEntry);
				if (isExempted){
					newEntry.skip = 1;
					return Status.EXEMPTED;
				}
				else{
					newEntry.exempt = 1;
					return Status.NEEDS_EXEMPTION;
				}
			}
			if (entry.skip > 0){
				if (isExempted){
					entry.skip ++;
					return Status.EXEMPTED;
				}
				else{
					entry.exempt = entry.skip + 1;
					entry.skip = 0;
					return Status.NEEDS_EXEMPTION;
				}
				
			}
			else{
				// entry.exempt > 0
				entry.exempt ++;
				return isExempted ? Status.EXEMPTED : Status.NEEDS_EXEMPTION;
			}
		}
		
		/**
		 *
		 * @param type
		 * @param isExempted If the player is currently exempted, to be filled in with NCPHookManager.isExempted(player, type)
		 * @return Status, if NEEDS_EXEMPTION the player has to be exempted. If NEEDS_UNEXEMPTION, the player has to be unexempted.
		 */
		public Status decrease(final CheckType type, final boolean isExempted){
			final CheckEntry info = entries.get(type);
			if (info == null) return isExempted ? Status.EXEMPTED : Status.NOT_EXEMPTED;
			if (info.skip > 0){
				info.skip --;
				if (info.skip == 0){ 
					entries.remove(type);
					return isExempted ? Status.EXEMPTED : Status.NOT_EXEMPTED;
				}
				else if (isExempted) return Status.EXEMPTED;
				else{
					info.exempt = info.skip;
					info.skip = 0;
					return Status.NEEDS_EXEMPTION;
				}
			}
			else{
				info.exempt --;
				if (info.exempt == 0){
					 entries.remove(type);
					 return isExempted ? Status.NEEDS_UNEXEMPTION : Status.NOT_EXEMPTED;
				}
				else{
					return isExempted ? Status.EXEMPTED : Status.NEEDS_EXEMPTION;
				}
			}
		}
	}
	
	/** Exact player name -> ExemptionInfo */
	protected final Map<String, ExemptionInfo> exemptions;
	
	
	public ExemptionManager(){
		this(30, 0.75f);
	}
	
	/**
	 * 
	 * @param initialCapacity For the exemption HashMap.
	 * @param loadFactor For the exemption HashMap.
	 */
	public ExemptionManager(int initialCapacity, float loadFactor) {
		exemptions = new HashMap<String, ExemptionManager.ExemptionInfo>(initialCapacity, loadFactor);
	}

	/**
	 * Add exemption count and exempt, if necessary.
	 * @param player
	 * @param type
	 * @return If the player was exempted already.
	 */
	public boolean addExemption(final Player player, final CheckType type){
		final Status status = addExemption(player.getName(), type, NCPExemptionManager.isExempted(player, type));
		if (status == Status.NEEDS_EXEMPTION) NCPExemptionManager.exemptPermanently(player, type);
//		System.out.println("add: " + type.toString() + " -> " + status);
		return status == Status.EXEMPTED;
	}
	
	/**
	 * Increment exemption count.
	 * @param name
	 * @return If exemption is needed (NEEDS_EXEMPTION).
	 */
	public Status addExemption(final String name, final CheckType type, boolean isExempted){
		 final ExemptionInfo info = exemptions.get(name);
		 final Status status;
		 if (info == null){
			 final ExemptionInfo newInfo = new ExemptionInfo();
			 status = newInfo.increase(type, isExempted);
			 exemptions.put(name, newInfo);
		 }
		 else{
			 status = info.increase(type, isExempted);
		 }

		return status;
	}
	
	/**
	 * Decrement exemption count, exempt or unexempt if necessary.
	 * @param player
	 * @param type
	 * @return If the player is still exempted.
	 */
	public boolean removeExemption(final Player player, final CheckType type){
		final Status status = removeExemption(player.getName(), type, NCPExemptionManager.isExempted(player, type));
		if (status == Status.NEEDS_EXEMPTION) NCPExemptionManager.exemptPermanently(player, type);
		else if (status == Status.NEEDS_UNEXEMPTION) NCPExemptionManager.unexempt(player, type);
//		System.out.println("remove: " + type.toString() + " -> " + status);
		return status == Status.EXEMPTED || status == Status.NEEDS_EXEMPTION;
	}
	
	/**
	 * Decrement exemption count.
	 * @param name
	 * @return Status, NEEDS_EXEMPTION  and NEEDS_UNEXEMPTION make it necessary to call NCP API.
	 */
	public Status removeExemption(final String name, final CheckType type, boolean isExempted){
		final ExemptionInfo info = exemptions.get(name);
		if (info == null) return isExempted ? Status.EXEMPTED : Status.NOT_EXEMPTED;
		final Status status = info.decrease(type, isExempted);
		if (info.isEmpty()) exemptions.remove(name);
		return status;
	}
	 
	/**
	 * Check if the player should be exempted right now according to stored info.
	 * @param name
	 * @param type
	 * @return
	 */
	public boolean shouldBeExempted(final String name, final CheckType type){
		final ExemptionInfo info = exemptions.get(name);
		if (info == null) return false;
		return !info.isEmpty();
	}
	
	/**
	 * Hides the API access from listeners potentially.
	 * @param player
	 * @param checkType
	 */
	public void exempt(final Player player, final CheckType checkType){
		NCPExemptionManager.exemptPermanently(player, checkType);
	}
	
	/**
	 * Hides the API access from listeners potentially.
	 * @param player
	 * @param checkType
	 */
	public void unexempt(final Player player, final CheckType checkType){
		NCPExemptionManager.unexempt(player, checkType);
	}
	
}

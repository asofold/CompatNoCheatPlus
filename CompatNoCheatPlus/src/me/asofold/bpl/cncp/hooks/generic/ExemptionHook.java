package me.asofold.bpl.cncp.hooks.generic;

import java.util.LinkedHashMap;
import java.util.Map;

import me.asofold.bpl.cncp.hooks.AbstractHook;

/**
 * Auxiliary methods and data structure to handle simple sort of exemption. 
 * @author mc_dev
 *
 */
public abstract class ExemptionHook extends AbstractHook{
	
	
	protected final Map<String, Integer> exemptions = new LinkedHashMap<String, Integer>(30);
	
	/**
	 * Increment exemption count.
	 * @param name
	 */
	public void addExemption(final String name){
		final Integer count = exemptions.get(name);
		if (count == null){
			exemptions.put(name, 1);
		}
		else{
			exemptions.put(name, count.intValue() + 1);
		}
	}
	
	/**
	 * Decrement exemption count.
	 * @param name
	 * @return If the player is still exempted.
	 */
	public boolean removeExemption(final String name){
		final Integer count = exemptions.remove(name);
		if (count == null) return false;
		final int v = count.intValue();
		if (v == 1) return false;
		else{
			exemptions.put(name, v - 1);
			return true;
		}
	}
	
}

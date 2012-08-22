package me.asofold.bpl.cncp.hooks.generic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import me.asofold.bpl.cncp.hooks.AbstractHook;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPHook;

public final class HookPlayerClass extends AbstractHook {
	
	private final Set<String> classNames = new HashSet<String>();
	
	private boolean exemptAll = true;
	
	private boolean checkSuperClass = true;
	
	private Object ncpHook = null;
	
	/**
	 * Normal class name.
	 */
	private String playerClassName = "CraftPlayer";
	
	public HookPlayerClass(){
		this.classNames.addAll(classNames);
	}
	
	public final void setClassNames(final Collection<String> classNames){
		this.classNames.clear();
		this.classNames.addAll(classNames);
	}
	
	public final void setExemptAll(final boolean exemptAll){
		this.exemptAll = exemptAll;
	}
	
	public final void setPlayerClassName(final String playerClassName){
		this.playerClassName = playerClassName;
	}
	
	public final void setCheckSuperClass(final boolean superClass){
		this.checkSuperClass = superClass;
	}

	@Override
	public final String getHookName() {
		return "PlayerClass(default)";
	}

	@Override
	public final String getHookVersion() {
		return "1.0";
	}

	

	@Override
	public NCPHook getNCPHook() {
		if (ncpHook == null){
			ncpHook = new NCPHook() {
				@Override
				public boolean onCheckFailure(CheckType checkType, Player player) {
					if (exemptAll && !player.getClass().getSimpleName().equals(playerClassName)) return true;
					else {
						if (classNames.isEmpty()) return false;
						final Class<?> clazz = player.getClass();
						final String name = clazz.getSimpleName();
						if (classNames.contains(name)) return true;
						else if (checkSuperClass){
							while (true){
								final Class<?> superClass = clazz.getSuperclass();
								if (superClass  == null) return false;
								else{
									final String superName = superClass.getSimpleName();
									if (superName.equals("Object")) return false;
									else if (classNames.contains(superName)){
										return true;
									}
								}
							} 
						}
					}
					return false; // ECLIPSE
				}
				
				@Override
				public String getHookVersion() {
					return "1.0";
				}
				
				@Override
				public String getHookName() {
					return "PlayerClass(cncp)";
				}
			};
		}
		return (NCPHook) ncpHook;
	}

}

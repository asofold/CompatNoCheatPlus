package me.asofold.bpl.cncp.hooks.generic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import me.asofold.bpl.cncp.hooks.AbstractHook;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;

public final class HookPlayerClass extends AbstractHook {
	
	private final Set<String> classNames = new HashSet<String>();
	
	private boolean exemptAll = true;
	
	private boolean checkSuperClass = true;
	
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
		return "0.1";
	}


	@Override
	public final boolean onCheckFailure(CheckType checkType, final Player player) {
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

}

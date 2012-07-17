package me.asofold.bukkit.cncp.hooks.citizens;

import me.asofold.bukkit.cncp.hooks.AbstractHook;
import net.citizensnpcs.resources.npclib.PathNPC;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckEvent;

public final class HookCitizens extends AbstractHook {

	@Override
	public String getHookName() {
		return "Citizens(default)";
	}

	@Override
	public String getHookVersion() {
		return "0.0";
	}

	@Override
	public final void processEvent(final String group, final String check, final CheckEvent event) {
		final Player player = event.getPlayer().getBukkitPlayer();
		if (player instanceof CraftPlayer){
			final CraftPlayer cp = (CraftPlayer) player;
			if (cp.getHandle() instanceof PathNPC) event.setCancelled(true);
		}
	}

}

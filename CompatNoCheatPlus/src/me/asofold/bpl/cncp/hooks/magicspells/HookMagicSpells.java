package me.asofold.bpl.cncp.hooks.magicspells;

import me.asofold.bpl.cncp.hooks.AbstractConfigurableHook;
import me.asofold.bpl.cncp.utils.TickTask2;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spell.SpellCastState;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellCastedEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

public class HookMagicSpells extends AbstractConfigurableHook implements Listener{
	
	public HookMagicSpells(){
		super("MagicSpells(default)", "0.0", "magicspells.");
		assertPluginPresent("MagicSpells");
	}

	@Override
	public Listener[] getListeners() {
		return new Listener[]{
			this
		};
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSpellCast(final SpellCastEvent event){
		if (event.getSpellCastState() != SpellCastState.NORMAL) return;
		exempt(event.getCaster(), event.getSpell());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSpellCasted(final SpellCastedEvent event){
		unexempt(event.getCaster(), event.getSpell());
	}
	
	private void exempt(final Player player, final Spell spell) {
		final CheckType[] types = getCheckTypes(spell);
		if (types == null) return;
		for (final CheckType type : types){
			NCPExemptionManager.exemptPermanently(player, type);
			// Safety fall-back:
			// TODO: Might interfere with "slow" effects of spells ? [might add config for this.]
			TickTask2.addUnexemptions(player, types);
		}
	}
	
	private void unexempt(final Player player, final Spell spell) {
		final CheckType[] types = getCheckTypes(spell);
		if (types == null) return;
		for (final CheckType type : types){
			NCPExemptionManager.unexempt(player, type);
		}
	}
	
	protected CheckType[] getCheckTypes(final Spell spell){
		// TODO: Find checktypes according to spell (config ?)
		// TODO: Use config !
		return null;
	}
}

package me.asofold.bpl.cncp.hooks.magicspells;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.asofold.bpl.cncp.config.compatlayer.CompatConfig;
import me.asofold.bpl.cncp.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.cncp.config.compatlayer.ConfigUtil;
import me.asofold.bpl.cncp.hooks.AbstractConfigurableHook;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import fr.neatmonster.nocheatplus.checks.CheckType;

public class HookMagicSpells extends AbstractConfigurableHook implements Listener{
	
	protected final Map<String, CheckType[]> spellMap = new HashMap<String, CheckType[]>();
	
	public HookMagicSpells(){
		super("MagicSpells(default)", "1.0", "magicspells.");
		assertPluginPresent("MagicSpells");
	}

	@Override
	public Listener[] getListeners() {
		return new Listener[]{
			this
		};
	}
	
//	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//	public void onSpellCast(final SpellCastEvent event){
//		if (event.getSpellCastState() != SpellCastState.NORMAL) return;
//		exempt(event.getCaster(), event.getSpell());
//	}
//
//	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//	public void onSpellCasted(final SpellCastedEvent event){
//		unexempt(event.getCaster(), event.getSpell());
//	}
//	
//	private void exempt(final Player player, final Spell spell) {
//		final CheckType[] types = getCheckTypes(spell);
//		if (types == null) return;
//		for (final CheckType type : types){
//			NCPExemptionManager.exemptPermanently(player, type);
//			// Safety fall-back:
//			// TODO: Might interfere with "slow" effects of spells ? [might add config for this.]
//			TickTask2.addUnexemptions(player, types);
//		}
//	}
//	
//	private void unexempt(final Player player, final Spell spell) {
//		final CheckType[] types = getCheckTypes(spell);
//		if (types == null) return;
//		for (final CheckType type : types){
//			NCPExemptionManager.unexempt(player, type);
//		}
//	}
//	
//	protected CheckType[] getCheckTypes(final Spell spell){
//		return spellMap.get(spell.getName());
//	}

	/* (non-Javadoc)
	 * @see me.asofold.bpl.cncp.hooks.AbstractConfigurableHook#applyConfig(me.asofold.bpl.cncp.config.compatlayer.CompatConfig, java.lang.String)
	 */
	@Override
	public void applyConfig(CompatConfig cfg, String prefix) {
		super.applyConfig(cfg, prefix);
		prefix += this.configPrefix;
		List<String> keys = cfg.getStringKeys(prefix + "exempt-spells");
		for (String key : keys){
			String fullKey = ConfigUtil.bestPath(cfg, prefix + "exempt-spells." + key);
			String types = cfg.getString(fullKey);
			if (types == null) continue;
			String[] split = types.split(",");
			Set<CheckType> checkTypes = new HashSet<CheckType>();
			for (String input : split){
				input = input.trim().toUpperCase().replace('-', '_').replace(' ', '_').replace('.', '_');
				CheckType type = null;
				try{
					type = CheckType.valueOf(input);
				}
				catch(Throwable t){
				}
				if (type == null){
					Bukkit.getLogger().warning("[cncp] HookMagicSpells: Bad check type at " + fullKey + ": " + input);
				}
				else checkTypes.add(type);
			}
			if (checkTypes.isEmpty()){
				Bukkit.getLogger().warning("[cncp] HookMagicSpells: No CheckType entries at: " + fullKey);
			}
			else{
				CheckType[] a = new CheckType[checkTypes.size()];
				checkTypes.toArray(a);
				spellMap.put(key, a);
			}
		}
	}

	/* (non-Javadoc)
	 * @see me.asofold.bpl.cncp.hooks.AbstractConfigurableHook#updateConfig(me.asofold.bpl.cncp.config.compatlayer.CompatConfig, java.lang.String)
	 */
	@Override
	public boolean updateConfig(CompatConfig cfg, String prefix) {
		super.updateConfig(cfg, prefix);
		CompatConfig defaults = CompatConfigFactory.getConfig(null);
		prefix += this.configPrefix;
		// TODO: Write default section.
		defaults.set(prefix + "NOTE", "MagicSpells support is experimental, only instant spells can be added here.");
		defaults.set(prefix + "exempt-spells.NameOfExampleSpell", "COMBINED_MUNCHHAUSEN, UNKNOWN, BLOCKPLACE_NOSWING");
		return ConfigUtil.forceDefaults(defaults, cfg);
	}
	
}

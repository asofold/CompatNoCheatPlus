package me.asofold.bpl.cncp.hooks.mcmmo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import fr.neatmonster.nocheatplus.hooks.NCPHook;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties.ToolProps;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties.ToolType;
import me.asofold.bpl.cncp.CompatNoCheatPlus;
import me.asofold.bpl.cncp.hooks.generic.ExemptionManager;
import me.asofold.bpl.cncp.hooks.generic.HookInstaBreak;
import me.asofold.bpl.cncp.hooks.mcmmo.HookmcMMO.HookFacade;
import me.asofold.bpl.cncp.utils.ActionFrequency;
import me.asofold.bpl.cncp.utils.TickTask2;

@SuppressWarnings("deprecation")
public class HookFacadeImpl implements HookFacade, NCPHook {


    protected final ExemptionManager exMan = new ExemptionManager();

    /** Normal click per block skills. */
    protected final CheckType[] exemptBreakNormal = new CheckType[]{
            CheckType.BLOCKBREAK_FASTBREAK, CheckType.BLOCKBREAK_FREQUENCY,
            CheckType.BLOCKBREAK_NOSWING, 
            CheckType.BLOCKBREAK_WRONGBLOCK, // Not optimal but ok.
    };


    protected final CheckType[] exemptBreakMany = new CheckType[]{
            CheckType.BLOCKBREAK, CheckType.COMBINED_IMPROBABLE,
    };

    /** Fighting damage of effects such as bleeding or area (potentially). */
    protected final CheckType[] exemptFightEffect = new CheckType[]{
            CheckType.FIGHT_SPEED, CheckType.FIGHT_DIRECTION,
            CheckType.FIGHT_ANGLE, CheckType.FIGHT_NOSWING,
            CheckType.FIGHT_REACH, CheckType.COMBINED_IMPROBABLE,
    };

    // Presets for after failure exemption.
    protected final Map<CheckType, Integer> cancelChecksBlockBreak = new HashMap<CheckType, Integer>();
    //	protected final Map<CheckType, Integer> cancelChecksBlockDamage = new HashMap<CheckType, Integer>();
    //	protected final Map<CheckType, Integer> cancelChecksDamage = new HashMap<CheckType, Integer>();

    protected boolean useInstaBreakHook;
    protected int clicksPerSecond;
    protected String cancel = null;
    protected long cancelTicks = 0;

    protected final Map<CheckType, Integer> cancelChecks = new HashMap<CheckType, Integer>();

    /**
     * Last block breaking time
     */
    protected final Map<String, ActionFrequency> lastBreak = new HashMap<String, ActionFrequency>(50);

    /** Counter for nested events to cancel break counting. */
    protected int breakCancel = 0; 

    protected int lastBreakAddCount = 0;
    protected long lastBreakCleanup = 0;

    public HookFacadeImpl(boolean useInstaBreakHook, int clicksPerSecond){
        this.useInstaBreakHook = useInstaBreakHook;
        this.clicksPerSecond = clicksPerSecond;
        cancelChecksBlockBreak.put(CheckType.BLOCKBREAK_NOSWING, 1);
        cancelChecksBlockBreak.put(CheckType.BLOCKBREAK_FASTBREAK, 1);
        //		
        //		cancelChecksBlockDamage.put(CheckType.BLOCKBREAK_FASTBREAK, 1);
        //		
        //		cancelChecksDamage.put(CheckType.FIGHT_ANGLE, 1);
        //		cancelChecksDamage.put(CheckType.FIGHT_SPEED, 1);
    }

    @Override
    public String getHookName() {
        return "mcMMO(cncp)";
    }

    @Override
    public String getHookVersion() {
        return "2.3";
    }

    @Override
    public final boolean onCheckFailure(final CheckType checkType, final Player player, final IViolationInfo info) {
        //		System.out.println(player.getName() + " -> " + checkType + "---------------------------");
        // Somewhat generic canceling mechanism (within the same tick).
        // Might later fail, if block break event gets scheduled after block damage having set insta break, instead of letting them follow directly.
        if (cancel == null){
            return false;
        }

        final String name = player.getName();
        if (cancel.equals(name)){

            if (player.getTicksLived() != cancelTicks){
                cancel = null;
            }
            else{
                final Integer n = cancelChecks.get(checkType);
                if (n == null){
                    return false;
                }
                else if (n > 0){
                    if (n == 1) cancelChecks.remove(checkType);
                    else cancelChecks.put(checkType,  n - 1);
                }
                return true;
            }
        }
        return false;
    }

    private  final void setPlayer(final Player player, Map<CheckType, Integer> cancelChecks){
        cancel = player.getName();
        cancelTicks = player.getTicksLived();
        this.cancelChecks.clear();
        this.cancelChecks.putAll(cancelChecks);
    }

    public ToolProps getToolProps(final ItemStack stack){
        if (stack == null) return BlockProperties.noTool;
        else return BlockProperties.getToolProps(stack);
    }

    public void addExemption(final Player player, final CheckType[] types){
        for (final CheckType type : types){
            exMan.addExemption(player, type);
            TickTask2.addUnexemptions(player, types);
        }
    }

    public void removeExemption(final Player player, final CheckType[] types){
        for (final CheckType type : types){
            exMan.removeExemption(player, type);
        }
    }

    @Override
    public final void damageLowest(final Player player) {
        //		System.out.println("damage lowest");
        //		setPlayer(player, cancelChecksDamage);
        addExemption(player, exemptFightEffect);
    }

    @Override
    public final void blockDamageLowest(final Player player) {
        //		System.out.println("block damage lowest");
        //		setPlayer(player, cancelChecksBlockDamage);
        if (getToolProps(player.getItemInHand()).toolType == ToolType.AXE) addExemption(player, exemptBreakMany);
        else addExemption(player, exemptBreakNormal);
    }

    @Override
    public final boolean blockBreakLowest(final Player player) {
        //		System.out.println("block break lowest");
        final boolean isAxe = getToolProps(player.getItemInHand()).toolType == ToolType.AXE;
        if (breakCancel > 0){
            breakCancel ++;
            return true;
        }
        final String name = player.getName();
        ActionFrequency freq = lastBreak.get(name);
        final long now = System.currentTimeMillis();
        if (freq == null){
            freq = new ActionFrequency(3, 333);
            freq.add(now, 1f);
            lastBreak.put(name, freq);
            lastBreakAddCount ++;
            if (lastBreakAddCount > 100){
                lastBreakAddCount = 0;
                cleanupLastBreaks();
            }
        }
        else if (!isAxe){
            freq.add(now, 1f);
            if (freq.score(1f) > (float) clicksPerSecond){
                breakCancel ++;
                return true;
            }
        }

        addExemption(player, exemptBreakNormal);
        if (useInstaBreakHook){
            HookInstaBreak.addExemptNext(exemptBreakNormal);
            TickTask2.addUnexemptions(player, exemptBreakNormal);
        }
        else if (!isAxe){
            setPlayer(player, cancelChecksBlockBreak);
            Bukkit.getScheduler().scheduleSyncDelayedTask(CompatNoCheatPlus.getInstance(), new Runnable() {
                @Override
                public void run() {
                    DataManager.removeData(player.getName(), CheckType.BLOCKBREAK_FASTBREAK);
                }
            });
        }
        return false;
    }

    protected void cleanupLastBreaks() {
        final long ts = System.currentTimeMillis();
        if (ts - lastBreakCleanup < 30000 && ts > lastBreakCleanup) return;
        lastBreakCleanup = ts;
        final List<String> rem = new LinkedList<String>();
        if (ts >= lastBreakCleanup){
            for (final Entry<String, ActionFrequency> entry : lastBreak.entrySet()){
                if (entry.getValue().score(1f) == 0f) rem.add(entry.getKey());
            }
        }
        else{
            rem.addAll(lastBreak.keySet());
        }
        for (final String key :rem){
            lastBreak.remove(key);
        }
    }

    @Override
    public void damageMonitor(Player player) {
        //		System.out.println("damage monitor");
        removeExemption(player, exemptFightEffect);
    }

    @Override
    public void blockDamageMonitor(Player player) {
        //		System.out.println("block damage monitor");
        if (getToolProps(player.getItemInHand()).toolType == ToolType.AXE) addExemption(player, exemptBreakMany);
        else removeExemption(player, exemptBreakNormal);
    }

    @Override
    public void blockBreakMontitor(Player player) {
        if (breakCancel > 0){
            breakCancel --;
            return;
        }
        //		System.out.println("block break monitor");
        removeExemption(player, exemptBreakNormal);
    }



}

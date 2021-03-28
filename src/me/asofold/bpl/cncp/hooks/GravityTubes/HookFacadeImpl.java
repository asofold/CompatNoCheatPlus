package me.asofold.bpl.cncp.hooks.GravityTubes;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.permissions.Permissible;

import com.benzoft.gravitytubes.GTPerm;
import com.benzoft.gravitytubes.GravityTube;
import com.benzoft.gravitytubes.files.ConfigFile;
import com.benzoft.gravitytubes.files.GravityTubesFile;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.hooks.NCPHook;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.location.TrigUtil;
import me.asofold.bpl.cncp.hooks.GravityTubes.HookGravityTubes.HookFacade;

public class HookFacadeImpl implements HookFacade, NCPHook {

    public HookFacadeImpl(){}

    @Override
    public String getHookName() {
        return "GravityTubes(cncp)";
    }

    @Override
    public String getHookVersion() {
        return "1.1";
    }

    @Override
    public final boolean onCheckFailure(final CheckType checkType, final Player player, final IViolationInfo info) {
        //if (checkType == CheckType.MOVING_CREATIVEFLY && !ConfigFile.getInstance().isSneakToFall() )
        if (player.getGameMode() == GameMode.SPECTATOR) return false;
        if ((checkType == CheckType.MOVING_CREATIVEFLY || checkType == CheckType.MOVING_SURVIVALFLY) && GTPerm.USE.checkPermission((Permissible)player)) {
            final GravityTube tube = GravityTubesFile.getInstance().getTubes().stream().filter(gravityTube -> isInTube(gravityTube, player, true, true)).findFirst().orElse(null);
            if (tube != null) {
                return true;
            }
        }
        return false;
    }

    private boolean isInTube(GravityTube tube, Player p, boolean longerH, boolean extendxz) {
        final int tubePower = tube.getPower();
        int power = tubePower > 171 ? 4 : tubePower > 80 ? 3 : tubePower > 25 ? 2 : 1;
        final Location pLoc = p.getLocation();
        final Location tLoc = tube.getSourceLocation();
        final boolean b1 = p.getWorld().equals(tLoc.getWorld()) && pLoc.getY() >= tLoc.getBlockY() && pLoc.getY() <= tLoc.getBlockY() + tube.getHeight() + (longerH ? power : 0);
        if (!extendxz) 
            return b1 && pLoc.getBlockX() == tLoc.getBlockX() && pLoc.getBlockZ() == tLoc.getBlockZ();
        return b1 && isTubeNearby(pLoc.getBlockX(), pLoc.getBlockZ(), tLoc.getBlockX(), tLoc.getBlockZ()); 
    }

    private boolean isTubeNearby(int x1, int z1, int x2, int z2) {
        return TrigUtil.distance(x1,z1,x2,z2) < 1.5;
    }

    @Override
    public void onMoveLowest(PlayerMoveEvent event) {
        final Player p = event.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR) return;
        final double hDist = TrigUtil.xzDistance(event.getFrom(), event.getTo());
        final double vDist = event.getFrom().getY() - event.getTo().getY();
        if (GTPerm.USE.checkPermission((Permissible)p) 
            && vDist > 0 && hDist < 0.35 
            && ConfigFile.getInstance().isDisableFallDamage() && p.isSneaking()) {
            final GravityTube tube = GravityTubesFile.getInstance().getTubes().stream().filter(gravityTube -> isInTube(gravityTube, p, false, false)).findFirst().orElse(null);
            final IPlayerData pData = DataManager.getPlayerData(p);
            if (tube != null && pData != null) {
                final MovingData mData = pData.getGenericInstance(MovingData.class);
                mData.clearNoFallData();
            }
        }
    }
}

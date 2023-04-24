package me.asofold.bpl.cncp.ClientVersion;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import com.viaversion.viaversion.api.Via;
import fr.neatmonster.nocheatplus.compat.Folia;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;
import me.asofold.bpl.cncp.CompatNoCheatPlus;

public class ClientVersionListener implements Listener {
    
    private Plugin ViaVersion = Bukkit.getPluginManager().getPlugin("ViaVersion");
    private Plugin ProtocolSupport = Bukkit.getPluginManager().getPlugin("ProtocolSupport");
    private final Class<?> ProtocolSupportAPIClass = ReflectionUtil.getClass("protocolsupport.api.ProtocolSupportAPI");
    private final Class<?> ProtocolVersionClass = ReflectionUtil.getClass("protocolsupport.api.ProtocolVersion");
    private final Method getProtocolVersion = ProtocolSupportAPIClass == null ? null : ReflectionUtil.getMethod(ProtocolSupportAPIClass, "getProtocolVersion", Player.class);
    
    @SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        Folia.runSyncDelayedTask(CompatNoCheatPlus.getInstance(), (arg) -> {
            final IPlayerData pData = DataManager.getPlayerData(player);
            if (pData != null) {
                if (ViaVersion != null && ViaVersion.isEnabled()) {
                    // Give precedence to ViaVersion
                    pData.setClientVersionID(Via.getAPI().getPlayerVersion(player));
                } 
                else if (ProtocolSupport != null && getProtocolVersion != null && ProtocolSupport.isEnabled()) {
                    // Fallback to PS
                    Object protocolVersion = ReflectionUtil.invokeMethod(getProtocolVersion, null, player);
                    Method getId = ReflectionUtil.getMethodNoArgs(ProtocolVersionClass, "getId", int.class);
                    int version = (int) ReflectionUtil.invokeMethodNoArgs(getId, protocolVersion);
                    pData.setClientVersionID(version);
                }
                // (Client version stays unknown (-1))
            }
        }, 20); // Wait 20 ticks before setting client data
    }
}

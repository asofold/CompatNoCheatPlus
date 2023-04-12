package me.asofold.bpl.cncp.ClientVersion;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import com.viaversion.viaversion.api.Via;
import protocolsupport.api.ProtocolSupportAPI;

import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import me.asofold.bpl.cncp.CompatNoCheatPlus;
import me.asofold.bpl.cncp.config.Settings;

public class ClientVersionListener implements Listener {
    
    private Plugin ViaVersion = Bukkit.getPluginManager().getPlugin("ViaVersion");
    private Plugin ProtocolSupport = Bukkit.getPluginManager().getPlugin("ProtocolSupport");
    
    @SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        Bukkit.getScheduler()
              .runTaskLater(CompatNoCheatPlus.getInstance(), new Runnable() {
                @Override
                public void run() {
                    final IPlayerData pData = DataManager.getPlayerData(player);
                    if (pData != null) {
                        if (ViaVersion != null && ViaVersion.isEnabled()) {
                            // Give precedence to ViaVersion
                            pData.setClientVersionID(Via.getAPI().getPlayerVersion(player));
                        } 
                        else if (ProtocolSupport != null && ProtocolSupport.isEnabled()) {
                            // Fallback to PS
                            pData.setClientVersionID(ProtocolSupportAPI.getProtocolVersion(player).getId());
                        }
                        // (Client version stays unknown (-1))
                    }
                }
            }, 20); // Wait 20 ticks before setting client data
    }
}

package me.asofold.bpl.cncp.bungee;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.floodgate.FloodgateAPI;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.MinecraftProtocol;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class CompatNoCheatPlus extends Plugin implements Listener {
    private boolean floodgate;
    private boolean geyser;

    @Override
    public void onEnable() {
        geyser = checkGeyser();
        floodgate = checkFloodgate();
        getLogger().info("Registering listeners");
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().registerChannel("cncp:geyser");
        getLogger().info("cncp Bungee mode with Geyser : " + geyser + ", Floodgate : " + floodgate);
    }

    @EventHandler
    public void onMessageReceive(PluginMessageEvent event) {
        if (event.getTag().equalsIgnoreCase("cncp:geyser")) {
            // Message sent from client, cancel it
            if (event.getSender() instanceof ProxiedPlayer) {
                event.setCancelled(true);
            }
        }
    }

    private boolean checkFloodgate() {
        return ProxyServer.getInstance().getPluginManager().getPlugin("floodgate-bungee") != null;
    }

    private boolean checkGeyser() {
        return ProxyServer.getInstance().getPluginManager().getPlugin("Geyser-BungeeCord") != null;
    }

    private boolean isBedrockPlayer(ProxiedPlayer player) {
        if (floodgate) {
            return FloodgateAPI.isBedrockPlayer(player.getUniqueId());
        } else if (geyser) {
            return GeyserConnector.getInstance().getPlayers().stream()
                    .map(GeyserSession::getProtocol)
                    .map(MinecraftProtocol::getProfile)
                    .map(GameProfile::getName)
                    .anyMatch(name -> player.getName().equals(name));
        }
        return false;
    }

    @EventHandler
    public void onChangeServer(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        Server server = player.getServer();

        if (!isBedrockPlayer(player)) return;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeUTF(player.getName());
            getProxy().getScheduler().schedule(this, () -> {
                server.sendData("cncp:geyser", outputStream.toByteArray());
            }, 1L, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

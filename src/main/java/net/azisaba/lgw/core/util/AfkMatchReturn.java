package net.azisaba.lgw.core.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/** Shared exit path for LGW's inactivity timer and Essentials' AFK status. Call on the server thread. */
public final class AfkMatchReturn {
    private AfkMatchReturn() { }

    public static void leave(LeonGunWar plugin, Player player) {
        if (!player.isOnline() || !plugin.getManager().isPlayerMatching(player)) return;
        try {
            plugin.getManager().removeEntryPlayer(player);
            plugin.getManager().kickPlayer(player);
            if (!plugin.getMainConfig().isLobby) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(plugin.getMainConfig().lobbyServer);
                player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            }
            player.sendMessage(Chat.f("{0}&7放置と判定されたため試合から退出しました", LeonGunWar.GAME_PREFIX));
            plugin.getLogger().info(player.getName() + " を試合から退出させました");
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING, "AFK退出処理に失敗: " + player.getName(), exception);
        }
    }
}

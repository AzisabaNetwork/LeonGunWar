package net.azisaba.lgw.core.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.Chat;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (LeonGunWar.getPlugin().getMainConfig().isLobby) {
            Location lobby = LeonGunWar.getPlugin().getSpawnsConfig().getLobby();
            if (lobby == null) {
                player.sendMessage(Chat.f("{0}&cロビーの移動先が未設定、またはワールドが読み込まれていません。管理者に連絡してください。", LeonGunWar.GAME_PREFIX));
                if (player.hasPermission("leongunwar.command.leongunwaradmin")) {
                    player.sendMessage(Chat.f("&eロビー鯖: plugins/LeonGunWar/spawns.yml の lobby.world と座標を確認してください。"));
                    player.sendMessage(Chat.f("&e試合鯖: plugins/LeonGunWar/main.yml を isLobby: false に設定して完全再起動してください（/lgwadmin reload では反映されません）。"));
                }
                return true;
            }
            if (!player.teleport(lobby)) {
                player.sendMessage(Chat.f("{0}&cロビーへの移動がキャンセルされました。管理者に連絡してください。", LeonGunWar.GAME_PREFIX));
            }
        } else {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(LeonGunWar.getPlugin().getMainConfig().lobbyServer);
            player.sendPluginMessage(LeonGunWar.getPlugin(), "BungeeCord", out.toByteArray());
        }
        return true;
    }
}

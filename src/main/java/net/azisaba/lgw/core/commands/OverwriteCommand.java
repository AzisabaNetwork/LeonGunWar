package net.azisaba.lgw.core.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OverwriteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;

        if (LeonGunWar.getPlugin().config().mainConfig.isLobby) {
            ((Player) sender).teleport(LeonGunWar.getPlugin().config().spawnsConfig.lobby);
        } else {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF("lgw2");
            ((Player) sender).sendPluginMessage(LeonGunWar.getPlugin(), "BungeeCord", out.toByteArray());
        }
        return true;
    }
}

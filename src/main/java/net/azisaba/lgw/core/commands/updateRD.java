package net.azisaba.lgw.core.commands;

import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class updateRD implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        LeonGunWar.getPlugin().updateLeonCSAddon();
        return true;
    }
}

package net.azisaba.lgw.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

// Skriptにあった謎コマンドを追加する
public class KIAICommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Screaaaam!!!");
        return true;
    }
}

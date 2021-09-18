package net.azisaba.lgw.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.azisaba.lgw.core.utils.Chat;


public class MapCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ実行可能です"));
            return true;
        }

        Player p = (Player) sender;

        sender.sendMessage(Chat.f("&7あなたはワールド&8[&c{0}&&8]&7にいます。", p.getWorld().getName()));
        return true;
    }
}

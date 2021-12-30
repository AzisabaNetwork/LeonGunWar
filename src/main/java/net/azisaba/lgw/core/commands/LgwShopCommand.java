package net.azisaba.lgw.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.azisaba.lgw.core.utils.Chat;

public class LgwShopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(command.getName().equalsIgnoreCase("lgwshop")){

            if(sender instanceof Player ){

            }else {
                sender.sendMessage(Chat.f("&cこのコマンドはクライアントから実行してください！！！"));
                return true;
            }

        }

        return true;
    }
}

package net.azisaba.lgw.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.azisaba.lgw.core.GUI.AngelOfDeathGUI;
import net.azisaba.lgw.core.LeonGunWar;

public class AngelOfDeathCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(command.getName().equalsIgnoreCase("angelOfDeath")){

            if( !LeonGunWar.getPlugin().isLobby() ){
                sender.sendMessage(ChatColor.RED + "You are not allowed to use this command on this server!");
                return true;
            }

            if(args.length == 0){

                new AngelOfDeathGUI((Player)sender).shop();

            }

        }

        return true;
    }
}

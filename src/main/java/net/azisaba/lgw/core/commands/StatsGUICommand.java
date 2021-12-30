package net.azisaba.lgw.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.azisaba.lgw.core.GUI.StatsGUI;

public class StatsGUICommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("StatsGUI")){

            new StatsGUI((Player)sender);

        }
        return true;
    }
}

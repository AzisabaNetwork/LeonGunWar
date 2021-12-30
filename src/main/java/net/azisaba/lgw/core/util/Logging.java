package net.azisaba.lgw.core.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Logging {

    private static final String ERROR_ONE = "Failed to send you (ERROR-1 - QUEUE NOT FOUND)";
    private static final String ERROR_TWO = "Failed to send you (ERROR-2 - QUEUE IS NOT LOADED)";

    public static void info(String message){
        Bukkit.getLogger().info(message);
    }

    public static void warn(String message){
        Bukkit.getLogger().warning(message);
    }

    public static void error(String message, Player player){

        warn(message);

        player.sendMessage(ChatColor.RED + message);

    }

    public static void error(int id,Player player){

        String message = "";

        switch ( id ){

            case 1: message = ERROR_ONE;
            case 2: message = ERROR_TWO;
            default: message = "";

        }

        error(message,player);

    }


}

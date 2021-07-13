package net.azisaba.lgw.core.util;

import org.bukkit.Bukkit;

public class Logging {

    public static void info(String message){
        Bukkit.getLogger().info(message);
    }

    public static void warn(String message){
        Bukkit.getLogger().warning(message);
    }

}

package net.azisaba.lgw.core.util;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

import java.text.MessageFormat;

@UtilityClass
public class Chat {

    // メッセージをフォーマットして、&で色をつける
    public static String f(String text, Object... args) {
        //return MessageFormat.format(ChatColor.translateAlternateColorCodes('&', text), args);
        return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(text, args));
    }

    // 色を消す
    public String r(String text) {
        return ChatColor.stripColor(text);
    }
}

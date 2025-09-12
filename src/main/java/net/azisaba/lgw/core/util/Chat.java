package net.azisaba.lgw.core.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;

import java.text.MessageFormat;

@UtilityClass
public class Chat {

    // メッセージをフォーマットして、&で色をつける
    public static String f(String text, Object... args) {
        return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(text, args));
    }

    // 色を消す
    public String r(String text) {
        return ChatColor.stripColor(text);
    }

    //fのComponent版
    public static Component c(String str) {
        return Component.text(ChatColor.translateAlternateColorCodes('&', str));
    }
}

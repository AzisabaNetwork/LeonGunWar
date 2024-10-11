package net.azisaba.lgw.core.utils;

import java.text.MessageFormat;

import org.bukkit.ChatColor;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Chat {

    // メッセージをフォーマットして、&で色をつける
    public static String f(String text, Object... args) {

        // String型の引数は装飾コードを適用する
        Object[] formattedArgs = new Object[args.length];
        for(int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                formattedArgs[i] = f(ChatColor.translateAlternateColorCodes('&', args[i].toString()));
            }
            else {
                formattedArgs[i] = args[i];
            }
        }
        return MessageFormat.format(ChatColor.translateAlternateColorCodes('&', text), formattedArgs);
    }

    // 色を消す
    public String r(String text) {
        return ChatColor.stripColor(text);
    }
}

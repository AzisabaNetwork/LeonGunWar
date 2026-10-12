package net.azisaba.lgw.core.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Normalize compact HEX colors to the repeated legacy form understood by Bukkit and chat plugins. */
public final class LegacyColors {
    private static final Pattern HEX = Pattern.compile("(?i)&(?:x|#)([0-9a-f]{6})");
    private LegacyColors() { }

    public static String normalize(String text) {
        Matcher matcher = HEX.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            StringBuilder code = new StringBuilder("&x");
            for (char digit : matcher.group(1).toCharArray()) code.append('&').append(digit);
            matcher.appendReplacement(result, Matcher.quoteReplacement(code.toString()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', normalize(text));
    }
}

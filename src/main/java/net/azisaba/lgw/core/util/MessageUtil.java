package net.azisaba.lgw.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MessageUtil {
    public static Component getAbleComponent(String name, boolean state) {
        if (state) {
            return Component.text(name + "が可能になりました").color(NamedTextColor.GREEN);
        } else {
            return Component.text(name + "が不可能になりました").color(NamedTextColor.RED);
        }
    }
}

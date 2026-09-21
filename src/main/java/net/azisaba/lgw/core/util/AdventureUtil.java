package net.azisaba.lgw.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public final class AdventureUtil {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private AdventureUtil() {
    }

    public static Component legacy(String message) {
        return LEGACY.deserialize(message);
    }

    public static void actionBar(Player player, String message) {
        player.sendActionBar(legacy(message));
    }
}

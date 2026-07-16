package net.azisaba.lgw.core.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@UtilityClass
public class KillLogUtils {

    public static Component createDistanceText(@Nullable Player killer, Player victim) {
        Component label = Component.text("キル距離: ").color(NamedTextColor.AQUA);
        if (killer == null || killer.getWorld() != victim.getWorld()) {
            return label.append(Component.text("計測不能").color(NamedTextColor.GRAY));
        }

        double distance = killer.getLocation().distance(victim.getLocation());
        String formattedDistance = String.format(Locale.ROOT, "%.1f ブロック", distance);
        return label.append(Component.text(formattedDistance).color(NamedTextColor.WHITE));
    }
}

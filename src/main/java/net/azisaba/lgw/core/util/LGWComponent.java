package net.azisaba.lgw.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class LGWComponent {
    // functional area
    public static Component surrounded(NamedTextColor surroundColor, Component textComponent) {
        return Component.join(JoinConfiguration.noSeparators(),
                Component.text("[").color(surroundColor),
                textComponent,
                Component.text("[").color(surroundColor));
    }

    // definition area
    public static Component QUICK_BAR = Component.join(
            JoinConfiguration.spaces(),
            LGWComponent.surrounded(
                    NamedTextColor.GRAY,
                    Component.text("Quick").color(NamedTextColor.AQUA)
            ),
            Component.text("ここをクリック →"),
            Component.text("[エントリー]")
                    .color(NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand("/leongunwar:match entry")),
            Component.text("[エントリー解除]")
                    .color(NamedTextColor.RED)
                    .clickEvent(ClickEvent.runCommand("/leongunwar:match leave")),
            Component.text("[途中参加]")
                    .color(NamedTextColor.GOLD)
                    .clickEvent(ClickEvent.runCommand("/leongunwar:match rejoin"))
    );
}

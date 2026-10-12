package net.azisaba.lgw.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LegacyColorsTest {
    private static TextColor firstColor(Component component) {
        if (component.color() != null) return component.color();
        for (var child : component.children()) { var color = firstColor(child); if (color != null) return color; }
        return null;
    }
    @Test void allHexFormsRenderAsExactRgbInGuiAndExternalAmpersandChat() {
        for (String text : List.of("&x&B&5&D&F&E&8称号", "&xB5DFE8称号", "&#B5DFE8称号", "§x§b§5§d§f§e§8称号")) {
            assertEquals(TextColor.color(0xB5DFE8), firstColor(AdventureUtil.legacy(LegacyColors.color(text))), text);
            var chat = LegacyComponentSerializer.legacyAmpersand().deserialize(LegacyColors.normalize(text.replace('§', '&')));
            assertEquals(TextColor.color(0xB5DFE8), firstColor(chat), text);
        }
    }
    @Test void existingNamedColorsAndPlainTextRemainUsable() {
        assertEquals(NamedTextColor.GOLD, firstColor(AdventureUtil.legacy(LegacyColors.color("&6[古参]"))));
        assertEquals("色なし", LegacyColors.color("色なし"));
        assertEquals("&xZZZZZZ", LegacyColors.normalize("&xZZZZZZ"));
    }
}

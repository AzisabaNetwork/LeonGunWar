package net.azisaba.lgw.core.util;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Color;

/**
 * チームを表現するためだけに作られたEnumクラス
 *
 * @author siloneco
 *
 */
public enum BattleTeam {

    RED("赤チーム", Color.fromRGB(0x930000), ChatColor.DARK_RED, NamedTextColor.RED),
    BLUE("青チーム", Color.fromRGB(0x0000A0), ChatColor.BLUE, NamedTextColor.BLUE);

    private final String name;
    private final Color color;
    private final ChatColor chatColor;
    private final NamedTextColor namedtextcolor;


    BattleTeam(String name, Color color, ChatColor chatColor, NamedTextColor namedtextcolor) {
        this.name = name;
        this.color = color;
        this.chatColor = chatColor;
        this.namedtextcolor = namedtextcolor;
    }

    public String getTeamName() {
        return chatColor + name;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public Color getColor() {
        return color;
    }
    public NamedTextColor getNamedTextColor() {
        return namedtextcolor;
    }
}

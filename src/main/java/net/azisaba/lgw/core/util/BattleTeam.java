package net.azisaba.lgw.core.util;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * チームを表現するためだけに作られたEnumクラス
 *
 * @author siloneco
 *
 */
public enum BattleTeam {

    RED("赤チーム", Color.fromRGB(0x930000), ChatColor.DARK_RED, NamedTextColor.DARK_RED , "red"),
    BLUE("青チーム", Color.fromRGB(0x0000A0), ChatColor.BLUE, NamedTextColor.BLUE, "blue");

    private final String name;
    @Getter
    private final Color color;
    @Getter
    private final ChatColor chatColor;
    private final NamedTextColor namedtextcolor;
    @Getter
    private final String engTeamName;


    BattleTeam(String name, Color color, ChatColor chatColor, NamedTextColor namedtextcolor, String engTeamName) {
        this.name = name;
        this.color = color;
        this.chatColor = chatColor;
        this.namedtextcolor = namedtextcolor;
        this.engTeamName = engTeamName;
    }

    public String getTeamName() {
        return chatColor + name;
    }

    public NamedTextColor getNamedTextColor() {
        return namedtextcolor;
    }
}

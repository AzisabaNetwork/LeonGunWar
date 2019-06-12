package net.azisaba.lgw.core.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * チームを表現するためだけに作られたEnumクラス
 * @author siloneco
 *
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BattleTeam {

	RED("赤チーム", Color.fromRGB(0x930000), ChatColor.DARK_RED, "Red"),
	BLUE("青チーム", Color.fromRGB(0x0000A0), ChatColor.BLUE, "Blue");

	private final String teamName;
	private final Color teamColor;
	private final ChatColor chatColor;
	private final String scoreboardTeamName;

	public String getDisplayTeamName() {
		return chatColor + teamName;
	}
}

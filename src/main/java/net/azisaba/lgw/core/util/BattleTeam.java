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

	RED("赤チーム", Color.fromRGB(0x930000), ChatColor.DARK_RED),
	BLUE("青チーム", Color.fromRGB(0x0000A0), ChatColor.BLUE);

	private final String teamName;
	private final Color teamColor;
	private final ChatColor chatColor;

//	private final String displayTeamName = getChatColor() + getTeamName();

	public String getDisplayTeamName() {
		return getChatColor() + getTeamName();
	}
}

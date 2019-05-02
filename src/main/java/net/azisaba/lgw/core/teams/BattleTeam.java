package net.azisaba.lgw.core.teams;

import org.bukkit.ChatColor;
import org.bukkit.Color;

/**
 * チームを表現するためだけに作られたEnumクラス
 * @author siloneco
 *
 */
public enum BattleTeam {

	RED(ChatColor.RED + "赤チーム", Color.fromRGB(0x930000)),
	BLUE(ChatColor.BLUE + "青チーム", Color.fromRGB(0x0000A0));

	private final String teamName;
	private final Color teamColor;

	private BattleTeam(String teamName, Color teamColor) {
		this.teamName = teamName;
		this.teamColor = teamColor;
	}

	public String getTeamName() {
		return teamName;
	}

	public Color getTeamColor() {
		return teamColor;
	}
}

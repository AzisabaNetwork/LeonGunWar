package net.azisaba.lgw.core.teams;

import org.bukkit.Color;

/**
 * チームを表現するためだけに作られたEnumクラス
 * @author siloneco
 *
 */
public enum BattleTeam {

	RED(Color.fromRGB(0x930000)), BLUE(Color.fromRGB(0x0000A0));

	private final Color teamColor;

	private BattleTeam(Color teamColor) {
		this.teamColor = teamColor;
	}

	public Color getTeamColor() {
		return teamColor;
	}
}

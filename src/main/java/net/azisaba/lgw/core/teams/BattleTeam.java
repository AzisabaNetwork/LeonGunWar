package net.azisaba.lgw.core.teams;

import org.bukkit.ChatColor;
import org.bukkit.Color;

/**
 * チームを表現するためだけに作られたEnumクラス
 * @author siloneco
 *
 */
public enum BattleTeam {

	RED(ChatColor.RED + "赤チーム", Color.fromRGB(0x930000), ChatColor.DARK_RED), BLUE(ChatColor.BLUE + "青チーム",
			Color.fromRGB(0x0000A0), ChatColor.DARK_BLUE);

	private final String teamName;
	private final Color teamColor;
	private final ChatColor chatColor;

	private BattleTeam(String teamName, Color teamColor, ChatColor chatColor) {
		this.teamName = teamName;
		this.teamColor = teamColor;
		this.chatColor = chatColor;
	}

	public String getTeamName() {
		return teamName;
	}

	public Color getTeamColor() {
		return teamColor;
	}

	public ChatColor getChatColor() {
		return chatColor;
	}
}

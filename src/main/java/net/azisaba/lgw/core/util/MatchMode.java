package net.azisaba.lgw.core.util;

import net.azisaba.lgw.core.utils.Chat;

public enum MatchMode {

	TEAM_DEATH_MATCH(Chat.f("&9チームデスマッチ"), Chat.f("&7先に &a50キル &7で勝利")),
	LEADER_DEATH_MATCH(Chat.f("&dリーダーデスマッチ"), Chat.f("&7相手チームの &dリーダー &7を倒して勝利"));

	private final String modeName;
	private final String description;

	private MatchMode(String modeName, String description) {
		this.modeName = modeName;
		this.description = description;
	}

	public static MatchMode getFromString(String msg) {
		String msgNoSpace = msg.replace(" ", "");
		switch (msgNoSpace.toLowerCase()) {
		case "ldm":
		case "leaderdeathmatch":
			return LEADER_DEATH_MATCH;
		case "tdm":
		case "teamdeathmatch":
			return TEAM_DEATH_MATCH;
		default:
			return null;
		}
	}

	public String getModeName() {
		return modeName;
	}

	public String getDescription() {
		return description;
	}
}

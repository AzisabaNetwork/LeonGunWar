package net.azisaba.lgw.core.util;

import net.azisaba.lgw.core.utils.Chat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MatchMode {

	TEAM_DEATH_MATCH(Chat.f("&9チームデスマッチ"), Chat.f("&9TDM"), Chat.f("&7先に &a50キル &7で勝利")),
	LEADER_DEATH_MATCH(Chat.f("&dリーダーデスマッチ"), Chat.f("&dLDM"), Chat.f("&7相手チームの &dリーダー &7を倒して勝利")),
	TEAM_DEATH_MATCH_NOLIMIT(Chat.f("&6上限なしチームデスマッチ"), Chat.f("&6TDM-NOLIMIT"), Chat.f("&7終了時に &cキル数が多いチーム &7が勝利"));

	private final String modeName;
	private final String shortModeName;
	private final String description;

	public static MatchMode getFromString(String text) {
		switch (text.replace(" ", "").toLowerCase()) {
		case "ldm":
		case "leaderdeathmatch":
		case "leader":
			return LEADER_DEATH_MATCH;
		case "tdm":
		case "teamdeathmatch":
		case "team":
			return TEAM_DEATH_MATCH;
		case "nolimit":
		case "no-limit":
		case "teamdeathmatchnolimit":
		case "tdm-nolimit":
		case "tdm-no-limit":
		case "team-no-limit":
		case "team-nolimit":
			return TEAM_DEATH_MATCH_NOLIMIT;
		default:
			return null;
		}
	}
}

package net.azisaba.lgw.core.util;

import java.util.UUID;

/**
 *
 * キル数とデス数、プレイヤーデータを格納したクラス
 * @author siloneco
 *
 */
public class KDPlayerData {

	private final String playerName;
	private final UUID uuid;
	private final int kills;
	private final int deaths;
	private final int assists;

	public KDPlayerData(UUID uuid, String playerName, int kills, int deaths, int assists) {
		this.playerName = playerName;
		this.uuid = uuid;
		this.kills = kills;
		this.deaths = deaths;
		this.assists = assists;
	}

	public String getPlayerName() {
		return playerName;
	}

	public UUID getUuid() {
		return uuid;
	}

	public int getKills() {
		return kills;
	}

	public int getDeaths() {
		return deaths;
	}

	public int getAssists() {
		return assists;
	}
}
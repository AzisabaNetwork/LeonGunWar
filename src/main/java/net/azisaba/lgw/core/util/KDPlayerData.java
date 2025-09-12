package net.azisaba.lgw.core.util;

import lombok.Data;

import java.util.UUID;

/**
 * キル数とデス数、プレイヤーデータを格納したクラス
 *
 * @author siloneco
 */
@Data
public class KDPlayerData {

    private final UUID uuid;
    private final String playerName;
    private final int kills;
    private final int deaths;
    private final int assists;

    public KDPlayerData(UUID uuid, String playerName, int kills, int deaths, int assists) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
    }
}

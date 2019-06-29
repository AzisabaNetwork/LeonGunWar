package net.azisaba.lgw.core.util;

import java.util.UUID;

import lombok.Data;

/**
 *
 * キル数とデス数、プレイヤーデータを格納したクラス
 * 
 * @author siloneco
 *
 */
@Data
public class KDPlayerData {

    private final UUID uuid;
    private final String playerName;
    private final int kills;
    private final int deaths;
    private final int assists;
}

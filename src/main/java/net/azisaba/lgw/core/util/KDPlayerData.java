package net.azisaba.lgw.core.util;

import java.util.UUID;

/**
 * キル数とデス数、プレイヤーデータを格納したクラス
 *
 * @author siloneco
 */
public record KDPlayerData(UUID uuid, String playerName, int kills, int deaths, int assists) {}

package net.azisaba.lgw.core;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

/**
 *
 * KDを保存するクラスです
 * @author siloneco
 *
 */
public class KillDeathCounter {

	// キル数とデス数をカウントするHashMap
	private HashMap<UUID, Integer> killCountMap, deathCountMap;

	public KillDeathCounter() {
		killCountMap = new HashMap<>();
		deathCountMap = new HashMap<>();
	}

	/**
	 * プレイヤーのキル数を1追加します
	 * @param player キル数を追加したいプレイヤー
	 *
	 * @exception IllegalArgumentException playerがnullの場合
	 */
	public void addKill(Player player) {
		// playerがnullの場合 IllegalArgumentException
		if (player == null) {
			throw new IllegalArgumentException("\"player\" mustn't be null.");
		}

		// すでにカウントされている場合は取得、なければデフォルト値である 0 を設定
		int kill = killCountMap.getOrDefault(player.getUniqueId(), 0);

		// キル追加
		kill++;

		// HashMapにセット
		killCountMap.put(player.getUniqueId(), kill);
	}

	/**
	 * プレイヤーのキル数を取得します
	 * @param player キル数を取得したいプレイヤー
	 * @return プレイヤーのキル数
	 *
	 * @exception IllegalArgumentException playerがnullの場合
	 */
	public int getKills(Player player) {
		// playerがnullの場合 IllegalArgumentException
		if (player == null) {
			throw new IllegalArgumentException("\"player\" mustn't be null.");
		}

		// プレイヤーのキル数を返す。キーが含まれていない場合はデフォルト値である 0 を返す
		return killCountMap.getOrDefault(player.getUniqueId(), 0);
	}

	/**
	 * プレイヤーのデス数を1追加します
	 * @param player デス数を追加したいプレイヤー
	 *
	 * @exception IllegalArgumentException playerがnullの場合
	 */
	public void addDeath(Player player) {
		// playerがnullの場合 IllegalArgumentException
		if (player == null) {
			throw new IllegalArgumentException("\"player\" mustn't be null.");
		}

		// すでにカウントされている場合は取得、なければデフォルト値である 0 を設定
		int death = deathCountMap.getOrDefault(player.getUniqueId(), 0);

		// デス追加
		death++;

		// HashMapにセット
		deathCountMap.put(player.getUniqueId(), death);
	}

	/**
	 * プレイヤーのデス数を取得します
	 * @param player デス数を取得したいプレイヤー
	 * @return プレイヤーのデス数
	 *
	 * @exception IllegalArgumentException playerがnullの場合
	 */
	public int getDeaths(Player player) {
		// playerがnullの場合 IllegalArgumentException
		if (player == null) {
			throw new IllegalArgumentException("\"player\" mustn't be null.");
		}

		// プレイヤーのデス数を返す。キーが含まれていない場合はデフォルト値である 0 を返す
		return deathCountMap.getOrDefault(player.getUniqueId(), 0);
	}
}

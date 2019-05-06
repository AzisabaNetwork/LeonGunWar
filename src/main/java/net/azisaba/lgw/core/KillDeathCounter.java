package net.azisaba.lgw.core;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

/**
 *
 * KDを保存するクラスです
 * @author siloneco
 *
 */
public class KillDeathCounter {

	// キル数とデス数をカウントするHashMap
	private final HashMap<UUID, Integer> killCountMap = new HashMap<>(), deathCountMap = new HashMap<>();
	private final HashMap<UUID, String> playerNameContainer = new HashMap<>();

	/**
	 * プレイヤーのキル数を1追加します
	 * @param player キル数を追加したいプレイヤー
	 *
	 * @exception IllegalArgumentException playerがnullの場合
	 */
	public void addKill(Player player) {
		// playerがnullの場合 IllegalArgumentException
		Preconditions.checkNotNull(player, "\"player\" mustn't be null.");

		// プレイヤー情報を保存
		updatePlayerName(player);

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
		Preconditions.checkNotNull(player, "\"player\" mustn't be null.");

		// プレイヤー情報を保存
		updatePlayerName(player);

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
		Preconditions.checkNotNull(player, "\"player\" mustn't be null.");

		// プレイヤー情報を保存
		updatePlayerName(player);

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
		Preconditions.checkNotNull(player, "\"player\" mustn't be null.");

		// プレイヤー情報を保存
		updatePlayerName(player);

		// プレイヤーのデス数を返す。キーが含まれていない場合はデフォルト値である 0 を返す
		return deathCountMap.getOrDefault(player.getUniqueId(), 0);
	}

	/**
	 * もっとも試合に貢献したプレイヤーをリストで取得します (2人以上いることがあるため)
	 * 存在しない場合はnullを返します
	 * @return MVPのKDPlayerData
	 */
	public List<KDPlayerData> getMVPPlayer() {

		int mvpKills = killCountMap.values().stream()
				.max(Comparator.reverseOrder())
				.get();

		return killCountMap.entrySet().stream()
				.filter(entry -> entry.getValue() == mvpKills)
				.map(Entry::getKey)
				.map(mvp -> {
					// プレイヤー名取得 (なければnull)
					String playerName = playerNameContainer.getOrDefault(mvp, null);
					// キル数取得 (なければ0)
					int kills = killCountMap.getOrDefault(mvp, 0);
					// デス数取得 (なければ0)
					int deaths = deathCountMap.getOrDefault(mvp, 0);

					// KDPlayerData作成
					return new KDPlayerData(mvp, playerName, kills, deaths);
				})
				.collect(Collectors.toList());
	}

	/**
	 * プレイヤーのUUIDと名前を紐づけます
	 * プレイヤーがログアウトした後にプレイヤー名とキル数を紐づけるために使用されます
	 * @param player 情報を更新したいプレイヤー
	 */
	private void updatePlayerName(Player player) {
		playerNameContainer.put(player.getUniqueId(), player.getName());
	}

	/**
	 *
	 * キル数とデス数、プレイヤーデータを格納したクラス
	 * @author siloneco
	 *
	 */
	public class KDPlayerData {

		private String playerName;
		private UUID uuid;
		private int kills;
		private int deaths;

		private KDPlayerData(UUID uuid, String playerName, int kills, int deaths) {
			this.playerName = playerName;
			this.uuid = uuid;
			this.kills = kills;
			this.deaths = deaths;
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
	}
}

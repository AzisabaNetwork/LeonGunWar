package net.azisaba.lgw.core;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

import net.azisaba.lgw.core.util.KDPlayerData;

/**
 *
 * KDを保存するクラスです
 * @author siloneco
 *
 */
public class KillDeathCounter {

	// キル数とデス数とアシスト数をカウントするHashMap
	private final Map<UUID, Integer> killCountMap = new HashMap<>(), deathCountMap = new HashMap<>(),
			assistCountMap = new HashMap<>();
	// UUIDとプレイヤー名を紐付けるためのHashMap
	private final Map<UUID, String> playerNameContainer = new HashMap<>();

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
	 * プレイヤーのアシスト数を1追加します
	 * @param player アシスト数を追加したいプレイヤー
	 *
	 * @exception IllegalArgumentException playerがnullの場合
	 */
	public void addAssist(Player player) {
		// playerがnullの場合 IllegalArgumentException
		Preconditions.checkNotNull(player, "\"player\" mustn't be null.");

		// プレイヤー情報を保存
		updatePlayerName(player);

		// すでにカウントされている場合は取得、なければデフォルト値である 0 を設定
		int assist = assistCountMap.getOrDefault(player.getUniqueId(), 0);

		// アシスト追加
		assist++;

		// HashMapにセット
		assistCountMap.put(player.getUniqueId(), assist);
	}

	/**
	 * プレイヤーのアシスト数を取得します
	 * @param player アシスト数を取得したいプレイヤー
	 * @return プレイヤーのアシスト数
	 *
	 * @exception IllegalArgumentException playerがnullの場合
	 */
	public int getAssists(Player player) {
		// playerがnullの場合 IllegalArgumentException
		Preconditions.checkNotNull(player, "\"player\" mustn't be null.");

		// プレイヤー情報を保存
		updatePlayerName(player);

		// プレイヤーのアシスト数を返す。キーが含まれていない場合はデフォルト値である 0 を返す
		return assistCountMap.getOrDefault(player.getUniqueId(), 0);
	}

	/**
	 * もっとも試合に貢献したプレイヤーをリストで取得します (2人以上いることがあるため)
	 * 存在しない場合は空のリストを返します
	 * @return MVPのKDPlayerDataをList形式で
	 */
	public List<KDPlayerData> getMVPPlayer() {
		// キルカウントMapから最大キル数を取得、ない場合は-1
		int mvpKills = killCountMap.values().stream()
				.max(Comparator.naturalOrder())
				.orElse(-1);

		// mvpKillsと同じキル数のプレイヤーだけ抽出して、KDPlayerDataに変換しList形式で取得
		// mvpKillsが-1の場合は空のリストを返す
		return killCountMap.entrySet().stream()
				.filter(entry -> entry.getValue() == mvpKills)
				.map(Map.Entry::getKey)
				.map(mvp -> {
					// プレイヤー名取得 (なければnull)
					String playerName = playerNameContainer.getOrDefault(mvp, null);
					// キル数取得 (なければ0)
					int kills = killCountMap.getOrDefault(mvp, 0);
					// デス数取得 (なければ0)
					int deaths = deathCountMap.getOrDefault(mvp, 0);
					// アシスト数取得 (なければ0)
					int assists = assistCountMap.getOrDefault(mvp, 0);

					// KDPlayerData作成
					return new KDPlayerData(mvp, playerName, kills, deaths, assists);
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
}

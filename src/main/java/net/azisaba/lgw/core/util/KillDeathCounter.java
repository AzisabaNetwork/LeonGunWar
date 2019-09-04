package net.azisaba.lgw.core.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Strings;

import net.azisaba.lgw.core.events.PlayerAssistEvent;
import net.azisaba.lgw.core.utils.Chat;

import lombok.Getter;
import lombok.NonNull;

/**
 *
 * KDを保存するクラスです
 *
 * @author siloneco
 *
 */
public class KillDeathCounter {

    // キル数とデス数とアシスト数をカウントするHashMap
    private final Map<UUID, Integer> killCountMap = new HashMap<>();
    private final Map<UUID, Integer> deathCountMap = new HashMap<>();
    private final Map<UUID, Integer> assistCountMap = new HashMap<>();
    // アクションバーのバーを保存するHashMap
    private final Map<UUID, String> actionBarMap = new HashMap<>();
    // UUIDとプレイヤー名を紐付けるためのHashMap
    private final Map<UUID, String> playerNameContainer = new HashMap<>();

    // 何もデータがない時のアクションバー
    @Getter
    private final String defaultActionBar = Chat.f("&6&l0 &rKill(s) &7[ &r{0} &7] &6&l0 &rDeath(s) &7&l/ &6&l0 &rAssist(s) &7&l/ &3&l0.000 &rKD", Strings.repeat("┃", 50));

    /**
     * プレイヤーのキル数を1追加します
     *
     * @param player キル数を追加したいプレイヤー
     *
     * @exception NullPointerException playerがnullの場合
     */
    public void addKill(@NonNull Player player) {
        // プレイヤー情報を保存
        updatePlayerName(player);

        // すでにカウントされている場合は取得、なければデフォルト値である 0 を設定
        int kill = killCountMap.getOrDefault(player.getUniqueId(), 0);

        // キル追加
        kill++;

        // HashMapにセット
        killCountMap.put(player.getUniqueId(), kill);

        // アクションバーを更新する
        updateActionbar(player);
    }

    /**
     * プレイヤーのキル数を取得します
     *
     * @param player キル数を取得したいプレイヤー
     * @return プレイヤーのキル数
     *
     * @exception NullPointerException playerがnullの場合
     */
    public int getKills(@NonNull Player player) {
        // プレイヤー情報を保存
        updatePlayerName(player);

        // プレイヤーのキル数を返す。キーが含まれていない場合はデフォルト値である 0 を返す
        return killCountMap.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * プレイヤーのデス数を1追加します
     *
     * @param player デス数を追加したいプレイヤー
     *
     * @exception NullPointerException playerがnullの場合
     */
    public void addDeath(@NonNull Player player) {
        // プレイヤー情報を保存
        updatePlayerName(player);

        // すでにカウントされている場合は取得、なければデフォルト値である 0 を設定
        int death = deathCountMap.getOrDefault(player.getUniqueId(), 0);

        // デス追加
        death++;

        // HashMapにセット
        deathCountMap.put(player.getUniqueId(), death);

        // アクションバーを更新する
        updateActionbar(player);
    }

    /**
     * プレイヤーのデス数を取得します
     *
     * @param player デス数を取得したいプレイヤー
     * @return プレイヤーのデス数
     *
     * @exception NullPointerException playerがnullの場合
     */
    public int getDeaths(@NonNull Player player) {
        // プレイヤー情報を保存
        updatePlayerName(player);

        // プレイヤーのデス数を返す。キーが含まれていない場合はデフォルト値である 0 を返す
        return deathCountMap.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * プレイヤーのアシスト数を1追加します
     *
     * @param player アシスト数を追加したいプレイヤー
     *
     * @exception NullPointerException playerがnullの場合
     */
    public void addAssist(@NonNull Player player) {
        // プレイヤー情報を保存
        updatePlayerName(player);

        // すでにカウントされている場合は取得、なければデフォルト値である 0 を設定
        int assist = assistCountMap.getOrDefault(player.getUniqueId(), 0);

        // アシスト追加
        assist++;

        // HashMapにセット
        assistCountMap.put(player.getUniqueId(), assist);

        // アクションバーを更新する
        updateActionbar(player);

        // イベントの呼び出し
        PlayerAssistEvent event = new PlayerAssistEvent(player);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * プレイヤーのアシスト数を取得します
     *
     * @param player アシスト数を取得したいプレイヤー
     * @return プレイヤーのアシスト数
     *
     * @exception NullPointerException playerがnullの場合
     */
    public int getAssists(@NonNull Player player) {
        // プレイヤー情報を保存
        updatePlayerName(player);

        // プレイヤーのアシスト数を返す。キーが含まれていない場合はデフォルト値である 0 を返す
        return assistCountMap.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * もっとも試合に貢献したプレイヤーをリストで取得します (2人以上いることがあるため) 存在しない場合は空のリストを返します
     *
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
     * プレイヤーのアクションバーに表示する内容を取得します
     *
     * @param player 対象プレイヤー
     * @return アクションバーに表示する内容。データがない場合はnull
     */
    public String getActionBar(@NonNull Player player) {
        return actionBarMap.getOrDefault(player.getUniqueId(), null);
    }

    /**
     * プレイヤーのUUIDと名前を紐づけます プレイヤーがログアウトした後にプレイヤー名とキル数を紐づけるために使用されます
     *
     * @param player 情報を更新したいプレイヤー
     */
    private void updatePlayerName(Player player) {
        playerNameContainer.put(player.getUniqueId(), player.getName());
    }

    /**
     * プレイヤーのアクションバーに表示する内容を更新します。これはプレイヤーのアクションバーを更新するのではなく、{@link #actionBarMap}
     * の内容を更新するメソッドです
     *
     * @param player 対象プレイヤー
     */
    private void updateActionbar(Player player) {

        StringBuilder barBuilder = new StringBuilder();
        StringBuilder actionBar = new StringBuilder();

        int kills = getKills(player);
        int deaths = getDeaths(player);
        int assists = getAssists(player);
        // KDレート算出
        double kdRatio = kills;
        if ( deaths > 0 ) {
            kdRatio = (double) kills / (double) deaths;
        }

        // kills + deathsが0より多い場合はバーを作成
        if ( kills + deaths > 0 ) {
            // キルのパーセンテージ
            double killsPercentage = (double) kills / (double) (kills + deaths) * 100d;
            // デスのパーセンテージ
            double deathsPercentage = (double) deaths / (double) (kills + deaths) * 100d;

            barBuilder.append(Chat.f("&d{0}", Strings.repeat("┃", (int) killsPercentage / 2)));
            barBuilder.append(Chat.f("&5{0}", Strings.repeat("┃", (int) deathsPercentage / 2)));

            // キル数とデス数を数字で表示
            actionBar.append(Chat.f("&6&l{0} &rKill(s) &7[ &r{1} &7] &6&l{2} &rDeath(s)", kills, barBuilder.toString(), deaths));
        } else {
            // それ以外なら白いバーを作成
            actionBar.append(Chat.f("&6&l{0} &rKill(s) &7[ &r{1} &7] &6&l{2} &rDeath(s)", 0, Strings.repeat("┃", 50), 0));
        }

        // アシスト数とKDレートを表示
        actionBar.append(Chat.f(" &7&l/ &6&l{0} &rAssist(s) &7&l/ &3&l{1} &rKD", assists, String.format("%.3f", kdRatio)));

        // HashMapに設定
        actionBarMap.put(player.getUniqueId(), actionBar.toString());
    }
}

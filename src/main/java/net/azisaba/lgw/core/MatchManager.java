package net.azisaba.lgw.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.azisaba.lgw.core.distributors.KDTeamDistributor_BETA;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import net.azisaba.lgw.core.distributors.DefaultTeamDistributor;
import net.azisaba.lgw.core.distributors.TeamDistributor;
import net.azisaba.lgw.core.events.PlayerEntryMatchEvent;
import net.azisaba.lgw.core.events.PlayerKickMatchEvent;
import net.azisaba.lgw.core.events.PlayerLeaveEntryMatchEvent;
import net.azisaba.lgw.core.events.TeamPointIncreasedEvent;
import net.azisaba.lgw.core.tasks.MatchCountdownTask;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.util.KillDeathCounter;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.CustomItem;
import net.azisaba.playersettings.PlayerSettings;
import net.azisaba.playersettings.util.SettingsData;

import lombok.Data;
import lombok.NonNull;

/**
 *
 * ゲームを司るコアクラス
 *
 * @author siloneco
 *
 */
@Data
public class MatchManager {

    private boolean initialized = false;

    // チーム分けを行うクラス
    private TeamDistributor teamDistributor;

    // ゲーム中かどうかの判定
    private boolean isMatching = false;
    // 現在のマップ
    private GameMap currentGameMap = null;
    // 試合の残り時間
    private final AtomicInteger timeLeft = new AtomicInteger(0);
    // 試合を動かすタスク
    private BukkitTask matchTask;
    // KDカウンター
    private KillDeathCounter killDeathCounter;

    // マッチで使用するスコアボード
    private Scoreboard scoreboard;
    // スコアボードチーム
    private final Map<BattleTeam, Team> teams = new HashMap<>();
    // 試合に参加するプレイヤーのリスト
    private final List<Player> entryPlayers = new ArrayList<>();
    // チェストプレート
    private final Map<BattleTeam, ItemStack> chestplates = new HashMap<>();

    // ポイントを集計するHashMap
    private final Map<BattleTeam, Integer> pointMap = new HashMap<>();

    // 試合の種類
    private MatchMode matchMode = null;
    // チームのリーダー
    private final Map<BattleTeam, Player> ldmLeaderMap = new HashMap<>();

    // 試合時間を表示するボスバー
    private BossBar bossBar = null;

    /**
     * 初期化メソッド Pluginが有効化されたときのみ呼び出されることを前提としています
     */
    protected void initialize() {
        // すでに初期化されている場合はreturn
        if ( initialized ) {
            return;
        }

        // killDeathCounterを新規作成
        killDeathCounter = new KillDeathCounter();

        // デフォルトのTeamDistributorを指定
        teamDistributor = new DefaultTeamDistributor();
        // メインではない新しいスコアボードを取得
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // ScoreboardDisplayerにScoreboardを設定
        LeonGunWar.getPlugin().getScoreboardDisplayer().setScoreBoard(scoreboard);

        // 各スコアボードチームの取得 / 作成
        initializeTeams();

        // 全プレイヤーのスコアボードを変更
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.setScoreboard(scoreboard);
        });

        // 各チームのチェストプレートを設定
        Arrays.stream(BattleTeam.values())
                .forEach(team -> chestplates.put(team, CustomItem.getTeamChestplate(team)));

        initialized = true;
    }

    /**
     * マッチを開始するメソッド
     *
     * @exception IllegalStateException すでにゲームがスタートしている場合
     */
    public void startMatch() {
        // すでにマッチ中の場合はIllegalStateException
        Preconditions.checkState(!isMatching, "A match is already started.");

        // timeLeftを600に変更
        timeLeft.set(600);

        // ボスバー作成
        bossBar = Bukkit.createBossBar("", BarColor.PINK, BarStyle.SOLID);

        // マップを抽選
        currentGameMap = LeonGunWar.getPlugin().getMapsConfig().getRandomMap();
        // マップ名を表示
        Bukkit.broadcastMessage(
                Chat.f("{0}&7今回のマップは &b{1} &7です！", LeonGunWar.GAME_PREFIX, currentGameMap.getMapName()));

        // 参加プレイヤーを取得
        List<Player> entryPlayers = getEntryPlayers();
        // プレイヤーを振り分け
        teamDistributor.distributePlayers(entryPlayers, new ArrayList<>(teams.values()));

        // 各プレイヤーにチームに沿った処理を行う
        // エントリー削除したときにgetEntries()の中身が変わってエラーを起こさないように新しいリストを作成してfor文を使用する
        for ( BattleTeam team : BattleTeam.values() ) {
            Team scoreboardTeam = getScoreboardTeam(team);
            if ( scoreboardTeam == null ) {
                continue;
            }

            for ( String entry : new ArrayList<>(scoreboardTeam.getEntries()) ) {
                Player player = Bukkit.getPlayerExact(entry);

                // プレイヤーが見つからない場合はエントリーから削除してcontinue
                if ( player == null ) {
                    scoreboardTeam.removeEntry(entry);
                    continue;
                }

                // セットアップ
                setUpPlayer(player, team);
            }
        }

        // LDMならリーダーを抽選
        if ( matchMode == MatchMode.LEADER_DEATH_MATCH ) {
            // チームとそのプレイヤーを取得
            Map<BattleTeam, List<Player>> playerMap = getTeamPlayers();

            // 各チームからリーダーを抽選する
            for ( BattleTeam team : playerMap.keySet() ) {
                List<Player> plist = playerMap.get(team);

                // シャッフル
                Collections.shuffle(plist);
                // 先頭のプレイヤーを取得
                Player target = plist.get(0);

                // チームのリーダーに設定
                ldmLeaderMap.put(team, target);

                // メッセージを表示
                plist.forEach(p -> {
                    p.sendMessage(
                            Chat.f("{0}&7チームのリーダーに &r{1} &7が選ばれました！", LeonGunWar.GAME_PREFIX,
                                    target.getPlayerListName()));
                });

                // リーダーにタイトルを表示
                target.sendTitle(Chat.f("&cあなたがリーダーです！"), "", 0, 20 * 4, 10);
            }
        }

        // 全プレイヤーに音を鳴らす
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
        });

        // 開始メッセージ
        Bukkit.broadcastMessage(Chat.f("{0}&7{1}", LeonGunWar.GAME_PREFIX, Strings.repeat("=", 40)));
        Bukkit.broadcastMessage(Chat.f("{0}&7制限時間 &c{1}", LeonGunWar.GAME_PREFIX, "10分"));
        Bukkit.broadcastMessage(Chat.f("{0}&7{1}", LeonGunWar.GAME_PREFIX, matchMode.getDescription()));
        Bukkit.broadcastMessage(Chat.f("{0}&7{1}", LeonGunWar.GAME_PREFIX, Strings.repeat("=", 40)));

        // タスクスタート
        runMatchTask();

        // isMatchingをtrueに変更
        isMatching = true;

        // 全プレイヤーにQuickメッセージを送信
        LeonGunWar.getQuickBar().send(Bukkit.getOnlinePlayers().stream().toArray(Player[]::new));
    }

    /**
     * ゲーム終了時に行う処理を書きます
     */
    public void finalizeMatch() {
        // Entry削除
        teams.values().stream()
                .forEach(team -> new ArrayList<>(team.getEntries()).forEach(team::removeEntry));

        // タスクの終了
        if ( matchTask != null ) {
            matchTask.cancel();
            matchTask = null;
        }

        // 残り時間を0に
        timeLeft.set(0);
        // チームのポイントを0に
        pointMap.clear();

        // killDeathCounterを初期化
        killDeathCounter = new KillDeathCounter();

        // サイドバーを削除
        LeonGunWar.getPlugin().getScoreboardDisplayer().clearSideBar();
        // 全プレイヤーのdisplayNameを初期化
        Bukkit.getOnlinePlayers().forEach(p -> {

            // DisplayNameをリセット
            p.setDisplayName(p.getName());

            // PlayerListの色はエントリーしていたら緑色
            if ( entryPlayers.contains(p) ) {
                p.setPlayerListName(Chat.f("&a{0}", p.getName()));
            } else {
                // その他はリセット
                p.setPlayerListName(p.getName());
            }
        });

        // リーダーを削除
        ldmLeaderMap.clear();
        // モードをnullに設定
        matchMode = null;

        // ゲーム終了
        isMatching = false;
    }

    /**
     * プレイヤーをマッチ参加用のエントリーに参加させます
     *
     * @param p 参加させたいプレイヤー
     */
    public boolean addEntryPlayer(Player p) {
        // すでに参加している場合はreturn false
        if ( entryPlayers.contains(p) ) {
            return false;
        }

        // エントリー追加
        entryPlayers.add(p);
        // 名前がデフォルトの場合
        if ( !isPlayerMatching(p) ) {
            // 名前の色を変更
            p.setPlayerListName(Chat.f("&a{0}", p.getName()));
        }

        // イベント呼び出し
        PlayerEntryMatchEvent event = new PlayerEntryMatchEvent(p);
        Bukkit.getPluginManager().callEvent(event);

        return true;
    }

    /**
     * プレイヤーをマッチ参加用のエントリーから退出させます
     *
     * @param p 退出させたいプレイヤー
     */
    public boolean removeEntryPlayer(Player p) {
        // 参加していない場合はreturn false
        if ( !entryPlayers.contains(p) ) {
            return false;
        }

        // エントリー解除
        entryPlayers.remove(p);

        // DisplayNameが緑で始まっていたら元に戻す
        if ( !isPlayerMatching(p) ) {
            // 名前リセット
            p.setPlayerListName(p.getName());
        }

        // イベント呼び出し
        PlayerLeaveEntryMatchEvent event = new PlayerLeaveEntryMatchEvent(p);
        Bukkit.getPluginManager().callEvent(event);

        return true;
    }

    /**
     * プレイヤーが次の試合に参加するエントリーをしているかどうかを確認します
     *
     * @param p 確認したいプレイヤー
     * @return エントリーに参加しているかどうか
     */
    public boolean isEntryPlayer(Player p) {
        return entryPlayers.contains(p);
    }

    /**
     * ゲームの残り時間を操作するタイマータスクを起動します 基本はMatchTimeChangedEventを利用して、イベントからゲームを操作するため
     * このタスクでは基本他の動作を行いません
     */
    private void runMatchTask() {
        // 試合中ならreturn
        if ( isMatching ) {
            return;
        }

        matchTask = new MatchCountdownTask().runTaskTimer(LeonGunWar.getPlugin(), 20, 20);
    }

    /**
     * プレイヤーを試合から退出させます
     *
     * @param p 退出させたいプレイヤー
     */
    public void kickPlayer(Player p) {
        leavePlayer(p);

        // スポーンにTP
        p.teleport(LeonGunWar.getPlugin().getSpawnsConfig().getLobby());
    }

    public void leavePlayer(Player p) {

        // 試合中でない場合はreturn
        if ( !isMatching ) {
            return;
        }

        // チームに含まれていれば退出させる
        teams.values().stream()
                .filter(scoreboardTeam -> scoreboardTeam.hasEntry(p.getName()))
                .forEach(scoreboardTeam -> scoreboardTeam.removeEntry(p.getName()));

        // 回復
        p.setHealth(20);

        // アーマー削除
        p.getInventory().setChestplate(null);

        // 退出メッセージを全員に送信
        String msg = Chat.f("{0}{1} &7が試合から離脱しました。", LeonGunWar.GAME_PREFIX, p.getPlayerListName());
        Bukkit.broadcastMessage(msg);

        // DisplayNameとPlayerListName初期化
        p.setDisplayName(p.getName());
        p.setPlayerListName(p.getName());

        // イベント呼び出し
        PlayerKickMatchEvent event = new PlayerKickMatchEvent(p);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * 全チームのプレイヤーをMap+List形式で取得します
     *
     * @return チームごとのプレイヤーのMap
     */
    public Map<BattleTeam, List<Player>> getTeamPlayers() {
        return Arrays.stream(BattleTeam.values())
                .collect(Collectors.toMap(Function.identity(), this::getTeamPlayers));
    }

    /**
     * 全チームの全プレイヤーを1つのList形式で取得します
     *
     * @return 全プレイヤーのList
     */
    public List<Player> getAllTeamPlayers() {
        return getTeamPlayers().values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * 指定されたチームのプレイヤーリストを取得します
     *
     * @param team プレイヤーリストを取得したいチーム
     * @return チームのプレイヤーリスト
     *
     * @exception NullPointerException teamがnullの場合
     */
    public List<Player> getTeamPlayers(@NonNull BattleTeam team) {
        // 取得したプレイヤーリストを返す
        return getScoreboardTeam(team).getEntries().stream()
                .map(Bukkit::getPlayerExact)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * プレイヤーが所属しているチームを取得します
     *
     * @param p チームを取得したいプレイヤー
     * @return プレイヤーが参加しているチーム
     */
    public BattleTeam getBattleTeam(Player p) {
        // 各チームのプレイヤーリストを取得し、リスポーンするプレイヤーが含まれていればbreak
        for ( BattleTeam team : BattleTeam.values() ) {

            // スコアボードのTeamを取得
            Team scoreboardTeam = getScoreboardTeam(team);

            // 殺したプレイヤーが含まれていればplayerTeamに代入してbreak
            if ( scoreboardTeam.getEntries().contains(p.getName()) ) {
                return team;
            }
        }

        return null;
    }

    /**
     * スコアボードのチームに対応するBattleTeamを取得する
     *
     * @param team 取得したいスコアボードのチーム
     * @return teamに対応するBattleTeam (不明ならnull)
     */
    public BattleTeam getBattleTeam(Team team) {
        // 各チームのプレイヤーリストを取得し、リスポーンするプレイヤーが含まれていればbreak
        for ( BattleTeam battleTeam : BattleTeam.values() ) {

            // スコアボードのTeamを取得
            Team scoreboardTeam = getScoreboardTeam(battleTeam);

            // 同じならreturn
            if ( scoreboardTeam == team ) {
                return battleTeam;
            }
        }

        // 無ければnull
        return null;
    }

    /**
     * 対象のプレイヤーが試合中かどうかを取得します
     *
     * @param player 対象のプレイヤー
     * @return 対象のプレイヤーが試合中かどうか
     */
    public boolean isPlayerMatching(Player player) {
        return isMatching && getBattleTeam(player) != null;
    }

    /**
     * 対象の複数のプレイヤーが同じチームどうかを取得します
     *
     * @param players 対象の複数のプレイヤー
     * @return 対象の複数のプレイヤーが同じチームどうか
     */
    public boolean isSameBattleTeam(Player... players) {
        return isMatching && Arrays.stream(players)
                .filter(Objects::nonNull)
                .map(this::getBattleTeam)
                .filter(Objects::nonNull)
                .distinct()
                .limit(2)
                .count() < 2;
    }

    public Team getScoreboardTeam(BattleTeam team) {
        return teams.getOrDefault(team, null);
    }

    /**
     * 指定したチームの現在のポイント数を取得します 試合が行われていない場合は-1を返します
     *
     * @param team ポイントを取得したいチーム
     * @return 指定したチームの現在のポイント
     *
     * @exception NullPointerException teamがnullの場合
     */
    public int getCurrentTeamPoint(@NonNull BattleTeam team) {
        // ポイント取得、無ければ0
        return pointMap.getOrDefault(team, 0);
    }

    public int getCurrentTeamPoint(@NonNull Team team) {
        // battleTeamに変換
        BattleTeam battleTeam = getBattleTeam(team);

        // 変換失敗なら0を返す
        if ( battleTeam == null ) {
            return 0;
        }

        // ポイントを取得、無ければ0
        return pointMap.getOrDefault(battleTeam, 0);
    }

    /**
     * 指定したチームに1ポイントを追加します。
     *
     * @param team ポイントを追加したいチーム
     * @exception NullPointerException teamがnullの場合
     */
    public void addTeamPoint(@NonNull BattleTeam team) {
        // 現在のポイント取得、無ければ0
        int currentPoint = pointMap.getOrDefault(team, 0);

        // ポイント追加
        currentPoint++;

        // 設定
        pointMap.put(team, currentPoint);

        // イベント呼び出し
        TeamPointIncreasedEvent event = new TeamPointIncreasedEvent(team, currentPoint);
        Bukkit.getPluginManager().callEvent(event);
    }

    public boolean addPlayerIntoBattle(Player p) {
        // すでに参加している場合はreturn
        if ( getAllTeamPlayers().contains(p) ) {
            return false;
        }

        // チーム分けする
        teamDistributor.distributePlayer(p, new ArrayList<>(teams.values()));

        // チームを取得する
        Map.Entry<BattleTeam, Team> entry = teams.entrySet().stream()
                .filter(e -> e.getValue().getEntries().contains(p.getName()))
                .findFirst()
                .orElse(null);
        BattleTeam team = entry != null ? entry.getKey() : null;

        if ( team == null ) {
            return false;
        }

        // セットアップする
        setUpPlayer(p, team);

        Player leader = getLDMLeader(team);
        if ( leader != null ) {
            p.sendMessage(Chat.f("{0}&7チームのリーダーに &r{1} &7が選ばれています！", LeonGunWar.GAME_PREFIX, leader.getPlayerListName()));
        }

        Bukkit.broadcastMessage(Chat.f("{0}{1} &7が途中参加しました！", LeonGunWar.GAME_PREFIX, p.getPlayerListName()));

        // 設定でエントリーするようになっていればエントリーする
        // Pluginが無効化されていたらreturn
        Plugin playerSettingsPlugin = Bukkit.getPluginManager().getPlugin("PlayerSettings");
        if ( playerSettingsPlugin == null || !playerSettingsPlugin.isEnabled() ) {
            return true;
        }

        // 設定を取得
        SettingsData data = PlayerSettings.getPlugin().getManager().getSettingsData(p);
        boolean enableEntry = data.isSet("LeonGunWar.EntryOnRejoin") && data.getBoolean("LeonGunWar.EntryOnRejoin");

        // 有効ならエントリーする
        if ( enableEntry ) {

            if ( !entryPlayers.contains(p) ) {
                // エントリー追加
                entryPlayers.add(p);

                // イベント呼び出し
                PlayerEntryMatchEvent event = new PlayerEntryMatchEvent(p);
                Bukkit.getPluginManager().callEvent(event);

                p.sendMessage(Chat.f("{0}&7設定に基づいて試合にエントリーしました", LeonGunWar.GAME_PREFIX));
            }
        }

        return true;
    }

    /**
     * LDMで使用されるメソッド。リーダーとなるプレイヤーを取得します
     *
     * @param team リーダーを取得したいチーム
     * @return そのチームのリーダー / LDMが指定されていない場合もしくは存在しなければnullを返す
     */
    public Player getLDMLeader(BattleTeam team) {
        // 試合のモードがLDMでなければreturn null
        if ( matchMode != MatchMode.LEADER_DEATH_MATCH ) {
            return null;
        }

        // 指定してあったらそれを返し、なければnullを返す
        return ldmLeaderMap.getOrDefault(team, null);
    }

    /**
     * LDMで使用されるメソッド。各チームのリーダーをMap形式で返します
     *
     * @return そのチームのリーダー / LDMでなければ空のMapを返す
     */
    public Map<BattleTeam, Player> getLDMLeaderMap() {
        // 試合のモードがLDMでなければreturn null
        if ( matchMode != MatchMode.LEADER_DEATH_MATCH ) {
            return new HashMap<>();
        }

        return ldmLeaderMap;
    }

    public void setMatchMode(MatchMode mode) {
        // 既に設定されていればIllegalStateExceptionを出す
        Preconditions.checkState(matchMode == null, "The mode is already set.");

        // モードを設定
        matchMode = mode;

        // すでに人数が集まっている場合はカウントダウンを開始
        if ( getEntryPlayers().size() >= 2 ) {
            LeonGunWar.getPlugin().getCountdown().startCountdown();
        }
    }

    protected void onDisablePlugin() {
        // 試合をしていなければreturn
        if ( !isMatching ) {
            return;
        }

        // 試合に参加しているプレイヤーを取得
        List<Player> plist = getAllTeamPlayers();

        // 全員をロビーにTP
        plist.forEach(p -> {

            // メッセージを表示
            p.sendMessage(Chat.f("{0}&c試合は強制終了されました", LeonGunWar.GAME_PREFIX));
            // スポーンにTP
            p.teleport(LeonGunWar.getPlugin().getSpawnsConfig().getLobby());

            // アーマー削除
            p.getInventory().setChestplate(null);

            // 各記録を取得
            int kills = killDeathCounter.getKills(p);
            int deaths = killDeathCounter.getDeaths(p);
            int assists = killDeathCounter.getAssists(p);

            // プレイヤーの戦績を表示
            p.sendMessage(Chat.f("&7[Your Score] {0} {1} Kill(s), {2} Death(s), {3} Assist(s)", p.getName(), kills,
                    deaths, assists));
        });

        // ボスバーを非表示
        bossBar.removeAll();
    }

    private void setUpPlayer(Player p, BattleTeam team) {
        // メッセージを表示する
        p.sendMessage(Chat.f("{0}&7あなたは &r{1} &7になりました！", LeonGunWar.GAME_PREFIX, team.getTeamName()));
        // DisplayNameとPlayerListNameを指定
        p.setDisplayName(Chat.f("{0}{1}&r", team.getChatColor(), p.getName()));
        p.setPlayerListName(Chat.f("{0}{1}&r", team.getChatColor(), p.getName()));
        // テレポート
        p.teleport(currentGameMap.getSpawnPoint(team));

        // 防具を装備
        p.getInventory().setChestplate(chestplates.get(team));
    }

    /**
     * 各チームの初期化を行います
     */
    private void initializeTeams() {
        // すでに初期化されている場合はreturn
        if ( initialized ) {
            return;
        }

        for ( BattleTeam team : BattleTeam.values() ) {
            String teamName = team.getTeamName();

            // チーム取得(なかったら作成)
            Team scoreboardTeam = scoreboard.getTeam(teamName);
            if ( scoreboardTeam == null ) {
                // チーム作成
                scoreboardTeam = scoreboard.registerNewTeam(teamName);
                // チームの色を指定
                scoreboardTeam.setColor(team.getChatColor());
                // フレンドリーファイアーを無効化
                scoreboardTeam.setAllowFriendlyFire(false);
                // 他チームからネームタグが見えるのを無効化
                scoreboardTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
                // 押し合いをなくす
                scoreboardTeam.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
                // Prefixを設定
                scoreboardTeam.setPrefix(team.getChatColor() + "");
            }

            // チームを保存
            teams.putIfAbsent(team, scoreboardTeam);
        }
    }

    /**
     * プレイヤーのリスポーン地点を取得します
     *
     * @param p 対象のプレイヤー
     * @return 対象のプレイヤーがリスポーンするべき場所
     */
    public Location getRespawnLocation(Player p) {

        // 試合をしていなければlobbySpawnを返す
        if ( !isMatching ) {
            return LeonGunWar.getPlugin().getSpawnsConfig().getLobby();
        }

        // チームを取得
        BattleTeam playerTeam = getBattleTeam(p);
        // スポーン地点
        Location spawnPoint = null;

        // チームがnullではないならそのチームのスポーン地点にTPする
        if ( playerTeam != null ) {
            spawnPoint = currentGameMap.getSpawnPoint(playerTeam);
        }

        // それでもまだspawnPointがnullの場合lobbyのスポーン地点を指定
        if ( spawnPoint == null ) {
            spawnPoint = LeonGunWar.getPlugin().getSpawnsConfig().getLobby();
        }

        return spawnPoint;
    }

    /**
     * チームの戦力レベルを取得します
     *
     * @param team 対象のチーム
     * @return レベル
     */
    public int getTeamPowerLevel(Team team){
        int tpl = 0;
        //チームのエントリーリストを取得
        for(String pn : team.getEntries()){
            Player p = Bukkit.getPlayer(pn);
            //(ないとは思うが)一応オンライン確認
            if(p==null){
                //オフラインの場合スキップ
                continue;
            }
            //チームパワーレベルに代入
            tpl = tpl + KDTeamDistributor_BETA.getPlayerPowerLevel(p);
        }
        return tpl;
    }
}

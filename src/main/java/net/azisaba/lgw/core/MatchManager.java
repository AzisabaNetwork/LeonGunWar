package net.azisaba.lgw.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.google.common.base.Preconditions;

import net.azisaba.lgw.core.events.PlayerEntryMatchEvent;
import net.azisaba.lgw.core.events.PlayerLeaveEntryMatchEvent;
import net.azisaba.lgw.core.maps.GameMap;
import net.azisaba.lgw.core.tasks.MatchCountdownTask;
import net.azisaba.lgw.core.teams.BattleTeam;
import net.azisaba.lgw.core.teams.DefaultTeamDistributor;
import net.azisaba.lgw.core.teams.TeamDistributor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.CustomItem;
import net.azisaba.lgw.core.utils.LocationLoader;

/**
 *
 * ゲームを司るコアクラス
 * @author siloneco
 *
 */
public class MatchManager {

	private boolean initialized = false;

	// チーム分けを行うクラス
	private TeamDistributor teamDistributor;

	// ロビーのスポーン地点
	private Location lobbySpawnPoint;

	// ゲーム中かどうかの判定
	private boolean isMatching = false;
	// 現在のマップ
	private GameMap currentMap = null;
	// 試合の残り時間
	private final AtomicInteger timeLeft = new AtomicInteger(0);
	// 試合を動かすタスク
	private BukkitTask matchTask;
	// KDカウンター
	private KillDeathCounter kdCounter;

	// マッチで使用するスコアボード
	private Scoreboard scoreboard;
	// 赤、青、試合参加エントリー用のスコアボードチーム
	private Team redTeam, blueTeam, entry;
	// 赤、青チームのチェストプレート
	private ItemStack redChestplate, blueChestplate;

	// ポイントを集計するHashMap
	private final HashMap<BattleTeam, Integer> pointMap = new HashMap<>();

	/**
	 * 初期化メソッド
	 * Pluginが有効化されたときのみ呼び出されることを前提としています
	 */
	protected void initialize() {
		// すでに初期化されている場合はreturn
		if (initialized) {
			return;
		}

		// kdCounterを新規作成
		kdCounter = new KillDeathCounter();

		// デフォルトのTeamDistributorを指定
		teamDistributor = new DefaultTeamDistributor();
		// メインではない新しいスコアボードを取得
		scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		// ScoreboardDisplayerにScoreboardを設定
		LeonGunWar.getPlugin().getScoreboardDisplayer().setScoreBoard(scoreboard);

		// 各スコアボードチームの取得 / 作成 (赤、青、試合参加エントリー用)
		initializeTeams();

		// 各チームのチェストプレートを設定
		// 赤チーム
		redChestplate = CustomItem.getTeamChestplate(BattleTeam.RED);

		// 青チーム
		blueChestplate = CustomItem.getTeamChestplate(BattleTeam.BLUE);

		// ロビーのスポーン地点をロード
		loadLobbySpawnLocation();

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
		//		timeLeft = 600;
		timeLeft.set(60); // Debug

		// マップを抽選
		currentMap = LeonGunWar.getPlugin().getMapContainer().getRandomMap();
		// 参加プレイヤーを取得
		List<Player> entryPlayers = getEntryPlayers();
		// プレイヤーを振り分け
		teamDistributor.distributePlayers(entryPlayers, Arrays.asList(redTeam, blueTeam));

		// 各プレイヤーにチームに沿った処理を行う
		// エントリー削除したときにgetEntries()の中身が変わってエラーを起こさないように新しいリストを作成してfor文を使用する
		// 赤チームの処理
		for (String redEntry : new ArrayList<>(redTeam.getEntries())) {
			Player p = Bukkit.getPlayerExact(redEntry);

			// プレイヤーが見つからない場合はエントリーから削除してcontinue
			if (p == null) {
				redTeam.removeEntry(redEntry);
				continue;
			}

			// メッセージを表示する
			p.sendMessage(
					Chat.f("{0}&7あなたは&r{1}&7になりました！", LeonGunWar.GAME_PREFIX, BattleTeam.RED.getDisplayTeamName()));
			// 防具を装備
			p.getInventory().setChestplate(redChestplate);
			// テレポート
			p.teleport(currentMap.getSpawnPoint(BattleTeam.RED));
		}

		// 青チームの処理
		for (String blueEntry : new ArrayList<>(blueTeam.getEntries())) {
			Player p = Bukkit.getPlayerExact(blueEntry);

			// プレイヤーが見つからない場合はエントリーから削除してcontinue
			if (p == null) {
				blueTeam.removeEntry(blueEntry);
				continue;
			}

			// メッセージを表示する
			p.sendMessage(
					Chat.f("{0}&7あなたは&r{1}&7になりました！", LeonGunWar.GAME_PREFIX, BattleTeam.BLUE.getDisplayTeamName()));
			// 防具を装備
			p.getInventory().setChestplate(blueChestplate);
			// テレポート
			p.teleport(currentMap.getSpawnPoint(BattleTeam.BLUE));
		}

		// タスクスタート
		runMatchTask();

		// isMatchingをtrueに変更
		isMatching = true;
	}

	/**
	 * ゲーム終了時に行う処理を書きます
	 */
	public void finalizeMatch() {
		// 赤チームのEntry削除
		for (String redEntry : new ArrayList<>(redTeam.getEntries())) {
			redTeam.removeEntry(redEntry);
		}
		// 青チームのEntry削除
		for (String blueEntry : new ArrayList<>(blueTeam.getEntries())) {
			blueTeam.removeEntry(blueEntry);
		}

		// タスクの終了
		if (matchTask != null) {
			matchTask.cancel();
			matchTask = null;
		}

		// 残り時間を0に
		timeLeft.set(0);

		// チームのポイントを0に
		pointMap.clear();

		// KillDeathCounterを初期化
		kdCounter = new KillDeathCounter();

		// サイドバーを削除
		LeonGunWar.getPlugin().getScoreboardDisplayer().clearSideBar();

		// ゲーム終了
		isMatching = false;
	}

	/**
	 * プレイヤーをマッチ参加用のエントリーに参加させます
	 * @param p 参加させたいプレイヤー
	 */
	public boolean addEntryPlayer(Player p) {
		// すでに参加している場合はreturn false
		if (entry.hasEntry(p.getName())) {
			return false;
		}

		// エントリー追加
		entry.addEntry(p.getName());
		// イベント呼び出し
		PlayerEntryMatchEvent event = new PlayerEntryMatchEvent(p);
		Bukkit.getPluginManager().callEvent(event);

		return true;
	}

	/**
	 * プレイヤーをマッチ参加用のエントリーから退出させます
	 * @param p 参加させたいプレイヤー
	 */
	public boolean removeEntryPlayer(Player p) {
		// 参加していない場合はreturn false
		if (!entry.hasEntry(p.getName())) {
			return false;
		}

		// エントリー解除
		entry.removeEntry(p.getName());
		// イベント呼び出し
		PlayerLeaveEntryMatchEvent event = new PlayerLeaveEntryMatchEvent(p);
		Bukkit.getPluginManager().callEvent(event);

		return true;
	}

	/**
	 * プレイヤーが次の試合に参加するエントリーをしているかどうかを確認します
	 * @param p 確認したいプレイヤー
	 * @return エントリーに参加しているかどうか
	 */
	public boolean isEntryPlayer(Player p) {
		return entry.hasEntry(p.getName());
	}

	/**
	 * 試合に参加するプレイヤーのリストを取得します
	 * @return entryスコアボードチームに参加しているプレイヤー
	 */
	public List<Player> getEntryPlayers() {
		// リスト作成
		List<Player> players = new ArrayList<>();

		// 名前からプレイヤー検索
		for (String entryName : new ArrayList<>(entry.getEntries())) {
			Player target = Bukkit.getPlayerExact(entryName);

			// プレイヤーが見つからない場合はエントリー解除してcontinue
			if (target == null) {
				entry.removeEntry(entryName);
				continue;
			}

			// リストに追加
			players.add(target);
		}

		return players;
	}

	/**
	 * プレイヤーのKDを保存するクラスを取得します
	 * キル数デス数の追加もここから行います
	 * @return 現在のKillDeathCounter
	 *
	 * @exception IllegalStateException まだ初期化されていないときにメソッドが呼ばれた場合
	 */
	public KillDeathCounter getKillDeathCounter() {
		Preconditions.checkState(initialized, "\"" + MatchManager.class.getName() + "\" is not initialized yet.");

		return kdCounter;
	}

	/**
	 * ゲームの残り時間を操作するタイマータスクを起動します
	 * 基本はMatchTimeChangedEventを利用して、イベントからゲームを操作するため
	 * このタスクでは基本他の動作を行いません
	 */
	private void runMatchTask() {
		// 試合中ならreturn
		if (isMatching) {
			return;
		}

		matchTask = new MatchCountdownTask().runTaskTimer(LeonGunWar.getPlugin(), 20, 20);
	}

	/**
	 * プレイヤーを試合から退出させます
	 * @param p 退出させたいプレイヤー
	 */
	public void kickPlayer(Player p) {

		// スポーンにTP
		p.teleport(getLobbySpawnLocation());

		// チームに含まれていれば退出させる
		if (redTeam.hasEntry(p.getName())) {
			redTeam.removeEntry(p.getName());
		} else if (blueTeam.hasEntry(p.getName())) {
			blueTeam.removeEntry(p.getName());
		}

		// 回復
		p.setHealth(20);
	}

	/**
	 * 全チームのプレイヤーをMap+List形式で取得します
	 * @return チームごとのプレイヤーのMap
	 */
	public Map<BattleTeam, List<Player>> getTeamPlayers() {
		return Stream.of(BattleTeam.values())
				.collect(Collectors.toMap(Function.identity(), this::getTeamPlayers));
	}

	/**
	 * 全チームの全プレイヤーを1つのList形式で取得します
	 * @return 全プレイヤーのList
	 */
	public List<Player> getAllTeamPlayers() {
		return getTeamPlayers().values().stream().flatMap(List::stream).collect(Collectors.toList());
	}

	/**
	 * 指定されたチームのプレイヤーリストを取得します
	 * @param team プレイヤーリストを取得したいチーム
	 * @return チームのプレイヤーリスト
	 *
	 * @exception IllegalArgumentException teamがnullの場合
	 */
	public List<Player> getTeamPlayers(BattleTeam team) {
		// teamがnullならIllegalArgumentException
		Preconditions.checkNotNull(team, "\"team\" mustn't be null.");

		// リスト作成
		List<Player> players = new ArrayList<>();
		// 名前のリストを作成
		List<String> entryList = null;

		// 赤チームの場合
		if (team == BattleTeam.RED) {
			entryList = new ArrayList<>(redTeam.getEntries());
		} else if (team == BattleTeam.BLUE) {
			entryList = new ArrayList<>(blueTeam.getEntries());
		}

		// Entryしている名前からプレイヤー検索
		for (String entryName : entryList) {
			// プレイヤーを取得
			Player player = Bukkit.getPlayerExact(entryName);

			// プレイヤーがいない場合はcontinue
			if (player == null) {
				continue;
			}

			// リストに追加
			players.add(player);
		}

		// 取得したプレイヤーリストを返す
		return players;
	}

	/**
	 * プレイヤーが所属しているチームを取得します
	 * @param p チームを取得したいプレイヤー
	 * @return プレイヤーが参加しているチーム
	 */
	public BattleTeam getBattleTeam(Player p) {
		// 各チームのプレイヤーリストを取得し、リスポーンするプレイヤーが含まれていればbreak
		for (BattleTeam team : BattleTeam.values()) {

			// スコアボードのTeamを取得
			Team scoreboardTeam = getScoreboardTeam(team);

			// 殺したプレイヤーが含まれていればplayerTeamに代入してbreak
			if (scoreboardTeam.getEntries().contains(p.getName())) {
				return team;
			}
		}

		return null;
	}

	/**
	 * 現在のマップを取得します
	 * 試合が行われていない場合はnullを返します
	 *
	 * @return 試合中のマップ
	 */
	public GameMap getCurrentGameMap() {
		// 試合中でなかったらnullを返す
		if (!isMatching) {
			return null;
		}

		return currentMap;
	}

	public Team getScoreboardTeam(BattleTeam team) {
		if (team == BattleTeam.RED) {
			return redTeam;
		} else if (team == BattleTeam.BLUE) {
			return blueTeam;
		} else {
			return null;
		}
	}

	/**
	 * 現在試合を行っているかどうかをbooleanで返します
	 * @return 現在試合を行っているかどうか
	 */
	public boolean isMatching() {
		return isMatching;
	}

	/**
	 * 試合の残り秒数を返します
	 * @return 試合の残り秒数
	 */
	public AtomicInteger getTimeLeft() {
		return timeLeft;
	}

	/**
	 * 指定したチームの現在のポイント数を取得します
	 * 試合が行われていない場合は-1を返します
	 *
	 * @param team ポイントを取得したいチーム
	 * @return 指定したチームの現在のポイント
	 *
	 * @exception IllegalArgumentException teamがnullの場合
	 */
	public int getCurrentTeamPoint(BattleTeam team) {
		// teamがnullならIllegalArgumentException
		Preconditions.checkNotNull(team, "\"team\" mustn't be null.");

		// 試合中でなかったら-1を返す
		if (!isMatching) {
			return -1;
		}

		// ポイント取得、無ければ0
		return pointMap.getOrDefault(team, 0);
	}

	/**
	 * 指定したチームに1ポイントを追加します。
	 *
	 * @param team ポイントを追加したいチーム
	 * @exception IllegalArgumentException チームがRED, BLUE以外の場合
	 */
	public void addTeamPoint(BattleTeam team) {
		// REDでもBLUEでもなければIllegalArgumentException
		Preconditions.checkNotNull(team, "\"team\" mustn't be null.");

		// 現在のポイント取得、無ければ0
		int currentPoint = pointMap.getOrDefault(team, 0);

		// ポイント追加
		currentPoint++;

		// 設定
		pointMap.put(team, currentPoint);
	}

	/**
	 * チーム分けを行うクラスを変更します
	 * @param distributor 変更するTeamDistributorを実装したクラスのコンストラクタ
	 */
	public void setTeamDistributor(TeamDistributor distributor) {
		teamDistributor = distributor;
	}

	/**
	 * ロビーのスポーン地点を取得します
	 * 初期化前に呼び出された場合はIllegalStateExceptionを投げます
	 *
	 * @return ロビーのスポーン地点
	 * @exception IllegalStateException 初期化前にメソッドが呼び出された場合
	 */
	public Location getLobbySpawnLocation() {
		// 初期化前なら IllegalStateException
		Preconditions.checkState(initialized, "\"" + MatchManager.class.getName() + "\" is not initialized yet.");

		return lobbySpawnPoint;
	}

	/**
	 * 各チームの初期化を行います
	 */
	private void initializeTeams() {
		// すでに初期化されている場合はreturn
		if (initialized) {
			return;
		}

		// 赤チーム取得(なかったら作成)
		redTeam = scoreboard.getTeam("Red");
		if (redTeam == null) {
			// チーム作成
			redTeam = scoreboard.registerNewTeam("Red");
			// チームの色を指定
			redTeam.setColor(ChatColor.DARK_RED);
			// フレンドリーファイアーを無効化
			redTeam.setAllowFriendlyFire(false);
			// 他チームからネームタグが見えるのを無効化
			redTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
			// 他プレイヤーに押されないように設定
			redTeam.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
		}

		// 青チーム取得(なかったら作成)
		blueTeam = scoreboard.getTeam("Blue");
		if (blueTeam == null) {
			// チーム作成
			blueTeam = scoreboard.registerNewTeam("Blue");
			// チームの色を指定
			blueTeam.setColor(ChatColor.DARK_BLUE);
			// フレンドリーファイアーを無効化
			blueTeam.setAllowFriendlyFire(false);
			// 他チームからネームタグが見えるのを無効化
			blueTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
			// 他プレイヤーに押されないように設定
			blueTeam.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
		}

		// エントリーチーム取得 (なかったら作成)
		entry = scoreboard.getTeam("Entry");
		if (entry == null) {
			// チーム作成
			entry = scoreboard.registerNewTeam("Entry");
			// チームの色を指定
			entry.setColor(ChatColor.GREEN);
		}
	}

	/**
	 * ロビーのスポーン地点をロードします
	 * 設定されていない場合はデフォルト値を設定します
	 */
	private void loadLobbySpawnLocation() {
		// ファイル
		File lobbySpawnFile = new File(LeonGunWar.getPlugin().getDataFolder(), "spawn.yml");
		YamlConfiguration spawnLoader = YamlConfiguration.loadConfiguration(lobbySpawnFile);

		// 座標をロード
		lobbySpawnPoint = LocationLoader.getLocation(spawnLoader, "lobby");

		// ロードできなかった場合はworldのスポーン地点を取得
		if (lobbySpawnPoint == null) {
			lobbySpawnPoint = Bukkit.getWorld("world").getSpawnLocation();
		}

		// 設定されていない場合はデフォルト値を設定
		if (spawnLoader.getConfigurationSection("lobby") == null) {
			lobbySpawnPoint = new Location(Bukkit.getWorld("world"), 616.5, 10, 70.5, 0, 0);
			// 設定
			LocationLoader.setLocationWithWorld(spawnLoader, lobbySpawnPoint, "lobby");
			// セーブ
			try {
				spawnLoader.save(lobbySpawnFile);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}

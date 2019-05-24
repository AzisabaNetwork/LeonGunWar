package net.azisaba.lgw.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.azisaba.lgw.core.commands.LgwCommand;
import net.azisaba.lgw.core.commands.UAVCommand;
import net.azisaba.lgw.core.listeners.DamageListener;
import net.azisaba.lgw.core.listeners.MatchControlListener;
import net.azisaba.lgw.core.listeners.MatchStartDetectListener;
import net.azisaba.lgw.core.listeners.PlayerControlListener;
import net.azisaba.lgw.core.listeners.modes.LeaderDeathMatchListener;
import net.azisaba.lgw.core.listeners.modes.TeamDeathMatchListener;
import net.azisaba.lgw.core.listeners.others.AfkKickEntryListener;
import net.azisaba.lgw.core.listeners.others.AutoRespawnListener;
import net.azisaba.lgw.core.listeners.others.CrackShotLimitListener;
import net.azisaba.lgw.core.listeners.others.DisableItemDamageListener;
import net.azisaba.lgw.core.listeners.others.DisableOffhandListener;
import net.azisaba.lgw.core.listeners.others.DisableOpenInventoryListener;
import net.azisaba.lgw.core.listeners.others.DisableRecipeListener;
import net.azisaba.lgw.core.listeners.others.DisableTNTBlockDamageListener;
import net.azisaba.lgw.core.listeners.others.EasyMigrateListener;
import net.azisaba.lgw.core.listeners.others.EnableKeepInventoryListener;
import net.azisaba.lgw.core.listeners.others.KillStreaksListener;
import net.azisaba.lgw.core.listeners.others.NoArrowGroundListener;
import net.azisaba.lgw.core.listeners.others.NoKnockbackListener;
import net.azisaba.lgw.core.listeners.others.RespawnKillProtectionListener;
import net.azisaba.lgw.core.listeners.others.SignWithColorListener;
import net.azisaba.lgw.core.listeners.others.TradeBoardListener;
import net.azisaba.lgw.core.listeners.signs.EntrySignListener;
import net.azisaba.lgw.core.listeners.signs.JoinAfterSignListener;
import net.azisaba.lgw.core.listeners.signs.MatchModeSignListener;
import net.azisaba.lgw.core.map.MapContainer;
import net.azisaba.lgw.core.map.MapLoader;
import net.azisaba.lgw.core.tasks.SignRemoveTask;
import net.azisaba.lgw.core.utils.Chat;

public class LeonGunWar extends JavaPlugin {

	public static final String GAME_PREFIX = Chat.f("&7[&6PvP&7]&r ");
	public static final String SIGN_ACTIVE = Chat.f("&a[ACTIVE]");
	public static final String SIGN_INACTIVE = Chat.f("&c[INACTIVE]");

	// plugin
	private static LeonGunWar plugin;

	private final MatchStartCountdown countdown = new MatchStartCountdown();
	private final ScoreboardDisplayer scoreboardDisplayer = new ScoreboardDisplayer();
	private final MapLoader mapLoader = new MapLoader();
	private final MapContainer mapContainer = new MapContainer();
	private final MatchManager manager = new MatchManager();
	private final KillStreaks killStreaks = new KillStreaks();
	private final TradeBoardManager tradeBoardManager = new TradeBoardManager();

	/**
	 * MatchStartCountdownのインスタンスを返します
	 * @return MatchStartCountdownのインスタンス
	 */
	public MatchStartCountdown getCountdown() {
		return countdown;
	}

	/**
	 * ScoreboardDisplayerのインスタンスを返します
	 * @return ScoreboardDisplayerのインスタンス
	 */
	public ScoreboardDisplayer getScoreboardDisplayer() {
		return scoreboardDisplayer;
	}

	/**
	 * MapLoaderのインスタンスを返します
	 * @return MapLoaderのインスタンス
	 */
	public MapLoader getMapLoader() {
		return mapLoader;
	}

	/**
	 * MapContainerのインスタンスを返します
	 * @return MapContainerのインスタンス
	 */
	public MapContainer getMapContainer() {
		return mapContainer;
	}

	/**
	 * MatchManagerのインスタンスを返します
	 * @return MatchManagerのインスタンス
	 */
	public MatchManager getManager() {
		return manager;
	}

	/**
	 * KillStreaksのインスタンスを返します
	 * @return KillStreaksのインスタンス
	 */
	public KillStreaks getKillStreaks() {
		return killStreaks;
	}

	/**
	 * TradeBoardManagerのインスタンスを返します
	 * @return TradeBoardManagerのインスタンス
	 */
	public TradeBoardManager getTradeBoardManager() {
		return tradeBoardManager;
	}

	@Override
	public void onEnable() {
		// 初期化が必要なファイルを初期化する
		mapContainer.loadMaps();
		manager.initialize();
		tradeBoardManager.init();

		// 移行を簡単にする [DEBUG]
		Bukkit.getPluginManager().registerEvents(new EasyMigrateListener(), this);

		// コマンドの登録
		Bukkit.getPluginCommand("leongunwar").setExecutor(new LgwCommand());
		Bukkit.getPluginCommand("uav").setExecutor(new UAVCommand());

		// コマンドの権限がない時のメッセージの指定
		Bukkit.getPluginCommand("leongunwar").setPermissionMessage(Chat.f("&c権限がありません！"));
		Bukkit.getPluginCommand("uav").setPermissionMessage(Chat.f("&c権限がありません！"));

		// リスナーの登録
		Bukkit.getPluginManager().registerEvents(new MatchControlListener(), this);
		Bukkit.getPluginManager().registerEvents(new EntrySignListener(), this);
		Bukkit.getPluginManager().registerEvents(new MatchModeSignListener(), this);
		Bukkit.getPluginManager().registerEvents(new JoinAfterSignListener(), this);
		Bukkit.getPluginManager().registerEvents(new MatchStartDetectListener(), this);
		Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerControlListener(), this);

		// リスナーの登録 (modes)
		Bukkit.getPluginManager().registerEvents(new TeamDeathMatchListener(), this);
		Bukkit.getPluginManager().registerEvents(new LeaderDeathMatchListener(), this);

		// リスナーの登録 (others)
		Bukkit.getPluginManager().registerEvents(new NoArrowGroundListener(), this);
		Bukkit.getPluginManager().registerEvents(new NoKnockbackListener(), this);
		Bukkit.getPluginManager().registerEvents(new DisableItemDamageListener(), this);
		Bukkit.getPluginManager().registerEvents(new DisableOpenInventoryListener(), this);
		Bukkit.getPluginManager().registerEvents(new DisableOffhandListener(), this);
		Bukkit.getPluginManager().registerEvents(new EnableKeepInventoryListener(), this);
		Bukkit.getPluginManager().registerEvents(new RespawnKillProtectionListener(), this);
		Bukkit.getPluginManager().registerEvents(new AutoRespawnListener(), this);
		Bukkit.getPluginManager().registerEvents(new AfkKickEntryListener(), this);
		Bukkit.getPluginManager().registerEvents(new KillStreaksListener(), this);
		Bukkit.getPluginManager().registerEvents(new DisableRecipeListener(), this);
		Bukkit.getPluginManager().registerEvents(new CrackShotLimitListener(), this);
		Bukkit.getPluginManager().registerEvents(new TradeBoardListener(), this);
		Bukkit.getPluginManager().registerEvents(new DisableTNTBlockDamageListener(), this);
		Bukkit.getPluginManager().registerEvents(new SignWithColorListener(), this);

		// SignRemoveTask (60秒後に最初の実行、それからは10分周期で実行)
		new SignRemoveTask().runTaskTimer(this, 20 * 60, 20 * 60 * 10);

		Bukkit.getLogger().info(Chat.f("{0} が有効化されました。", getName()));
	}

	@Override
	public void onDisable() {
		// Plugin終了時の処理を呼び出す
		manager.onDisablePlugin();

		// DisplayNameを戻す
		Bukkit.getOnlinePlayers().forEach(p -> {
			p.setDisplayName(p.getName());
		});

		// 武器交換掲示板の看板を保存
		tradeBoardManager.saveAll();

		Bukkit.getLogger().info(Chat.f("{0} が無効化されました。", getName()));
	}

	/**
	 * LeonGunWar pluginのインスタンスを返します
	 * @return LeonGunWarのインスタンス
	 */
	public static LeonGunWar getPlugin() {
		return plugin != null ? plugin : (plugin = JavaPlugin.getPlugin(LeonGunWar.class));
	}
}

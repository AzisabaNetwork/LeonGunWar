package net.azisaba.lgw.core;

import org.bukkit.plugin.java.JavaPlugin;

import net.azisaba.lgw.core.commands.LgwCommand;
import net.azisaba.lgw.core.kills.KillStreaks;
import net.azisaba.lgw.core.listeners.DamageListener;
import net.azisaba.lgw.core.listeners.EntrySignListener;
import net.azisaba.lgw.core.listeners.MatchControlListener;
import net.azisaba.lgw.core.listeners.MatchModeSignListener;
import net.azisaba.lgw.core.listeners.MatchStartDetectListener;
import net.azisaba.lgw.core.listeners.PlayerControlListener;
import net.azisaba.lgw.core.listeners.others.AfkKickEntryListener;
import net.azisaba.lgw.core.listeners.others.AutoRespawnListener;
import net.azisaba.lgw.core.listeners.others.DisableItemDamageListener;
import net.azisaba.lgw.core.listeners.others.DisableOffhandListener;
import net.azisaba.lgw.core.listeners.others.DisableOpenInventoryListener;
import net.azisaba.lgw.core.listeners.others.DisableRecipeListener;
import net.azisaba.lgw.core.listeners.others.EnableKeepInventoryListener;
import net.azisaba.lgw.core.listeners.others.KillStreaksListener;
import net.azisaba.lgw.core.listeners.others.NoArrowGroundListener;
import net.azisaba.lgw.core.listeners.others.NoKnockbackListener;
import net.azisaba.lgw.core.listeners.others.RespawnKillProtectionListener;
import net.azisaba.lgw.core.maps.MapContainer;
import net.azisaba.lgw.core.maps.MapLoader;
import net.azisaba.lgw.core.utils.Chat;

public class LeonGunWar extends JavaPlugin {

	public static final String GAME_PREFIX = Chat.f("&7[&6PvP&7]&r ");

	// plugin
	private static LeonGunWar plugin;

	private MatchStartCountdown countdown;
	private ScoreboardDisplayer scoreboardDisplayer;
	private MapLoader mapLoader;
	private MapContainer mapContainer;
	private MatchManager manager;
	private KillStreaks killStreaks;

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

	@Override
	public void onEnable() {
		// 初期化が必要なファイルを初期化する
		countdown = new MatchStartCountdown();
		scoreboardDisplayer = new ScoreboardDisplayer();
		mapLoader = new MapLoader();
		mapContainer = new MapContainer();
		mapContainer.loadMaps();
		manager = new MatchManager();
		manager.initialize();
		killStreaks = new KillStreaks();

		// コマンドの登録
		getServer().getPluginCommand("leongunwar").setExecutor(new LgwCommand());

		// コマンドの権限がない時のメッセージの指定
		getServer().getPluginCommand("leongunwar").setPermissionMessage(Chat.f("&c権限がありません！"));

		// リスナーの登録
		getServer().getPluginManager().registerEvents(new MatchControlListener(), this);
		getServer().getPluginManager().registerEvents(new EntrySignListener(), this);
		getServer().getPluginManager().registerEvents(new MatchModeSignListener(), this);
		getServer().getPluginManager().registerEvents(new MatchStartDetectListener(), this);
		getServer().getPluginManager().registerEvents(new DamageListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerControlListener(), this);

		// リスナーの登録 (others)
		getServer().getPluginManager().registerEvents(new NoArrowGroundListener(), this);
		getServer().getPluginManager().registerEvents(new NoKnockbackListener(), this);
		getServer().getPluginManager().registerEvents(new DisableItemDamageListener(), this);
		getServer().getPluginManager().registerEvents(new DisableOpenInventoryListener(), this);
		getServer().getPluginManager().registerEvents(new DisableOffhandListener(), this);
		getServer().getPluginManager().registerEvents(new EnableKeepInventoryListener(), this);
		getServer().getPluginManager().registerEvents(new RespawnKillProtectionListener(), this);
		getServer().getPluginManager().registerEvents(new AutoRespawnListener(), this);
		getServer().getPluginManager().registerEvents(new AfkKickEntryListener(), this);
		getServer().getPluginManager().registerEvents(new KillStreaksListener(), this);
		getServer().getPluginManager().registerEvents(new DisableRecipeListener(), this);

		getServer().getLogger().info(Chat.f("{0} enabled.", getName()));
	}

	@Override
	public void onDisable() {
		getServer().getLogger().info(Chat.f("{0} disabled.", getName()));
	}

	/**
	 * LeonGunWar pluginのインスタンスを返します
	 * @return LeonGunWarのインスタンス
	 */
	public static LeonGunWar getPlugin() {
		return plugin != null ? plugin : (plugin = JavaPlugin.getPlugin(LeonGunWar.class));
	}
}

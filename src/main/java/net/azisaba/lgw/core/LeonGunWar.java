package net.azisaba.lgw.core;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import net.azisaba.lgw.core.commands.LgwCommand;
import net.azisaba.lgw.core.listeners.DamageListener;
import net.azisaba.lgw.core.listeners.EntrySignListener;
import net.azisaba.lgw.core.listeners.MatchControlListener;
import net.azisaba.lgw.core.listeners.MatchStartDetectListener;
import net.azisaba.lgw.core.listeners.others.DisableItemDamageListener;
import net.azisaba.lgw.core.listeners.others.DisableOffhandListener;
import net.azisaba.lgw.core.listeners.others.DisableOpenInventoryListener;
import net.azisaba.lgw.core.listeners.others.EnableKeepInventoryListener;
import net.azisaba.lgw.core.listeners.others.InvincibleListener;
import net.azisaba.lgw.core.listeners.others.NoArrowGroundListener;
import net.azisaba.lgw.core.listeners.others.NoKnockbackListener;
import net.azisaba.lgw.core.maps.MapContainer;

public class LeonGunWar extends JavaPlugin {

	// plugin
	private static LeonGunWar plugin;

	@Override
	public void onEnable() {

		// pluginを指定
		plugin = this;

		// 初期化が必要なファイルを初期化する
		initializeClasses();

		// コマンドの登録
		getServer().getPluginCommand("leongunwar").setExecutor(new LgwCommand());

		// コマンドの権限がない時のメッセージの指定
		getServer().getPluginCommand("leongunwar").setPermissionMessage(ChatColor.RED + "権限がありません！");

		// リスナーの登録
		getServer().getPluginManager().registerEvents(new MatchControlListener(), this);
		getServer().getPluginManager().registerEvents(new EntrySignListener(), this);
		getServer().getPluginManager().registerEvents(new MatchStartDetectListener(), this);
		getServer().getPluginManager().registerEvents(new DamageListener(), this);

		// リスナーの登録 (others)
		getServer().getPluginManager().registerEvents(new NoArrowGroundListener(), this);
		getServer().getPluginManager().registerEvents(new NoKnockbackListener(), this);
		getServer().getPluginManager().registerEvents(new DisableItemDamageListener(), this);
		getServer().getPluginManager().registerEvents(new DisableOpenInventoryListener(), this);
		getServer().getPluginManager().registerEvents(new DisableOffhandListener(), this);
		getServer().getPluginManager().registerEvents(new EnableKeepInventoryListener(), this);
		getServer().getPluginManager().registerEvents(new InvincibleListener(), this);

		getServer().getLogger().info(getName() + " enabled.");
	}

	@Override
	public void onDisable() {
		getServer().getLogger().info(getName() + " disabled.");
	}

	/**
	 * 初期化を必要とするファイルを初期化します
	 */
	private void initializeClasses() {
		MapContainer.loadMaps();
		MatchManager.initialize();
	}

	/**
	 * LeonGunWar pluginのインスタンスを返します
	 * @return LeonGunWarのインスタンス
	 */
	public static LeonGunWar getPlugin() {
		return plugin;
	}
}

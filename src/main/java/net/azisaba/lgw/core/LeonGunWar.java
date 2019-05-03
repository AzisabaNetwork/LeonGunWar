package net.azisaba.lgw.core;

import org.bukkit.plugin.java.JavaPlugin;

import net.azisaba.lgw.core.commands.LgwCommand;
import net.azisaba.lgw.core.listeners.EntrySignListener;
import net.azisaba.lgw.core.listeners.KillDeathListener;
import net.azisaba.lgw.core.listeners.MatchControlListener;
import net.azisaba.lgw.core.listeners.MatchStartDetectListener;
import net.azisaba.lgw.core.listeners.others.DisableItemDamageListener;
import net.azisaba.lgw.core.listeners.others.DisableOffhandListener;
import net.azisaba.lgw.core.listeners.others.DisableOpenInventoryListener;
import net.azisaba.lgw.core.listeners.others.NoArrowGroundListener;
import net.azisaba.lgw.core.listeners.others.NoKnockbackListener;
import net.azisaba.lgw.core.maps.MapContainer;
import net.md_5.bungee.api.ChatColor;

public class LeonGunWar extends JavaPlugin {

	@Override
	public void onEnable() {
		// 初期化が必要なファイルを初期化する
		initializeClasses();

		// コマンドの登録
		getServer().getPluginCommand("lgw").setExecutor(new LgwCommand());

		// コマンドの権限がない時のメッセージの指定
		getServer().getPluginCommand("lgw").setPermissionMessage(ChatColor.RED + "権限がありません！");

		// リスナーの登録
		getServer().getPluginManager().registerEvents(new MatchControlListener(this), this);
		getServer().getPluginManager().registerEvents(new EntrySignListener(), this);
		getServer().getPluginManager().registerEvents(new MatchStartDetectListener(), this);
		getServer().getPluginManager().registerEvents(new KillDeathListener(), this);

		// リスナーの登録 (others)
		getServer().getPluginManager().registerEvents(new NoArrowGroundListener(this), this);
		getServer().getPluginManager().registerEvents(new NoKnockbackListener(), this);
		getServer().getPluginManager().registerEvents(new DisableItemDamageListener(), this);
		getServer().getPluginManager().registerEvents(new DisableOpenInventoryListener(), this);
		getServer().getPluginManager().registerEvents(new DisableOffhandListener(), this);

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
		MapContainer.init(this);
		MatchManager.init(this);
		MatchStartCountdown.init(this);
	}
}

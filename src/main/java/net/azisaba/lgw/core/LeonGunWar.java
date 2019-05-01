package net.azisaba.lgw.core;

import org.bukkit.plugin.java.JavaPlugin;

import net.azisaba.lgw.core.listeners.EntrySignListener;
import net.azisaba.lgw.core.listeners.MatchControlListener;
import net.azisaba.lgw.core.listeners.MatchStartDetectListener;
import net.azisaba.lgw.core.listeners.others.DisableItemDurability;
import net.azisaba.lgw.core.listeners.others.GroundArrowRemover;
import net.azisaba.lgw.core.listeners.others.NoKnockback;
import net.azisaba.lgw.core.maps.MapContainer;

public class LeonGunWar extends JavaPlugin {

	@Override
	public void onEnable() {
		// 初期化が必要なファイルを初期化する
		initializeClasses();

		// リスナーの登録
		getServer().getPluginManager().registerEvents(new GroundArrowRemover(this), this);
		getServer().getPluginManager().registerEvents(new NoKnockback(), this);
		getServer().getPluginManager().registerEvents(new DisableItemDurability(), this);
		getServer().getPluginManager().registerEvents(new MatchControlListener(this), this);
		getServer().getPluginManager().registerEvents(new EntrySignListener(), this);
		getServer().getPluginManager().registerEvents(new MatchStartDetectListener(), this);

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

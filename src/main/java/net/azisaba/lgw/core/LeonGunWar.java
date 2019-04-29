package net.azisaba.lgw.core;

import org.bukkit.plugin.java.JavaPlugin;

import net.azisaba.lgw.core.listeners.GroundArrowRemover;
import net.azisaba.lgw.core.listeners.NoNockBack;
import net.azisaba.lgw.core.maps.MapContainer;

public class LeonGunWar extends JavaPlugin {

	@Override
	public void onEnable() {
		// 初期化が必要なファイルを初期化する
		initializeClasses();

		// リスナーの登録
		getServer().getPluginManager().registerEvents(new GroundArrowRemover(this), this);
		getServer().getPluginManager().registerEvents(new NoNockBack(), this);

		getServer().getLogger().info(getName() + " enabled.");
	}

	@Override
	public void onDisable() {
		getServer().getLogger().info(getName() + " disabled.");
	}

	private void initializeClasses() {
		MapContainer.init(this);
		MatchManager.init(this);
	}
}

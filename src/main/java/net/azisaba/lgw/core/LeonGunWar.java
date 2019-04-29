package net.azisaba.lgw.core;

import org.bukkit.plugin.java.JavaPlugin;

public class LeonGunWar extends JavaPlugin {

	@Override
	public void onEnable() {
		getServer().getLogger().info(getName() + " enabled.");
	}

	@Override
	public void onDisable() {
		getServer().getLogger().info(getName() + " disabled.");
	}
}

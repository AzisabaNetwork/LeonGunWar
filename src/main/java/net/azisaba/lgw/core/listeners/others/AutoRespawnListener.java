package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class AutoRespawnListener implements Listener {

	@EventHandler(ignoreCancelled = false)
	public void onDeath(PlayerDeathEvent e) {
		Player deathPlayer = e.getEntity();

		// 即時リスポーン (座標指定は別リスナーで)
		deathPlayer.spigot().respawn();
	}
}

package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.azisaba.lgw.core.LeonGunWar;

public class KillStreaksListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player deader = e.getEntity();
		Player killer = deader.getKiller();

		// 自滅は無視
		if (killer == null || killer == deader) {
			return;
		}

		// カウントを追加
		LeonGunWar.getPlugin().getKillStreaks().add(killer);
	}
}

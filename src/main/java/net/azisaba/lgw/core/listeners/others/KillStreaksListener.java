package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.azisaba.lgw.core.LeonGunWar;

public class KillStreaksListener implements Listener {

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();

		// 自滅は無視
		if (p.getKiller() == null || p.getKiller() == p) {
			return;
		}

		Player killer = p.getKiller();

		// カウントを追加
		LeonGunWar.getPlugin().getKillStreaks().add(killer);
	}
}

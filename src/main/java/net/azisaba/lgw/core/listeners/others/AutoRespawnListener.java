package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.tasks.PlayPlayerHealingAnimationTask;

public class AutoRespawnListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onDeath(PlayerDeathEvent e) {
		Player deathPlayer = e.getEntity();

		// リスポーン
		deathPlayer.spigot().respawn();
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();

		e.setRespawnLocation(LeonGunWar.getPlugin().getManager().getRespawnLocation(p));

		new PlayPlayerHealingAnimationTask(p).runTaskLater(LeonGunWar.getPlugin(), 0);
	}
}

package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.teams.BattleTeam;

public class AutoRespawnListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeath(PlayerDeathEvent e) {
		Player deathPlayer = e.getEntity();

		// 体力を20に変更
		deathPlayer.setHealth(20);

		// リスポーンイベントをfire
		PlayerRespawnEvent event = new PlayerRespawnEvent(deathPlayer, getRespawnLocation(deathPlayer), true);
		LeonGunWar.getPlugin().getServer().getPluginManager().callEvent(event);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent e) {

		if (!e.getPlayer().isDead()) {
			e.getPlayer().teleport(e.getRespawnLocation());
		}
	}

	private Location getRespawnLocation(Player p) {
		// チームを取得
		BattleTeam playerTeam = MatchManager.getBattleTeam(p);
		// スポーン地点
		Location spawnPoint = null;

		// チームがnullではないならそのチームのスポーン地点にTPする
		if (playerTeam != null) {
			spawnPoint = MatchManager.getCurrentGameMap().getSpawnPoint(playerTeam);
		}

		// それでもまだspawnPointがnullの場合lobbyのスポーン地点を指定
		if (spawnPoint == null) {
			spawnPoint = MatchManager.getLobbySpawnLocation();
		}

		return spawnPoint;
	}
}

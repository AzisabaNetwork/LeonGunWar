package net.azisaba.lgw.core.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;

public class PlayerControlListener implements Listener {

	/**
	 * 試合中のプレイヤーがサーバーから退出した場合に試合から退出させるリスナー
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		MatchManager manager = LeonGunWar.getPlugin().getManager();
		// プレイヤーが試合中でなければreturn
		if (!manager.isMatching() || manager.getBattleTeam(p) == null) {
			return;
		}

		// 試合から退出
		manager.kickPlayer(p);
	}
}

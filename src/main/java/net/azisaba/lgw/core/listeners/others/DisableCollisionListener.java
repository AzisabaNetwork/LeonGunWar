package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import net.azisaba.lgw.core.LeonGunWar;

public class DisableCollisionListener implements Listener {

	private Team collisionTeam = null;

	public DisableCollisionListener() {
		// collisionTeamがnullの場合取得 / 作成
		if (collisionTeam == null) {
			Scoreboard board = LeonGunWar.getPlugin().getManager().getScoreboard();

			// チーム取得、nullなら作成
			collisionTeam = board.getTeam("Collision");
			if (collisionTeam == null) {
				collisionTeam = board.registerNewTeam("Collision");

				collisionTeam.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
			}
		}

		// 現在オンラインのプレイヤーを追加する
		Bukkit.getOnlinePlayers().forEach(p -> {
			// すでに追加されている場合はreturn
			if (collisionTeam.hasEntry(p.getName())) {
				return;
			}

			// 追加
			collisionTeam.addEntry(p.getName());
		});
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		// collisionTeamがnullの場合取得 / 作成
		if (collisionTeam == null) {
			Scoreboard board = LeonGunWar.getPlugin().getManager().getScoreboard();

			// チーム取得、nullなら作成
			collisionTeam = board.getTeam("Collision");
			if (collisionTeam == null) {
				collisionTeam = board.registerNewTeam("Collision");

				collisionTeam.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
			}
		}

		// プレイヤーを追加
		collisionTeam.addEntry(p.getName());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		// collisionTeamがnullの場合return
		if (collisionTeam == null) {
			return;
		}

		// プレイヤーが含まれていれば削除
		if (collisionTeam.hasEntry(e.getPlayer().getName())) {
			collisionTeam.removeEntry(e.getPlayer().getName());
		}
	}
}

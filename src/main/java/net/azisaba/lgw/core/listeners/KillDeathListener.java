package net.azisaba.lgw.core.listeners;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.teams.BattleTeam;

public class KillDeathListener implements Listener {

	/**
	 * プレイヤーを殺したことを検知するリスナー
	 * 死亡したプレイヤーの処理は他のリスナーで行います
	 */
	@EventHandler
	public void onKill(PlayerDeathEvent e) {
		// 試合中でなければreturn
		if (!MatchManager.isMatching()) {
			return;
		}

		// 殺したプレイヤーを取得
		Player killer = e.getEntity().getKiller();

		// 殺したプレイヤーがいない場合はreturn
		if (killer == null) {
			return;
		}

		// チームを取得
		BattleTeam killerTeam = null;

		// 各チームのプレイヤーリストを取得し、殺したプレイヤーが含まれていればbreak
		for (BattleTeam team : BattleTeam.values()) {
			// BOTHの場合はcontinue
			if (team == BattleTeam.BOTH) {
				continue;
			}

			// プレイヤーリストを取得
			List<Player> teamPlayers = MatchManager.getTeamPlayers(team);

			// 殺したプレイヤーが含まれていればkillerTeamに代入してbreak
			if (teamPlayers.contains(killer)) {
				killerTeam = team;
				break;
			}
		}

		// killerTeamがnullの場合return
		if (killerTeam == null) {
			return;
		}

		// ポイントを追加
		MatchManager.addTeamPoint(killerTeam);
		// 個人キルを追加
		MatchManager.getKillDeathCounter().addKill(killer);
	}

	/**
	 * 試合中のプレイヤーが死亡した場合、死亡カウントを増加させ、即時リスポーンさせます
	 */
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player deathPlayer = e.getEntity();

		// チームを取得
		BattleTeam deathPlayerTeam = null;

		// 各チームのプレイヤーリストを取得し、死んだプレイヤーが含まれていればbreak
		for (BattleTeam team : BattleTeam.values()) {
			// BOTHの場合はcontinue
			if (team == BattleTeam.BOTH) {
				continue;
			}

			// プレイヤーリストを取得
			List<Player> teamPlayers = MatchManager.getTeamPlayers(team);

			// 殺したプレイヤーが含まれていればdeathPlayerTeamに代入してbreak
			if (teamPlayers.contains(deathPlayer)) {
				deathPlayerTeam = team;
				break;
			}
		}

		// deathPlayerTeamがnullの場合return
		if (deathPlayerTeam == null) {
			return;
		}

		// 死亡数を追加
		MatchManager.getKillDeathCounter().addDeath(deathPlayer);

		// 即時リスポーン (座標指定は別リスナーで)
		deathPlayer.spigot().respawn();
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();

		// チームを取得
		BattleTeam playerTeam = null;

		// 各チームのプレイヤーリストを取得し、リスポーンするプレイヤーが含まれていればbreak
		for (BattleTeam team : BattleTeam.values()) {
			// BOTHの場合はcontinue
			if (team == BattleTeam.BOTH) {
				continue;
			}

			// プレイヤーリストを取得
			List<Player> teamPlayers = MatchManager.getTeamPlayers(team);

			// 殺したプレイヤーが含まれていればplayerTeamに代入してbreak
			if (teamPlayers.contains(p)) {
				playerTeam = team;
				break;
			}
		}

		// リスポーン先を指定
		if (playerTeam == BattleTeam.RED) { // 赤チームの場合
			e.setRespawnLocation(MatchManager.getCurrentGameMap().getRedSpawn());
		} else if (playerTeam == BattleTeam.BLUE) { // 青チームの場合
			e.setRespawnLocation(MatchManager.getCurrentGameMap().getBlueSpawn());
		} else { // その他の場合は(nullも含む) lobbyのスポーン
			e.setRespawnLocation(MatchManager.getLobbySpawnLocation());
		}
	}
}

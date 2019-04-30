package net.azisaba.lgw.core.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.MatchTimeChangedEvent;
import net.azisaba.lgw.core.teams.BattleTeam;
import net.md_5.bungee.api.ChatColor;

public class MatchControlListener implements Listener {

	private ItemStack winnerProof;

	private LeonGunWar plugin;

	public MatchControlListener(LeonGunWar plugin) {
		this.plugin = plugin;

		// 勝者の証を生成
		winnerProof = new ItemStack(Material.END_CRYSTAL);
		ItemMeta meta = winnerProof.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "勝者の証");
		meta.setLore(Arrays.asList("勝者に与えられる証", "ダイヤと交換できる"));
		winnerProof.setItemMeta(meta);
	}

	@EventHandler
	public void matchFinishDetector(MatchTimeChangedEvent e) {
		// 時間を取得して0じゃなかったらreturn
		if (e.getTimeLeft() > 0) {
			return;
		}

		// チーム作成
		BattleTeam team;

		// 各チームのポイントを取得
		int redPoint = MatchManager.getCurrentTeamPoint(BattleTeam.RED);
		int bluePoint = MatchManager.getCurrentTeamPoint(BattleTeam.BLUE);

		if (redPoint > bluePoint) { // 赤が多い場合
			team = BattleTeam.RED;
		} else if (bluePoint > redPoint) { // 青が多い場合
			team = BattleTeam.BLUE;
		} else { // 同じ場合
			team = BattleTeam.BOTH;
		}

		// イベントを呼び出す
		MatchFinishedEvent event = new MatchFinishedEvent(MatchManager.getCurrentGameMap(), team,
				MatchManager.getTeamPlayers(BattleTeam.RED), MatchManager.getTeamPlayers(BattleTeam.BLUE));
		plugin.getServer().getPluginManager().callEvent(event);
	}

	@EventHandler
	public void onMatchFinished(MatchFinishedEvent e) {
		// 勝ったチームのプレイヤーリストを取得
		List<Player> winnerPlayers = new ArrayList<>();
		if (e.getWinner() == BattleTeam.RED) {
			winnerPlayers = e.getRedTeamPlayers();
		} else if (e.getWinner() == BattleTeam.BLUE) {
			winnerPlayers = e.getBlueTeamPlayers();
		}

		for (Player p : winnerPlayers) {
			// 勝者の証を付与
			p.getInventory().addItem(winnerProof);
		}

		// 試合に参加した全プレイヤーを取得
		List<Player> allPlayers = new ArrayList<Player>(e.getRedTeamPlayers());
		allPlayers.addAll(e.getBlueTeamPlayers());

		for (Player p : allPlayers) {
			// スポーンにTP
			p.teleport(MatchManager.getLobbySpawnLocation());

			// アーマー削除
			p.getInventory().setChestplate(null);

			// TODO 戦績の表示
		}
	}
}

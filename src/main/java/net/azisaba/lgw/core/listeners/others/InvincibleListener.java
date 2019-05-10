package net.azisaba.lgw.core.listeners.others;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.teams.BattleTeam;

public class InvincibleListener implements Listener {

	private final double invincibleSeconds = 5.0d;

	private HashMap<Player, Long> respawnTime = new HashMap<>();
	private HashMap<Player, BukkitTask> taskMap = new HashMap<>();

	@EventHandler
	public void onDamageByEntity(EntityDamageByEntityEvent e) {
		// ダメージを受けたEntityがプレイヤーでなければreturn
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player victim = (Player) e.getEntity();

		// リスポーンから5秒以内ならキャンセル
		if (respawnTime.getOrDefault(victim, 0L) + (1000 * invincibleSeconds) > System.currentTimeMillis()) {
			e.setCancelled(true);

			// 攻撃したプレイヤーにメッセージを表示
			if (e.getDamager() instanceof Player) {

				// 攻撃されたプレイヤーのチームを取得
				BattleTeam team = MatchManager.getBattleTeam(victim);

				// 色を取得
				ChatColor nameColor = ChatColor.WHITE;
				if (team != null) {
					nameColor = team.getChatColor();
				}

				((Player) e.getDamager()).sendMessage(nameColor + victim.getName() + ChatColor.GRAY + "は保護されています！");
			}
		}
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();

		// リスポーン時間指定
		respawnTime.put(p, System.currentTimeMillis());

		// タスク終了
		BukkitTask task = taskMap.getOrDefault(p, null);
		if (task != null) {
			task.cancel();
		}

		// 次のタスクまでの時間を計算し起動する
		int floor = (int) Math.floor(invincibleSeconds);
		double runAfter = invincibleSeconds - floor;
		task = getRunnable(p).runTaskLater(LeonGunWar.getPlugin(), (long) runAfter * 20);

		// タスク更新
		taskMap.put(p, task);
	}

	private BukkitRunnable getRunnable(Player p) {
		return new BukkitRunnable() {
			@Override
			public void run() {

				// 残り何秒か取得
				int noInvincibleSeconds = (int) (invincibleSeconds
						- (System.currentTimeMillis() - respawnTime.get(p)) / 1000);

				// 残り秒数を表示。もし0なら終了と表示
				if (noInvincibleSeconds > 0) {
					p.sendMessage(
							ChatColor.GRAY + "無敵時間解除まであと" + ChatColor.RED + noInvincibleSeconds + "秒" + ChatColor.GRAY
									+ "！");
				} else {
					p.sendMessage(ChatColor.GRAY + "無敵時間終了！");
					return;
				}

				// 次のタスクまでの時間を計算し起動する
				double next = (respawnTime.get(p) + (invincibleSeconds - ((noInvincibleSeconds - 1) * 1000))
						- System.currentTimeMillis()) / 1000;
				BukkitTask task = getRunnable(p).runTaskLater(LeonGunWar.getPlugin(), (long) next * 20);

				// タスク更新
				taskMap.put(p, task);
			}
		};
	}
}

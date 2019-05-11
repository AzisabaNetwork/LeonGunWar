package net.azisaba.lgw.core.listeners.others;

import java.time.OffsetDateTime;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitTask;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.tasks.RespawnKillProtectionTask;
import net.azisaba.lgw.core.teams.BattleTeam;
import net.azisaba.lgw.core.utils.Chat;

public class RespawnKillProtectionListener implements Listener {

	private final long invincibleSeconds = 6;

	private final HashMap<Player, OffsetDateTime> remainTimes = new HashMap<>();
	private final HashMap<Player, BukkitTask> taskMap = new HashMap<>();

	@EventHandler
	public void onDamageByEntity(EntityDamageByEntityEvent e) {
		// ダメージを受けたEntityがプレイヤーでなければreturn
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player victim = (Player) e.getEntity();

		// リスポーンから5秒以内ならキャンセル
		if (OffsetDateTime.now().isBefore(remainTimes.getOrDefault(victim, OffsetDateTime.MIN))) {
			e.setCancelled(true);

			// 攻撃したプレイヤーにメッセージを表示
			if (e.getDamager() instanceof Player) {

				// 攻撃されたプレイヤーのチームを取得
				BattleTeam team = LeonGunWar.getPlugin().getManager().getBattleTeam(victim);

				// 色を取得
				ChatColor nameColor = team != null ? team.getChatColor() : ChatColor.RESET;

				((Player) e.getDamager()).sendMessage(
						Chat.f("{0}{1}{2} &7は保護されています！", LeonGunWar.GAME_PREFIX, nameColor, victim.getName()));
			}
		}
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();

		// リスポーン時間指定
		remainTimes.put(p, OffsetDateTime.now().plusSeconds(invincibleSeconds));

		taskMap.compute(p, (p2, task) -> {
			// タスク終了
			if (task != null) {
				task.cancel();
			}

			// タスク開始
			return new RespawnKillProtectionTask(p2, remainTimes).runTaskTimer(LeonGunWar.getPlugin(), 0, 20);
		});
	}
}

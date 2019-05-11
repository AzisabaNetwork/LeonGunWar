package net.azisaba.lgw.core.listeners.others;

import java.time.OffsetDateTime;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.tasks.RespawnKillProtectionTask;
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

			Player attacker = null;
			// 攻撃したEntityがプレイヤーならメッセージ送信対象に指定
			if (e.getDamager() instanceof Player) {
				attacker = (Player) e.getDamager();
			} else if (e.getDamager() instanceof Projectile) {
				// 攻撃したEntityが投げ物なら、投げたEntityを取得
				ProjectileSource shooter = ((Projectile) e.getDamager()).getShooter();

				// shooterがプレイヤーならメッセージ送信対象に指定
				if (shooter instanceof Player) {
					attacker = (Player) shooter;
				}
			}

			// attackerがnullではない場合、メッセージを送信
			if (attacker != null) {
				attacker.sendMessage(Chat.f("{0}{1} &7は保護されています！", LeonGunWar.GAME_PREFIX, victim.getDisplayName()));
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

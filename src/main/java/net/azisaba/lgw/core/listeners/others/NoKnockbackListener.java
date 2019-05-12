package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.azisaba.lgw.core.LeonGunWar;

/**
 * ノックバックを無効化するためのクラス
 * @author siloneco
 *
 */
public class NoKnockbackListener implements Listener {

	/**
	 * プレイヤーがノックバックしたときに呼び出されるイベント
	 * @param e ノックバックしたイベント
	 */
	@EventHandler
	public void onPlayerVelocity(PlayerVelocityEvent e) {
		if (e.getPlayer().getLastDamageCause().getCause() != DamageCause.PROJECTILE) {
			e.setVelocity(new Vector());
		} else if (e.getPlayer().getLastDamageCause().getCause().toString().endsWith("EXPLOEXPLOSION")){
			return;
		} else {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onDamaged(EntityDamageByEntityEvent e) {
		// 爆発ではなかったらreturn
		if (!e.getCause().toString().endsWith("EXPLOSION")) {
			return;
		}

		// プレイヤーではなかったらreturn
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		// イベントが終わった後にVelocityを初期化
		new BukkitRunnable() {
			public void run() {
				((Player) e.getEntity()).setVelocity(new Vector(0, 0, 0));
			}
		}.runTaskLater(LeonGunWar.getPlugin(), 0);
	}
}

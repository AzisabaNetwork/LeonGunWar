package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import net.azisaba.lgw.core.LeonGunWar;

/**
 * 地面に当たった矢を削除するリスナーです。
 * @author siloneco
 *
 */
public class NoArrowGroundListener implements Listener {

	/**
	 * 矢がブロックに当たったことを検知し削除します
	 * @param e
	 */
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e) {
		// 当たった飛び道具が矢でなければreturn
		if (!(e.getEntity() instanceof Arrow)) {
			return;
		}

		Arrow arrow = (Arrow) e.getEntity();

		// 1tick後にその矢がブロックに当たっているか確認
		LeonGunWar.getPlugin().getServer().getScheduler().runTaskLater(LeonGunWar.getPlugin(), () -> {
			// ブロックに刺さっているか確認
			if (arrow != null && arrow.isOnGround()) {
				// 矢を削除
				arrow.remove();
			}
		}, 1);
	}
}

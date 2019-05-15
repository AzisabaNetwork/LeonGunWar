package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.tasks.RemoveGroundArrowTask;

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

		new RemoveGroundArrowTask(arrow).runTaskLater(LeonGunWar.getPlugin(), 0);
	}
}

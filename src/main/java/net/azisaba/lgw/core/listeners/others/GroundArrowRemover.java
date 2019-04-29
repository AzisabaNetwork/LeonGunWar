package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.LeonGunWar;

/**
 * 地面に当たった矢を削除するリスナーです。
 * @author siloneco
 *
 */
public class GroundArrowRemover implements Listener {

	private LeonGunWar plugin;

	public GroundArrowRemover(LeonGunWar plugin) {
		this.plugin = plugin;
	}

	/**
	 * 矢がブロックに当たったことを検知し削除します
	 * @param e
	 */
	@EventHandler
	public void arrow(ProjectileHitEvent e) {
		// 当たった飛び道具が矢でなければreturn
		if (!(e.getEntity() instanceof Arrow)) {
			return;
		}

		Arrow arrow = (Arrow) e.getEntity();

		// 1tick後にその矢がブロックに当たっているか確認
		new BukkitRunnable() {
			public void run() {
				// ブロックに刺さっているか確認
				if (arrow.isOnGround()) {
					// 矢を削除
					arrow.remove();
				}
			}
		}.runTaskLater(plugin, 1);
	}

}

package net.azisaba.lgw.core.tasks;

import org.bukkit.entity.Arrow;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.RequiredArgsConstructor;

// 矢がブロックに当たっているか確認、当たっていたら削除
@RequiredArgsConstructor
public class RemoveGroundArrowTask extends BukkitRunnable {

	private final Arrow arrow;

	@Override
	public void run() {
		// ブロックに刺さっているか確認
		if (arrow != null && arrow.isOnGround()) {
			// 矢を削除
			arrow.remove();
		}
	}
}

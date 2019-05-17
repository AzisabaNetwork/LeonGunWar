package net.azisaba.lgw.core.tasks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.KeizibanManager;
import net.azisaba.lgw.core.SignData;

public class SignRemoveTask extends BukkitRunnable {

	@Override
	public void run() {
		for (SignData data : KeizibanManager.getAllSignData()) {

			if (data.getBreakAt() < System.currentTimeMillis()) {
				continue;
			}

			Block b = data.getLocation().getBlock();
			if (b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
				b.setType(Material.AIR);
				KeizibanManager.removeSignData(data.getLocation());
			}
		}
	}
}

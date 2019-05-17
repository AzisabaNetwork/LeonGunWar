package net.azisaba.lgw.core.tasks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.LeonGunWar;

public class SignRemoveTask extends BukkitRunnable {

	@Override
	public void run() {

		LeonGunWar.getPlugin().getTradeBoardManager().getAllSignData().forEach(data -> {
			if (data.getBreakAt() < System.currentTimeMillis()) {
				return;
			}

			Block b = data.getLocation().getBlock();
			if (b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
				b.setType(Material.AIR);
				LeonGunWar.getPlugin().getTradeBoardManager().removeSignData(data.getLocation());
			}
		});
	}
}

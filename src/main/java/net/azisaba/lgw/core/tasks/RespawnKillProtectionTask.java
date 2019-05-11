package net.azisaba.lgw.core.tasks;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.LeonGunWar;

public class RespawnKillProtectionTask extends BukkitRunnable {

	// 5秒間無敵
	private final double invincibleSeconds = 5.0d;

	// 対象のプレイヤー
	private final Player p;

	// プレイヤーごとの無敵残り時間
	private final HashMap<Player, Long> respawnTime;

	public RespawnKillProtectionTask(Player p, HashMap<Player, Long> respawnTime) {
		this.p = p;
		this.respawnTime = respawnTime;
	}

	@Override
	public void run() {
		// 残り時間 (秒) 取得
		int remain = (int) (invincibleSeconds - (System.currentTimeMillis() - respawnTime.get(p)) / 1000);

		// 0以下ならキャンセルしてreturn
		if (remain <= 0) {
			p.sendMessage(LeonGunWar.GAME_PREFIX + ChatColor.GRAY + "無敵時間終了");
			cancel();
			return;
		}

		// 残り秒数を表示
		p.sendMessage(LeonGunWar.GAME_PREFIX + ChatColor.GRAY + "無敵時間残り " + ChatColor.RED + remain + "秒");
	}
}

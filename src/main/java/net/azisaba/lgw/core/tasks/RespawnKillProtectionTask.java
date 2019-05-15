package net.azisaba.lgw.core.tasks;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;

public class RespawnKillProtectionTask extends BukkitRunnable {

	// 対象のプレイヤー
	private final Player p;

	// プレイヤーごとの無敵残り時間
	private final HashMap<Player, OffsetDateTime> remainTimes;

	public RespawnKillProtectionTask(Player p, HashMap<Player, OffsetDateTime> remainTimes) {
		this.p = p;
		this.remainTimes = remainTimes;
	}

	@Override
	public void run() {
		// 残り時間 (秒) 取得
		long remain = Duration.between(OffsetDateTime.now(), remainTimes.get(p)).getSeconds();

		// 0以下ならキャンセルしてreturn
		if (remain <= 0) {
			p.sendMessage(Chat.f("{0}&7無敵時間終了！", LeonGunWar.GAME_PREFIX));
			cancel();
			return;
		}

		// 残り秒数を表示
		p.sendMessage(Chat.f("{0}&7無敵時間残り &c{1}秒&7！", LeonGunWar.GAME_PREFIX, remain));
	}
}

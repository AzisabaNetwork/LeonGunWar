package net.azisaba.lgw.core.listeners.others;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;

public class AfkKickEntryListener implements Listener {

	// 最後にプレイヤーが動いた時のミリ秒を保存
	private final HashMap<Player, Long> lastMoved = new HashMap<>();

	public AfkKickEntryListener() {
		// コンストラクタが呼び出されたときにタスクを開始
		runAfkKickEntryTask();

		// 現在オンラインのプレイヤーを設定
		Bukkit.getOnlinePlayers().forEach(p -> {
			lastMoved.put(p, System.currentTimeMillis());
		});
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		lastMoved.put(e.getPlayer(), System.currentTimeMillis());
	}

	/**
	 * 参加したときも値を設定
	 */
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		lastMoved.put(e.getPlayer(), System.currentTimeMillis());
	}

	/**
	 * データがかさばるので退出したときに値を削除
	 */
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if (lastMoved.containsKey(e.getPlayer())) {
			lastMoved.remove(e.getPlayer());
		}
	}

	private void runAfkKickEntryTask() {
		new BukkitRunnable() {
			@Override
			public void run() {

				Bukkit.getOnlinePlayers().forEach(p -> {
					// プレイヤーが試合をしていない&エントリーしていなければreturn
					boolean matching = LeonGunWar.getPlugin().getManager().isMatching()
							&& LeonGunWar.getPlugin().getManager().getBattleTeam(p) != null;
					boolean entrying = LeonGunWar.getPlugin().getManager().isEntryPlayer(p);

					// 試合もエントリーもしていないプレイヤーならreturn
					if (!matching && !entrying) {
						return;
					}

					// 試合はしていないがエントリーはしているプレイヤーもreturn
					if (!matching && entrying) {
						return;
					}

					// プレイヤーが最後に動いた秒数を取得
					long lastMovedMilliSecond = lastMoved.getOrDefault(p, 0L);

					// 30秒より少なければreturn
					if (lastMovedMilliSecond + 1000 * 60 > System.currentTimeMillis()) {
						return;
					}

					// 権限を持っていればreturn
					if (p.hasPermission("leongunwar.afkkick.exempt")) {
						return;
					}

					// 試合から退出 & エントリー解除
					LeonGunWar.getPlugin().getManager().removeEntryPlayer(p);
					LeonGunWar.getPlugin().getManager().kickPlayer(p);

					p.sendMessage(Chat.f("{0}&7放置と判定されたため試合から退出しました", LeonGunWar.GAME_PREFIX));

					LeonGunWar.getPlugin().getLogger().info(Chat.f("{0} を試合から退出させました", p.getName()));
				});
			}
		}.runTaskTimer(LeonGunWar.getPlugin(), 1, 20 * 3);
	}
}

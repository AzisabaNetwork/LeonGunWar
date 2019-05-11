package net.azisaba.lgw.core.listeners;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.events.PlayerEntryMatchEvent;
import net.azisaba.lgw.core.events.PlayerLeaveEntryMatchEvent;
import net.md_5.bungee.api.ChatColor;

public class MatchStartDetectListener implements Listener {

	/**
	 * プレイヤーがエントリーしたときに現在のエントリー人数を確認し試合が開始できそうならカウントダウンを開始するリスナー
	 */
	@EventHandler
	public void matchStarter(PlayerEntryMatchEvent e) {
		// すでに試合中ならreturn
		if (LeonGunWar.getPlugin().getManager().isMatching()) {
			return;
		}

		// エントリーしているプレイヤーを取得
		List<Player> entryPlayers = LeonGunWar.getPlugin().getManager().getEntryPlayers();

		// 人数が2人未満ならreturn
		if (entryPlayers.size() < 2) {
			return;
		}

		// カウントダウン開始
		LeonGunWar.getPlugin().getCountdown().startCountdown();
	}

	/**
	 * プレイヤーがエントリー解除したときに試合を開始できない人数だった場合カウントダウンを解除する
	 */
	@EventHandler
	public void matchStarter(PlayerLeaveEntryMatchEvent e) {
		// すでに試合中ならreturn
		if (LeonGunWar.getPlugin().getManager().isMatching()) {
			return;
		}

		// エントリーしているプレイヤーを取得
		List<Player> entryPlayers = LeonGunWar.getPlugin().getManager().getEntryPlayers();

		// 人数が2人以上ならreturn
		if (entryPlayers.size() >= 2) {
			return;
		}

		// カウントダウン停止
		LeonGunWar.getPlugin().getCountdown().stopCountdown();

		// メッセージを表示
		Bukkit.broadcastMessage(LeonGunWar.GAME_PREFIX + ChatColor.GRAY + "人数が足りないため試合を開始できません！");
	}
}

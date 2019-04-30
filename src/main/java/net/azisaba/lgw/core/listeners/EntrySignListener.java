package net.azisaba.lgw.core.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.azisaba.lgw.core.MatchManager;
import net.md_5.bungee.api.ChatColor;

/**
 *
 * Entryチームに参加する看板に関するリスナークラス
 * @author siloneco
 *
 */
public class EntrySignListener implements Listener {

	/**
	 * エントリー看板をクリックしたことを検知し、プレイヤーを追加するリスナー
	 */
	@EventHandler
	public void onClickSignEvent(PlayerInteractEvent e) {
		// ブロックをクリックしていなければreturn
		if (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		// プレイヤー / ブロック取得
		Player p = e.getPlayer();
		Block clickedBlock = e.getClickedBlock();

		// ブロックが看板でなければreturn
		if (clickedBlock.getType() != Material.SIGN_POST && clickedBlock.getType() != Material.WALL_SIGN) {
			return;
		}

		// Signにキャスト
		Sign sign = (Sign) clickedBlock.getState();

		// 1行目が [entry] でなければreturn
		if (!sign.getLine(0).equals("[entry]")) {
			return;
		}

		// イベントをキャンセル
		e.setCancelled(true);

		// 4行目が[INACTIVE]ならキャンセル
		if (ChatColor.stripColor(sign.getLine(3)).equals("[INACTIVE]")) {
			return;
		}

		// エントリー
		boolean success = MatchManager.entryPlayer(p);

		// メッセージを表示
	}
}

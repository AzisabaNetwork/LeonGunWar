package net.azisaba.lgw.core.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.azisaba.lgw.core.MatchManager;

/**
 *
 * Entryチームに参加する看板に関するリスナークラス
 * ColorTeamingEntryのときは左クリックの時だけでクリエイティブの場合いちいちサバイバルに変更しなければならなくめんどくさかったため左右どちらも対応
 * ACTIVEとINACTIVEを切り替えるときはスニークをしながら右クリックで可能
 * 破壊するときはスニークしながら左クリックで可能
 *
 * @author siloneco
 *
 */
public class EntrySignListener implements Listener {

	/**
	 * エントリー看板をクリックしたことを検知し、プレイヤーを追加するリスナー
	 */
	@EventHandler
	public void onClickJoinEntrySign(PlayerInteractEvent e) {
		// ブロックをクリックしていなければreturn
		if (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		// プレイヤー / ブロック取得
		Player p = e.getPlayer();
		Block clickedBlock = e.getClickedBlock();

		// スニーク + 左クリックならreturn
		if (e.getAction() == Action.LEFT_CLICK_BLOCK && p.isSneaking() && p.getGameMode() == GameMode.CREATIVE) {
			return;
		}

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
		boolean success = MatchManager.addEntryPlayer(p);

		// メッセージを表示
		if (success) { // エントリーした場合
			p.sendMessage(ChatColor.GREEN + "エントリーに参加しました！");
		} else { // すでにエントリーしている場合
			p.sendMessage(ChatColor.RED + "すでにエントリーしています！");
		}
	}

	/**
	 * エントリー解除看板をクリックしたことを検知し、プレイヤーをエントリー解除するリスナー
	 */
	@EventHandler
	public void onClickLeaveEntrySign(PlayerInteractEvent e) {
		// ブロックをクリックしていなければreturn
		if (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		// プレイヤー / ブロック取得
		Player p = e.getPlayer();
		Block clickedBlock = e.getClickedBlock();

		// スニーク + 左クリックならreturn
		if (e.getAction() == Action.LEFT_CLICK_BLOCK && p.isSneaking() && p.getGameMode() == GameMode.CREATIVE) {
			return;
		}

		// ブロックが看板でなければreturn
		if (clickedBlock.getType() != Material.SIGN_POST && clickedBlock.getType() != Material.WALL_SIGN) {
			return;
		}

		// Signにキャスト
		Sign sign = (Sign) clickedBlock.getState();

		// 1行目が [leave] でなければreturn
		if (!sign.getLine(0).equals("[leave]")) {
			return;
		}

		// イベントをキャンセル
		e.setCancelled(true);

		// 4行目が[INACTIVE]ならキャンセル
		if (ChatColor.stripColor(sign.getLine(3)).equals("[INACTIVE]")) {
			return;
		}

		// エントリー
		boolean success = MatchManager.addEntryPlayer(p);

		// メッセージを表示
		if (success) { // エントリーした場合
			p.sendMessage(ChatColor.GREEN + "エントリーを解除しました！");
		} else { // すでにエントリーしている場合
			p.sendMessage(ChatColor.RED + "まだエントリーしていません！");
		}
	}

	@EventHandler
	public void changeSignState(PlayerInteractEvent e) {
		// ブロックをシフト + 右クリックしていなければreturn
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || !e.getPlayer().isSneaking()) {
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

		// 1行目が [entry] または [leave] でなければreturn
		if (!sign.getLine(0).equals("[leave]") && !sign.getLine(0).equals("[entry]")) {
			return;
		}

		// 権限がなければreturn
		if (!p.hasPermission("leongunwar.entrysign.changestate")) {
			return;
		}

		// イベントをキャンセル
		e.setCancelled(true);

		// 元メッセージ
		String line4 = ChatColor.stripColor(sign.getLine(3));
		// 編集先メッセージ
		String edit = "";

		// 4行目の編集
		if (line4.equalsIgnoreCase("[INACTIVE]")) { // [INACTIVE] の場合
			edit = ChatColor.GREEN + "[ACTIVE]";
		} else { // それ以外の場合は [INACITVE]に変更
			edit = ChatColor.RED + "[INACTIVE]";
		}

		// 変更
		sign.setLine(3, edit);
		// 必要か分からないのでとりあえず設定
		clickedBlock.getState().setData(sign.getData());
	}
}

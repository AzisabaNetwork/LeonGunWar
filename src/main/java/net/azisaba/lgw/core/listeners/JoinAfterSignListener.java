package net.azisaba.lgw.core.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.utils.Chat;

public class JoinAfterSignListener implements Listener {

	private HashMap<UUID, Long> lastClicked = new HashMap<>();

	/**
	 * 試合参加看板をクリックしたことを検知し、プレイヤーを試合に参加させるリスナー
	 */
	@EventHandler
	public void onClickJoinEntrySign(PlayerInteractEvent e) {
		// 最終クリックが10分より前ならreturn
		if (lastClicked.getOrDefault(e.getPlayer(), 0L) + (1000 * 60 * 10) > System.currentTimeMillis()) {
			e.getPlayer().sendMessage(Chat.f("&c現在クールダウン中です！"));
			return;
		}

		// ブロックをクリックしていなければreturn
		if (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		// プレイヤー / ブロック取得
		Player p = e.getPlayer();
		Block clickedBlock = e.getClickedBlock();

		// 権限を持っており スニーク + クリックならreturn
		if (p.hasPermission("leongunwar.entrysign.changestate") && p.isSneaking()) {
			return;
		}

		// ブロックが看板でなければreturn
		if (clickedBlock.getType() != Material.SIGN_POST && clickedBlock.getType() != Material.WALL_SIGN) {
			return;
		}

		// Signにキャスト
		Sign sign = (Sign) clickedBlock.getState();

		// 1行目が [entry] でなければreturn
		if (!sign.getLine(0).equals("[join]")) {
			return;
		}

		// イベントをキャンセル
		e.setCancelled(true);

		// 4行目が[INACTIVE]ならキャンセル
		if (Chat.r(sign.getLine(3)).equals("[INACTIVE]")) {
			return;
		}

		// 試合中ではない場合return
		if (!LeonGunWar.getPlugin().getManager().isMatching()) {
			p.sendMessage(Chat.f("{0}&7現在試合をしていないため途中参加はできません。\n代わりにエントリー看板を使用してください", LeonGunWar.GAME_PREFIX));
			return;
		}

		// プレイヤーを追加
		LeonGunWar.getPlugin().getManager().addPlayerIntoBattle(p);

		// lastClicked設定
		lastClicked.put(p.getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
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
		if (!sign.getLine(0).equals("[join]")) {
			return;
		}

		// 権限がなければreturn
		if (!p.hasPermission("leongunwar.entrysign.changestate")) {
			return;
		}

		// イベントをキャンセル
		e.setCancelled(true);

		// 元メッセージ
		String line4 = Chat.r(sign.getLine(3));
		// 編集先メッセージ
		String edit = "";

		// 4行目の編集
		if (line4.equalsIgnoreCase("[INACTIVE]")) { // [INACTIVE] の場合
			edit = Chat.f("&a[ACTIVE]");
		} else { // それ以外の場合は [INACITVE]に変更
			edit = Chat.f("&c[INACTIVE]");
		}

		// 変更
		sign.setLine(3, edit);
		// 更新
		sign.update();
	}

	@EventHandler
	public void onMatchFinishedEvent(MatchFinishedEvent e) {
		lastClicked.clear();
	}
}

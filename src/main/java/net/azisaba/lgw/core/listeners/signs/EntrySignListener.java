package net.azisaba.lgw.core.listeners.signs;

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
import net.azisaba.lgw.core.utils.Chat;

/**
 *
 * Entryチームに参加する看板に関するリスナークラス
 * ColorTeamingEntryのときは左クリックの時だけでクリエイティブの場合いちいちサバイバルに変更しなければならなくめんどくさかったため左右どちらも対応
 * ACTIVEとINACTIVEを切り替えるときはスニークをしながら右クリックで可能 破壊するときはスニークしながら左クリックで可能
 *
 * @author siloneco
 *
 */
public class EntrySignListener implements Listener {

    /**
     * エントリー看板をクリックしたことを検知し、プレイヤーを追加するリスナー
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClickJoinEntrySign(PlayerInteractEvent e) {
        // ブロックをクリックしていなければreturn
        if ( e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK ) {
            return;
        }

        // プレイヤー / ブロック取得
        Player p = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();

        // 権限を持っており スニーク + クリックならreturn
        if ( p.hasPermission("leongunwar.entrysign.changestate") && p.isSneaking() ) {
            return;
        }

        // ブロックが看板でなければreturn
        if ( clickedBlock.getType() != Material.OAK_WALL_SIGN && clickedBlock.getType() != Material.OAK_SIGN ) {
            return;
        }

        // Signにキャスト
        Sign sign = (Sign) clickedBlock.getState();

        // 1行目が [entry] でなければreturn
        if ( !sign.getLine(0).equals("[entry]") ) {
            return;
        }

        // 4行目が[ACTIVE]ではない場合キャンセル
        if ( !sign.getLine(3).equals(LeonGunWar.SIGN_ACTIVE) ) {
            return;
        }

        // イベントをキャンセル
        e.setCancelled(true);

        // エントリー
        boolean success = LeonGunWar.getPlugin().getManager().addEntryPlayer(p);

        // メッセージを表示
        if ( success ) { // エントリーした場合
            p.sendMessage(Chat.f("{0}&aゲームにエントリーしました。", LeonGunWar.GAME_PREFIX));
        } else { // すでにエントリーしている場合
            p.sendMessage(Chat.f("{0}&cあなたは、既に参加しています。", LeonGunWar.GAME_PREFIX));
        }
    }

    /**
     * エントリー解除看板をクリックしたことを検知し、プレイヤーをエントリー解除するリスナー
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClickLeaveEntrySign(PlayerInteractEvent e) {
        // ブロックをクリックしていなければreturn
        if ( e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK ) {
            return;
        }

        // プレイヤー / ブロック取得
        Player p = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();

        // 権限を持っておりスニーク + クリックならreturn
        if ( p.hasPermission("leongunwar.entrysign.changestate") && p.isSneaking() ) {
            return;
        }

        // ブロックが看板でなければreturn
        if ( clickedBlock.getType() != Material.OAK_WALL_SIGN && clickedBlock.getType() != Material.OAK_SIGN ) {
            return;
        }

        // Signにキャスト
        Sign sign = (Sign) clickedBlock.getState();

        // 1行目が [leave] でなければreturn
        if ( !sign.getLine(0).equals("[leave]") ) {
            return;
        }

        // 4行目が[ACTIVE]ではない場合return
        if ( !sign.getLine(3).equals(LeonGunWar.SIGN_ACTIVE) ) {
            return;
        }

        // イベントをキャンセル
        e.setCancelled(true);

        // エントリー解除
        boolean success = LeonGunWar.getPlugin().getManager().removeEntryPlayer(p);

        // メッセージを表示
        if ( success ) { // エントリー解除した場合
            p.sendMessage(Chat.f("{0}&aゲームから退出しました。", LeonGunWar.GAME_PREFIX));
        } else { // すでにエントリーしていない場合
            p.sendMessage(Chat.f("{0}&cあなたは、エントリーしていません。", LeonGunWar.GAME_PREFIX));
        }
    }

    @EventHandler
    public void changeSignState(PlayerInteractEvent e) {
        // ブロックをシフト + 右クリックしていなければreturn
        if ( e.getAction() != Action.RIGHT_CLICK_BLOCK || !e.getPlayer().isSneaking() ) {
            return;
        }

        // プレイヤー / ブロック取得
        Player p = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();

        // ブロックが看板でなければreturn
        if ( clickedBlock.getType() != Material.OAK_WALL_SIGN && clickedBlock.getType() != Material.OAK_SIGN ) {
            return;
        }

        // Signにキャスト
        Sign sign = (Sign) clickedBlock.getState();

        // 1行目が [entry] または [leave] でなければreturn
        if ( !sign.getLine(0).equals("[leave]") && !sign.getLine(0).equals("[entry]") ) {
            return;
        }

        // 権限がなければreturn
        if ( !p.hasPermission("leongunwar.entrysign.changestate") ) {
            return;
        }

        // イベントをキャンセル
        e.setCancelled(true);

        // 元メッセージ
        String line4 = sign.getLine(3);
        // 編集先メッセージ
        String edit;

        // 4行目の編集
        if ( line4.equals(LeonGunWar.SIGN_INACTIVE) ) { // [INACTIVE] の場合
            edit = LeonGunWar.SIGN_ACTIVE;
        } else { // それ以外の場合は [INACITVE]に変更
            edit = LeonGunWar.SIGN_INACTIVE;
        }

        // 変更
        sign.setLine(3, edit);
        // 更新
        sign.update();
    }
}

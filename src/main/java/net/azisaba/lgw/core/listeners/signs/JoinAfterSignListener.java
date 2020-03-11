package net.azisaba.lgw.core.listeners.signs;

import java.util.HashMap;
import java.util.Map;
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

    private final Map<UUID, Long> lastClicked = new HashMap<>();

    /**
     * 試合参加看板をクリックしたことを検知し、プレイヤーを試合に参加させるリスナー
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
        if ( clickedBlock.getType() != Material.SIGN_POST && clickedBlock.getType() != Material.WALL_SIGN ) {
            return;
        }

        // Signにキャスト
        Sign sign = (Sign) clickedBlock.getState();

        // 1行目が [rejoin] でなければreturn
        if ( !sign.getLine(0).equals("[rejoin]") ) {
            return;
        }

        // 4行目が[ACTIVE]ではない場合はreturn
        if ( !sign.getLine(3).equals(LeonGunWar.SIGN_ACTIVE) ) {
            return;
        }

        // 最終クリックが1分より前ならreturn
        if ( lastClicked.getOrDefault(e.getPlayer().getUniqueId(), 0L) + 1000 * 60 > System.currentTimeMillis() ) {
            e.getPlayer().sendMessage(Chat.f("&c現在クールダウン中です！"));
            return;
        }

        // イベントをキャンセル
        e.setCancelled(true);

        // 試合中ではない場合return
        if ( !LeonGunWar.getPlugin().getManager().isMatching() ) {
            p.sendMessage(Chat.f("{0}&7現在試合をしていないため途中参加はできません。\n代わりにエントリー看板を使用してください", LeonGunWar.GAME_PREFIX));
            return;
        }

        // プレイヤーを追加
        LeonGunWar.getPlugin().getManager().addPlayerIntoBattle(p);

        // lastClicked設定
        lastClicked.put(p.getUniqueId(), System.currentTimeMillis());
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
        if ( clickedBlock.getType() != Material.SIGN_POST && clickedBlock.getType() != Material.WALL_SIGN ) {
            return;
        }

        // Signにキャスト
        Sign sign = (Sign) clickedBlock.getState();

        // 1行目が [rejoin] でなければreturn
        if ( !sign.getLine(0).equals("[rejoin]") ) {
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

    @EventHandler
    public void onMatchFinishedEvent(MatchFinishedEvent e) {

        if ( LeonGunWar.getPlugin().getManager().isMatching() ) {
            lastClicked.clear();
        }
    }
}

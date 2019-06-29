package net.azisaba.lgw.core.listeners.signs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.google.common.base.Strings;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.Chat;

/**
 *
 * 次に実行する試合の種類を指定する看板 ACTIVEとINACTIVEを切り替えるときはスニークをしながら右クリックで可能
 * 破壊するときはスニークしながら左クリックで可能
 *
 * @author siloneco
 *
 */
public class MatchModeSignListener implements Listener {

    /**
     * エントリー看板をクリックしたことを検知し、プレイヤーを追加するリスナー
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClickSign(PlayerInteractEvent e) {
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

        // 1行目が [mode] でなければreturn
        if ( !Chat.r(sign.getLine(0)).equalsIgnoreCase("[mode]") ) {
            return;
        }

        // 4行目が[ACTIVE]でなければreturn
        if ( !sign.getLine(3).equals(LeonGunWar.SIGN_ACTIVE) ) {
            return;
        }

        // イベントをキャンセル
        e.setCancelled(true);

        // 2行目を取得し、MatchModeに変換
        String line2 = Chat.r(sign.getLine(1));
        MatchMode mode = MatchMode.getFromString(line2);

        // modeがnullの場合return
        if ( mode == null ) {
            return;
        }

        // モードを指定
        if ( LeonGunWar.getPlugin().getManager().getMatchMode() != null ) {
            p.sendMessage(Chat.f("{0}&7すでに設定されているためモード変更ができません！", LeonGunWar.GAME_PREFIX));
            return;
        }

        LeonGunWar.getPlugin().getManager().setMatchMode(mode);
        Bukkit.broadcastMessage(Chat.f("{0}&7{1}", LeonGunWar.GAME_PREFIX, Strings.repeat("=", 40)));
        Bukkit.broadcastMessage(Chat.f("{0}&7モード   {1}", LeonGunWar.GAME_PREFIX, mode.getModeName()));
        Bukkit.broadcastMessage(Chat.f("{0}&7人数が集まり次第開始します", LeonGunWar.GAME_PREFIX));
        Bukkit.broadcastMessage(Chat.f("{0}&7{1}", LeonGunWar.GAME_PREFIX, Strings.repeat("=", 40)));

        // 音を鳴らす
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
        });

        // 全プレイヤーにQuickメッセージを送信
        LeonGunWar.getQuickBar().send(Bukkit.getOnlinePlayers().stream().toArray(Player[]::new));
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

        // 1行目が [entry] または [leave] でなければreturn
        if ( !Chat.r(sign.getLine(0)).equalsIgnoreCase("[mode]") ) {
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
        String edit = "";

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

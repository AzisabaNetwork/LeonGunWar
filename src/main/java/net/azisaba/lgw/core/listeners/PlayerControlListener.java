package net.azisaba.lgw.core.listeners;

import java.util.List;
import java.util.Map;

import net.azisaba.lgw.core.util.SyogoData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.PlayerKickMatchEvent;
import net.azisaba.lgw.core.util.BattleTeam;

public class PlayerControlListener implements Listener {

    /**
     * 試合中のプレイヤーがサーバーから退出した場合に試合から退出させるリスナー
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        SyogoData.removeCache(p.getUniqueId());

        MatchManager manager = LeonGunWar.getPlugin().getManager();
        // プレイヤーが試合中でなければreturn
        if ( !manager.isPlayerMatching(p) ) {
            return;
        }

        // 試合から退出
        manager.kickPlayer(p);
    }

    /**
     * LDMでリーダーが退出した際にリーダーを再抽選するリスナー
     */
    @EventHandler
    public void onPlayerKicked(PlayerKickMatchEvent e) {
        Player p = e.getPlayer();
        MatchManager manager = LeonGunWar.getPlugin().getManager();

        // LDMではなかった場合return
        if ( !manager.isLeaderMatch() ) {
            return;
        }

        // どれかのチームの人数が0人の場合はキャンセル (他のListenerが対応するため)
        if ( manager.getTeamPlayers().values().stream()
                .anyMatch(List::isEmpty) ) {
            return;
        }

        // 全チームのリーダーを取得
        Map<BattleTeam, Player> leaderMap = manager.getLDMLeaderMap();

        // 蹴られたプレイヤーが居たチームを取得
        BattleTeam team = leaderMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(p))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        // プレイヤーがリーダーだった場合、リーダーの再抽選を行う
        if ( team != null ) {
            manager.setLeaderAtRandom(team);
        }
    }

    /**
     * プレイヤーが退出したときにエントリーを解除するリスナー
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void removeEntryWhenPlayerLeaveServer(PlayerQuitEvent e) {
        LeonGunWar.getPlugin().getManager().removeEntryPlayer(e.getPlayer());
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();

        // 試合中ではなかったらreturn
        if ( !LeonGunWar.getPlugin().getManager().isMatching() ) {
            return;
        }

        // プレイヤーが試合をしていなかったらreturn
        if ( !LeonGunWar.getPlugin().getManager().getAllTeamPlayers().contains(p) ) {
            return;
        }

        // Fromが試合のワールドではなかったらreturn
        if ( e.getFrom() != LeonGunWar.getPlugin().getManager().getCurrentGameMap().getWorld() ) {
            return;
        }

        // 退出
        LeonGunWar.getPlugin().getManager().leavePlayer(p);
    }

    /**
     * 参加時にQuickメッセージを表示します
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void sendQuickMessage(PlayerJoinEvent e) {
        LeonGunWar.getQuickBar().send(e.getPlayer());

        Bukkit.getScheduler().runTaskAsynchronously(LeonGunWar.getPlugin(), new Runnable() {
            @Override
            public void run() {
                SyogoData.getSyogoData(e.getPlayer().getUniqueId());
            }
        });

    }
}

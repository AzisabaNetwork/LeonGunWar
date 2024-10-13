package net.azisaba.lgw.core.tasks;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.utils.BroadcastUtils;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.SecondOfDay;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;

public class AssasinationWitherDeathCountdown extends BukkitRunnable {
    private int timeLeft = 60;
    private BattleTeam winteam;

    public AssasinationWitherDeathCountdown(BattleTeam winteam){
        this.winteam = winteam;
    }

    @Override
    public void run() {

        // 0の場合ゲームスタート
        if ( timeLeft <= 0 ) {
            MatchManager manager = LeonGunWar.getPlugin().getManager();
            // 試合終了
            MatchFinishedEvent event = new MatchFinishedEvent(manager.getCurrentGameMap(), Collections.singletonList(winteam),
                    manager.getTeamPlayers());
            Bukkit.getPluginManager().callEvent(event);
            return;
        }

        boolean chat = false;
        boolean sound = false;

        // 以下の場合残り秒数をチャット欄もしくはタイトルに表示する
        if ( timeLeft % 10 == 0 ) { // 10の倍数の場合
            chat = true;
        } else if ( timeLeft <= 5 ) { // 数字が5以下の場合
            chat = true;
            sound = true;
        }

        // chatがtrueの場合表示
        if ( chat ) {
            String msg;
            if(winteam == BattleTeam.RED){
                msg = Chat.f( BattleTeam.BLUE.getTeamName()  + "&7時間切れまで残り &c{0}&7", SecondOfDay.f(timeLeft));
            }else {
                msg = Chat.f( BattleTeam.RED.getTeamName()  + "&7時間切れまで残り &c{0}&7", SecondOfDay.f(timeLeft));
            }

            BroadcastUtils.broadcast(msg);
        }

        // soundがtrueの場合音を鳴らす
        if ( sound ) {
            BroadcastUtils.getOnlinePlayers()
                    .forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f));
        }

        // 1秒減らす
        timeLeft--;
    }
}

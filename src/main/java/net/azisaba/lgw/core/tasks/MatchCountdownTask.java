package net.azisaba.lgw.core.tasks;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.events.MatchTimeChangedEvent;

public class MatchCountdownTask extends BukkitRunnable {

    @Override
    public void run() {
        // 1秒減らす
        int timeLeft = LeonGunWar.getPlugin().getManager().getTimeLeft().decrementAndGet();

        // イベントを呼び出す

        Bukkit.getScheduler().runTask(LeonGunWar.getPlugin(), new Runnable() {
            @Override
            public void run() {
                MatchTimeChangedEvent event = new MatchTimeChangedEvent(timeLeft);
                Bukkit.getPluginManager().callEvent(event);
            }
        });

        // -15になったらストップ
        if ( timeLeft == -15 ) {
            cancel();
        }
    }
}

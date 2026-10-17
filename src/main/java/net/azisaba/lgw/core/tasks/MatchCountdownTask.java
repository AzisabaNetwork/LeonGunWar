package net.azisaba.lgw.core.tasks;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.events.MatchTimeChangedEvent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchCountdownTask extends BukkitRunnable {

    @Override
    public void run() {
        // 1秒減らす
        var remaining = LeonGunWar.getPlugin().getManager().getTimeLeft();
        var hijack = LeonGunWar.getPlugin().getHijackController();
        int timeLeft = hijack != null && hijack.isRunning() ? hijack.tickClock()
                : remaining.updateAndGet(value -> value < 0 ? -1 : Math.max(0, value - 1));

        // イベントを呼び出す
        MatchTimeChangedEvent event = new MatchTimeChangedEvent(timeLeft);
        Bukkit.getPluginManager().callEvent(event);

        // 0になったらストップ
        if (timeLeft == 0 && remaining.get() == 0) {
            cancel();
        }
    }
}

package net.azisaba.lgw.core.util;

import net.azisaba.lgw.core.LeonGunWar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ClockMachine {

    public void doubleRewardTaskStarter(){
        scheduleDailyTask(24, 0, this::doubleRewardEndTask);
        scheduleDailyTask(18, 0, this::doubleRewardStartTask);
    }

    private void doubleRewardStartTask(){
        Bukkit.getServer().broadcast(Component.text("-+----報酬取得可能状態になりました！----+-").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
        LeonGunWar.doubleRewardEnable = true;
    }
    private void doubleRewardEndTask(){
        Bukkit.getServer().broadcast(Component.text("-+----報酬取得可能時間が終了しました...----+-").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
        LeonGunWar.doubleRewardEnable = false;
    }

    public static boolean isWithinRewardTime() {
        LocalTime now = LocalTime.now(); // 現在時刻（システム時刻）
        LocalTime start = LocalTime.of(7, 0); // 14:00
        LocalTime end = LocalTime.of(23, 59);   // 18:00

        return !now.isBefore(start) && !now.isAfter(end);
    }

    //1日の指定された時間、分に送られてきたタスクを実行
    public void scheduleDailyTask(int hour, int minute, Runnable task) {
        long currentMillis = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long targetMillis = calendar.getTimeInMillis();
        if (targetMillis < currentMillis) {
            // 翌日のスケジュールに変更
            targetMillis += TimeUnit.DAYS.toMillis(1);
        }

        long initialDelay = targetMillis - currentMillis;
        long oneDayInTicks = 20L * 60 * 60 * 24; // 1日の長さ (20 ticks/秒)

        LeonGunWar.timeTaskList.add(Bukkit.getScheduler().runTaskTimer(LeonGunWar.getPlugin(), task, initialDelay / 50 + 2, oneDayInTicks));
    }
}

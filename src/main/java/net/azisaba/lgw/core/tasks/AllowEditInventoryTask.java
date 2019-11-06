package net.azisaba.lgw.core.tasks;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.SecondOfDay;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AllowEditInventoryTask extends BukkitRunnable {

    // 対象のプレイヤー
    private final Player p;

    // プレイヤーごとの残り時間
    private final Map<Player, Instant> remainTimes;

    // 無敵時間
    private final long duration;

    // ボスバー
    private final Map<Player, BossBar> bossBars = new HashMap<>();

    @Override
    public void run() {
        // ボスバーを作成して取得
        bossBars.putIfAbsent(p, Bukkit.createBossBar("", BarColor.PINK, BarStyle.SOLID));
        BossBar bossBar = bossBars.get(p);

        // 残り時間 (秒) 取得
        long remain = Duration.between(Instant.now(), remainTimes.get(p)).plusSeconds(1).getSeconds();

        // 0以下ならキャンセルしてreturn
        if ( remain <= 0 ) {
            bossBar.removePlayer(p);
            cancel();
            return;
        }

        // 残り秒数を表示
        bossBar.setTitle(Chat.f("&7アイテム整理まで &d» あと &f{0} ", SecondOfDay.f(remain)));
        bossBar.setProgress(remain * 1f / duration);
        bossBar.addPlayer(p);
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        bossBars.computeIfPresent(p, (k, bossBar) -> {
            bossBar.removePlayer(p);
            return null;
        });
        super.cancel();
    }
}

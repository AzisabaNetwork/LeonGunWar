package net.azisaba.lgw.core.tasks;

import java.time.Duration;
import java.time.Instant;
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
public class RespawnKillProtectionTask extends BukkitRunnable {

    // 対象のプレイヤー
    private final Player p;

    // プレイヤーごとの無敵残り時間
    private final Map<Player, Instant> remainTimes;

    // 無敵時間
    private final long duration;

    // ボスバー
    private final Map<Player, BossBar> bossBars;

    @Override
    public void run() {
        // ボスバーを作成して取得
        bossBars.putIfAbsent(p, Bukkit.createBossBar("", BarColor.YELLOW, BarStyle.SOLID));
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
        bossBar.setTitle(Chat.f("&7無敵 &e» あと &f{0} ", SecondOfDay.f(remain)));
        bossBar.setProgress(remain * 1f / duration);
        bossBar.addPlayer(p);
    }
}

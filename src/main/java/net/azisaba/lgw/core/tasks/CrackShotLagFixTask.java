package net.azisaba.lgw.core.tasks;

import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;

public class CrackShotLagFixTask extends BukkitRunnable {

    @Override
    public void run() {
        // 全ワールド
        long removed = Bukkit.getWorlds().stream()
                // ワールド内のラグエンティティ
                .flatMap(world -> world.getEntitiesByClasses(Projectile.class, Explosive.class).stream())
                // 20秒以上経過しているエンティティ
                .filter(entity -> entity.getTicksLived() > 20 * 20)
                // エンティティを削除
                .peek(Entity::remove)
                // カウント
                .count();
        // 削除したエンティティがいる場合
        if (removed > 0) {
            // ログに出力
            LeonGunWar.getPlugin().getLogger().info("定期 -> 不要な " + removed + " 体のエンティティが削除されました。");
        }
    }
}

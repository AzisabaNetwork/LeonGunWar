package net.azisaba.lgw.core.listeners.others;

import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.Arrays;

public class CrackShotLagFixListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        // チャンク内の全エンティティ
        long removed = Arrays.stream(e.getChunk().getEntities())
                // 並列化
                .parallel()
                // ラグエンティティ
                .filter(entity -> entity instanceof Projectile || entity instanceof Explosive)
                // エンティティを削除
                .peek(Entity::remove)
                // カウント
                .count();
        // 削除したエンティティがいる場合
        if (removed > 0) {
            // ログに出力
            LeonGunWar.getPlugin().getLogger().info("不要な " + removed + " 体のエンティティが削除されました。");
        }
    }
}

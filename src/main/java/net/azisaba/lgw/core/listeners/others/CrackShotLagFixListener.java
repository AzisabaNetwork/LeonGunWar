package net.azisaba.lgw.core.listeners.others;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.LgwLog;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.slf4j.Logger;

import java.util.Arrays;

public class CrackShotLagFixListener implements Listener {
    private final Logger logger = LgwLog.getLogger(this.getClass());

    private long removeLagEntities(Entity[] entities) {
        return Arrays.stream(entities)
                // ラグエンティティ
                .filter(entity -> entity instanceof Projectile || entity instanceof Explosive)
                // エンティティを削除
                .peek(Entity::remove)
                // カウント
                .count();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        // 不要なラグエンティティを削除
        long removed = removeLagEntities(e.getChunk().getEntities());
        // 削除したエンティティがいる場合
        if (removed > 0) {
            // ログに出力
            logger.info("チャンクロード -> 不要な " + removed + " 体のエンティティが削除されました。");
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        // 不要なラグエンティティを削除
        long removed = removeLagEntities(e.getChunk().getEntities());
        // 削除したエンティティがいる場合
        if (removed > 0) {
            // ログに出力
            logger.info("チャンクアンロード -> 不要な " + removed + " 体のエンティティが削除されました。");
        }
    }
}

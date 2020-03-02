package net.azisaba.lgw.core.listeners.others;

import java.util.Arrays;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import net.azisaba.lgw.core.LeonGunWar;

public class CrackShotLagFixListener implements Listener {

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
            LeonGunWar.getPlugin().getLogger().info("チャンクロード -> 不要な " + removed + " 体のエンティティが削除されました。");
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        // 不要なラグエンティティを削除
        long removed = removeLagEntities(e.getChunk().getEntities());
        // 削除したエンティティがいる場合
        if (removed > 0) {
            // ログに出力
            LeonGunWar.getPlugin().getLogger().info("チャンクアンロード -> 不要な " + removed + " 体のエンティティが削除されました。");
        }
    }
}

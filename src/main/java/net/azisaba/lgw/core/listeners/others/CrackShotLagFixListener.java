package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import net.azisaba.lgw.core.LeonGunWar;

public class CrackShotLagFixListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        int removed = 0;
        // チャンク内の全エンティティ
        for ( Entity entity : e.getChunk().getEntities() ) {
            // ラグアイテム
            if ( entity instanceof Projectile || entity instanceof Explosive ) {
                // エンティティを削除
                entity.remove();
                removed++;
            }
        }
        // 削除したエンティティがいる場合
        if ( removed > 0 ) {
            // ログに出力
            LeonGunWar.getPlugin().getLogger().info("不要な " + removed + " 体のエンティティが削除されました。");
        }
    }
}

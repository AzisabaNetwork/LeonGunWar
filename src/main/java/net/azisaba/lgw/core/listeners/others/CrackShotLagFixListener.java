package net.azisaba.lgw.core.listeners.others;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import net.azisaba.lgw.core.LeonGunWar;

public class CrackShotLagFixListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        // チャンク内の全エンティティ
        List<Entity> removals = Arrays.stream(e.getChunk().getEntities())
                // ラグアイテム
                .filter(entity -> entity instanceof Snowball || entity instanceof TNTPrimed || entity instanceof Egg)
                // かつ付近にプレイヤーがいない
                .filter(entity -> entity.getNearbyEntities(64, 64, 64).stream()
                        .filter(near -> near instanceof Player)
                        .count() == 0)
                // 収納
                .collect(Collectors.toList());

        if ( !removals.isEmpty() ) {
            // 不要なエンティティを削除
            removals.forEach(Entity::remove);

            // ログに出力
            LeonGunWar.getPlugin().getLogger().info("不要な " + removals.size() + " 体のエンティティが削除されました。");
        }
    }
}

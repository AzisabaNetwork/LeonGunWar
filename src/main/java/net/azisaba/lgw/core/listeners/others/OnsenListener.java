package net.azisaba.lgw.core.listeners.others;

import java.util.Comparator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;

import net.ess3.api.events.AfkStatusChangeEvent;

/**
 * AFK状態が変化したときに温泉にテレポートするListener
 *
 * @author YukiLeafX
 *
 */
public class OnsenListener implements Listener {

    // お湯を探す半径を指定
    private final int ONSEN_RADIUS = 8;

    @EventHandler
    public void onSenHairitai(AfkStatusChangeEvent event) {
        Player player = event.getAffected().getBase();

        boolean afk = event.getValue();
        boolean matching = LeonGunWar.getPlugin().getManager().isPlayerMatching(player);

        // 離席解除のイベントか、試合中の場合はreturn
        if ( !afk || matching ) {
            return;
        }

        Location onsen = LeonGunWar.getPlugin().getSpawnsConfig().getOnsen();

        // 温泉が見つからないか、プレイヤーのワールドが温泉のワールドと同じではない場合はreturn
        if ( onsen == null || onsen.getWorld() != player.getWorld() ) {
            return;
        }

        IntStream.range(-ONSEN_RADIUS, ONSEN_RADIUS)
                // 3次元に拡張する
                .boxed()
                .flatMap(x -> IntStream.range(-ONSEN_RADIUS, ONSEN_RADIUS)
                        .boxed()
                        .flatMap(y -> IntStream.range(-ONSEN_RADIUS, ONSEN_RADIUS)
                                .boxed()
                                .flatMap(z -> Stream.of(onsen.getBlock().getRelative(x, y, z)))))
                // 液体ブロックを検索
                .filter(Block::isLiquid)
                // 場所に変換
                .map(Block::getLocation)
                // 中央から近い順にソート
                .sorted(Comparator.comparingDouble(onsen::distance))
                // プレイヤーが近くにいない場所を検索
                .filter(location -> location.getWorld().getNearbyEntities(location.clone().add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5).stream()
                        .noneMatch(entity -> entity instanceof Player))
                // 最初に見つかったテレポート可能な場所を取得
                .findFirst()
                // テレポート位置の調整
                .map(location -> location.add(onsen.getX() % 1, onsen.getY() % 1, onsen.getZ() % 1))
                .map(location -> location.setDirection(onsen.getDirection()))
                // テレポート
                .ifPresent(location -> {
                    player.teleport(location);
                    player.sendMessage(Chat.f("&c放置しているため温泉に強制送還されました(*^▽^*) ごゆっくり～( ^^) _旦~~"));
                });
    }
}

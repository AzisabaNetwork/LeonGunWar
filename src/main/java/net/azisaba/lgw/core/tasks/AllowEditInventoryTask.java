package net.azisaba.lgw.core.tasks;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.SecondOfDay;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AllowEditInventoryTask extends BukkitRunnable {

    // 対象のプレイヤー
    private final Player p;

    // プレイヤーごとの残り時間
    private final Map<Player, Instant> remainTimes;

    @Override
    public void run() {
        // 残り時間 (秒) 取得
        long remain = Duration.between(Instant.now(), remainTimes.get(p)).plusSeconds(1).getSeconds();

        // 10ではなく5より大きければメッセージを表示しない
        if ( remain != 10 && remain > 5 ) {
            return;
        }

        // 0以下ならキャンセルしてreturn
        if ( remain <= 0 ) {
            p.sendMessage(Chat.f("{0}&7持ち替え時間終了！", LeonGunWar.GAME_PREFIX));
            cancel();
            // 開いているのが自分のインベントリなら閉じる
            if ( p.getOpenInventory() != null && p.getInventory().equals(p.getOpenInventory().getBottomInventory()) ) {
                ItemStack cursor = p.getOpenInventory().getCursor();
                if ( cursor != null ) {
                    p.getOpenInventory().setCursor(null);
                    HashMap<Integer, ItemStack> cannotGiveItems = p.getInventory().addItem(cursor);

                    if ( !cannotGiveItems.isEmpty() ) {
                        Bukkit.getLogger().warning("Cannot give item(s) for " + p.getName());
                        for ( ItemStack item : cannotGiveItems.values() ) {
                            Bukkit.getLogger().warning(item.toString());
                        }
                    }
                }
                p.closeInventory();
            }
            return;
        }

        // 残り秒数を表示
        p.sendMessage(Chat.f("{0}&7持ち替え時間残り &c{1}&7！", LeonGunWar.GAME_PREFIX, SecondOfDay.f(remain)));
    }
}

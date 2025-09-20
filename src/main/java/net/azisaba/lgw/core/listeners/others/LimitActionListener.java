package net.azisaba.lgw.core.listeners.others;

import me.rayzr522.jsonmessage.JSONMessage;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.api.limit.LimitActionAPI;
import net.azisaba.lgw.core.api.util.Chat;
import net.azisaba.lgw.core.commands.LimitCommand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * 運営の不必要な動きを制限するListener
 *
 * @author siloneco, sysnote8main
 */
public class LimitActionListener implements Listener {
    private final LeonGunWar plugin;

    public LimitActionListener(LeonGunWar plugin) {
        this.plugin = plugin;
    }

    // アイテムドロップ時
    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();

        // 権限がない場合return
        if (hasPermission(p)) return;
        // allowDropPlayersに含まれていない場合はキャンセル
        if (!limitActionAPI().isAllowedDrop(p.getUniqueId())) {
            e.setCancelled(true);
            JSONMessage.actionbar(Chat.f("&cアイテムをドロップするには /limit drop と入力"), p);
        }
    }

    // ブロック設置時
    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        // 権限がない場合return
        if (hasPermission(p)) return;
        // allowBuildPlayersに含まれていない場合はキャンセル
        if (!limitActionAPI().isAllowedBuild(p.getUniqueId())) {
            e.setCancelled(true);
            JSONMessage.actionbar(Chat.f("&c建築をするには /limit build と入力"), p);
        }
    }

    // ブロック破壊時
    @EventHandler
    public void onPlace(BlockBreakEvent e) {
        Player p = e.getPlayer();

        // 権限がない場合return
        if (hasPermission(p)) return;
        // allowBuildPlayersに含まれていない場合はキャンセル
        if (!limitActionAPI().isAllowedBuild(p.getUniqueId())) {
            e.setCancelled(true);
            JSONMessage.actionbar(Chat.f("&c建築をするには /limit build と入力"), p);
        }
    }

    // 額縁破壊時
    @EventHandler
    public void preventDestroyItemFrame(EntityDamageByEntityEvent e) {
        // Entityが額縁ではない場合return
        if (!(e.getEntity() instanceof ItemFrame)) {
            return;
        }

        Player p = null;

        // 攻撃者がプレイヤーの場合
        if (e.getDamager() instanceof Player) {
            p = (Player) e.getDamager();
        }
        // 攻撃者が発射物の場合
        if (e.getDamager() instanceof Projectile) {
            // ソースがプレイヤーの場合
            if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
                p = (Player) ((Projectile) e.getDamager()).getShooter();
            }
        }

        // プレイヤーではない場合return
        if (p == null) {
            return;
        }

        // 権限がない場合return
        if (hasPermission(p)) return;
        // allowBuildPlayersに含まれていない場合はキャンセル
        if (!limitActionAPI().isAllowedBuild(p.getUniqueId())) {
            e.setCancelled(true);
            JSONMessage.actionbar(Chat.f("&c建築をするには /limit build と入力"), p);
        }
    }

    // 空の額縁破壊時
    @EventHandler
    public void preventDestroyEmptyFrame(HangingBreakByEntityEvent e) {
        // 壊したのがプレイヤーではない場合return
        if (!(e.getRemover() instanceof Player p)) {
            return;
        }

        // プレイヤーを取得

        // 権限がない場合return
        if (hasPermission(p)) return;
        // allowBuildPlayersに含まれていない場合はキャンセル
        if (!limitActionAPI().isAllowedBuild(p.getUniqueId())) {
            e.setCancelled(true);
            JSONMessage.actionbar(Chat.f("&c建築をするには /limit build と入力"), p);
        }
    }

    @EventHandler
    public void onLeft(PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        limitActionAPI().removePlayerData(id);
    }

    private boolean hasPermission(Player player) {
        return player.hasPermission(LimitCommand.PERMISSION);
    }

    private LimitActionAPI limitActionAPI() {
        return plugin.getLimitActionAPI();
    }
}

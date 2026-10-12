package net.azisaba.lgw.core.util;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.BooleanSupplier;
import java.util.logging.Level;

/** 試合から離れる際の状態解除と、そのサーバー内のロビーへの帰還。 */
public final class MatchPlayerReturn {
    private MatchPlayerReturn() { }

    public static void reset(Player player) {
        // TPできなくてもチーム防具と外部プラグイン向けのタグは残さない。
        player.getInventory().setChestplate(null);
        player.removeScoreboardTag("red");
        player.removeScoreboardTag("blue");
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
        restoreVitals(player);
    }

    private static void restoreVitals(Player player) {
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setFoodLevel(20);
        if (!player.isDead()) {
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) player.setHealth(maxHealth.getValue());
        }
    }

    public static void returnToLobby(Plugin plugin, Player player, Location lobby, BooleanSupplier mayReturn) {
        if (!player.isOnline()) return;
        try {
            if (lobby == null || lobby.getWorld() == null) {
                reportFailure(plugin, player, "spawns.yml の lobby 地点またはワールドが利用できません", null);
                return;
            }
        } catch (IllegalArgumentException exception) {
            reportFailure(plugin, player, "ロビーのワールドがアンロードされています", exception);
            return;
        }
        Location destination = lobby.clone();
        if (player.isDead()) {
            player.spigot().respawn();
            // リスポーンの完了後に移動する。次の試合に参加済みなら古い帰還処理を捨てる。
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline() || player.isDead() || !mayReturn.getAsBoolean()) return;
                player.getInventory().setChestplate(null);
                // 終了処理で付けた「次の試合にエントリー中」の表示名を上書きしない。
                restoreVitals(player);
                teleport(plugin, player, destination);
            });
        } else if (mayReturn.getAsBoolean()) {
            teleport(plugin, player, destination);
        }
    }

    private static void teleport(Plugin plugin, Player player, Location destination) {
        try {
            if (!player.teleport(destination)) {
                reportFailure(plugin, player, "ロビーへのテレポートがキャンセルされました", null);
            }
        } catch (RuntimeException exception) {
            reportFailure(plugin, player, "ロビーへのテレポートに失敗しました", exception);
        }
    }

    private static void reportFailure(Plugin plugin, Player player, String reason, RuntimeException exception) {
        plugin.getLogger().log(Level.WARNING, player.getName() + ": " + reason, exception);
        player.sendMessage(Chat.f("&cロビーへ戻れませんでした。運営に連絡してください。"));
    }
}

package net.azisaba.lgw.core.listeners.others;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;

/**
 * 試合中に釣りをできなくするListener
 *
 * @author siloneco
 */
public class NoFishingOnFightListener implements Listener {

    @EventHandler
    public void onInteract(PlayerFishEvent e) {
        Player p = e.getPlayer();

        // 試合プレイヤーに含まれていない場合return
        if ( !LeonGunWar.getPlugin().getManager().isPlayerMatching(p) ) {
            return;
        }

        // イベントキャンセル
        e.setCancelled(true);
        p.sendMessage(Chat.f("&c試合中に釣りをすることはできません！"));
    }
}

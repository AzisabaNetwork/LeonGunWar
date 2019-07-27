package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerVelocityEvent;

/**
 * ノックバックを無効化するためのクラス
 *
 * @author siloneco
 *
 */
public class NoKnockbackListener implements Listener {

    /**
     * プレイヤーがノックバックしたときにキャンセルするリスナー
     */
    @EventHandler
    public void onPlayerVelocity(PlayerVelocityEvent e) {
        Player p =e.getPlayer();
        p.sendMessage("Velocity: "+e.getVelocity().toString()+" / "+p.getVelocity()+"、Direction: "+p.getLocation().getDirection());
    }
}

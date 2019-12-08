package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;
import net.ess3.api.events.AfkStatusChangeEvent;

public class OnsenListener implements Listener {

    @EventHandler
    public void onSenHairitai(AfkStatusChangeEvent event) {
        Player player = event.getAffected().getBase();
        boolean afk = event.getValue();
        boolean matching = LeonGunWar.getPlugin().getManager().isPlayerMatching(player);
        if ( afk && !matching ) {
            Location onsen = LeonGunWar.getPlugin().getSpawnsConfig().getOnsen();
            if ( onsen != null && onsen.getWorld() == player.getWorld() ) {
                player.teleport(onsen);
                player.sendMessage(Chat.f("&c放置しているため温泉に強制送還されました(*^▽^*) ごゆっくり～( ^^) _旦~~"));
            }
        }
    }
}

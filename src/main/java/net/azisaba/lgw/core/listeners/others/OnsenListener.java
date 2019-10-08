package net.azisaba.lgw.core.listeners.others;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;
import net.ess3.api.events.AfkStatusChangeEvent;

public class OnsenListener implements Listener {

    @EventHandler
    public void onSenHairitai(AfkStatusChangeEvent e) {
        if ( e.getValue() ) {
            Player p = e.getAffected().getBase();
            Optional.ofNullable(LeonGunWar.getPlugin().getSpawnsConfig().getOnsen())
                    .ifPresent(p::teleport);
            p.sendMessage(Chat.f("&c放置しているため温泉に強制送還されました。(*^▽^*) ごゆっくり～( ^^) _旦~~"));
        }
    }
}

package net.azisaba.lgw.core.listeners.others;

import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;

import net.azisaba.lgw.core.util.StrikesCooldown;
import net.azisaba.lgw.core.utils.Chat;

import me.rayzr522.jsonmessage.JSONMessage;

public class FixStrikesCooldownListener implements Listener {

    private final Map<UUID, List<StrikesCooldown>> playerCooldowns = new HashMap<>();

    @EventHandler
    public void onCooldown(WeaponPrepareShootEvent event) {
        Player player = event.getPlayer();
        if ( player == null ) {
            return;
        }

        List<StrikesCooldown> cooldowns = playerCooldowns.getOrDefault(player.getUniqueId(), new ArrayList<>());

        StrikesCooldown cooldown = cooldowns.stream()
                .filter(next -> next.getWeaponTitle().equals(event.getWeaponTitle()))
                .findFirst()
                .orElseGet(() -> {
                    StrikesCooldown created = new StrikesCooldown(event.getWeaponTitle());
                    cooldowns.add(created);
                    playerCooldowns.put(player.getUniqueId(), cooldowns);
                    return created;
                });

        if ( cooldown.isNowInCooldown() ) {
            event.setCancelled(true);
            JSONMessage.actionbar(Chat.f("&cこの武器はクールダウン中です！"), player);
        } else if ( cooldown.isEnabled() ) {
            cooldown.setLastUsed(System.currentTimeMillis());
        }
    }
}

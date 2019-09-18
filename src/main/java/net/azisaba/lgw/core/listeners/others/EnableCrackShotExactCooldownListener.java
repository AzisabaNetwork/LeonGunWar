package net.azisaba.lgw.core.listeners.others;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;

import net.azisaba.lgw.core.util.WeaponCooldown;

import jp.azisaba.lgw.kdstatus.utils.Chat;
import me.rayzr522.jsonmessage.JSONMessage;

public class EnableCrackShotExactCooldownListener implements Listener {

    private final Map<UUID, List<WeaponCooldown>> playerCooldowns = new HashMap<>();

    @EventHandler
    public void onCooldown(WeaponPrepareShootEvent event) {
        Player player = event.getPlayer();
        if ( player == null ) {
            return;
        }

        List<WeaponCooldown> cooldowns = playerCooldowns.getOrDefault(player.getUniqueId(), new ArrayList<>());

        WeaponCooldown cooldown = cooldowns.stream()
                .filter(next -> next.getWeaponTitle().equals(event.getWeaponTitle()))
                .findFirst()
                .orElseGet(() -> {
                    WeaponCooldown created = new WeaponCooldown(event.getWeaponTitle());
                    cooldowns.add(created);
                    playerCooldowns.put(player.getUniqueId(), cooldowns);
                    return created;
                });

        if ( cooldown.isNowInCooldown() ) {
            event.setCancelled(true);
            JSONMessage.actionbar(Chat.f("&cこの武器はクールダウン中です！"), player);
        } else {
            cooldown.setLastUsed(System.currentTimeMillis());
        }
    }
}

package net.azisaba.lgw.core.listeners.weaponcontrols;

import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.events.MatchStartedEvent;
import net.azisaba.lgw.core.utils.Chat;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class LimitOneShotPerMatchListener implements Listener {

    private final Map<String, Map<UUID, Integer>> weaponUsedCount = new HashMap<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onShoot(WeaponPrepareShootEvent e) {
        Player p = e.getPlayer();
        if (!LeonGunWar.getPlugin().getManager().isPlayerMatching(p)) {
            return;
        }

        int allowedCount = LeonGunWar.getPlugin().getWeaponControlConfig()
            .getMaxUseCount(e.getWeaponTitle());

        if (allowedCount < 0) {
            return;
        }

        Map<UUID, Integer> everyoneUsedCountMap = weaponUsedCount.get(e.getWeaponTitle());
        int playerUsedCount;

        if (everyoneUsedCountMap == null) {
            playerUsedCount = 0;
            everyoneUsedCountMap = new HashMap<>();
            weaponUsedCount.put(e.getWeaponTitle(), everyoneUsedCountMap);
        } else {
            playerUsedCount = everyoneUsedCountMap.getOrDefault(p.getUniqueId(), 0);
        }

        if (playerUsedCount >= allowedCount) {
            e.setCancelled(true);

            if (playerUsedCount > 0) {
                p.sendMessage(
                    Chat.f("{0}&cこの武器は1試合に{1}回までしか使用できません！", LeonGunWar.GAME_PREFIX,
                        allowedCount));
            } else {
                p.sendMessage(
                    Chat.f("{0}&cこの武器は試合で使用できません！", LeonGunWar.GAME_PREFIX));
            }

            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        everyoneUsedCountMap.put(p.getUniqueId(), playerUsedCount + 1);
    }

    @EventHandler
    public void onMatchStart(MatchStartedEvent e) {
        weaponUsedCount.clear();
    }
}

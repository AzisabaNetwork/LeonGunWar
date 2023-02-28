package net.azisaba.lgw.core.listeners.weaponcontrols;

import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private final HashMap<String, List<UUID>> alreadyUsed = new HashMap<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onShoot(WeaponPrepareShootEvent e) {
        Player p = e.getPlayer();
        if (!LeonGunWar.getPlugin().getManager().isPlayerMatching(p)) {
            return;
        }

        if (!LeonGunWar.getPlugin().getWeaponControlConfig().getWeaponsLimitOnlyOncePerMatch()
            .contains(e.getWeaponTitle())) {
            return;
        }

        List<UUID> alreadyUsedPlayers = alreadyUsed.get(e.getWeaponTitle());

        if (alreadyUsedPlayers == null) {
            alreadyUsedPlayers = new ArrayList<>();
            alreadyUsedPlayers.add(p.getUniqueId());
            alreadyUsed.put(e.getWeaponTitle(), alreadyUsedPlayers);
            return;
        }

        if (alreadyUsedPlayers.contains(p.getUniqueId())) {
            e.setCancelled(true);
            p.sendMessage(Chat.f("{0}&cこの武器は1試合に1回までしか使用できません！", LeonGunWar.GAME_PREFIX));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        alreadyUsedPlayers.add(p.getUniqueId());
    }

    @EventHandler
    public void onMatchStart(MatchStartedEvent e) {
        alreadyUsed.clear();
    }
}

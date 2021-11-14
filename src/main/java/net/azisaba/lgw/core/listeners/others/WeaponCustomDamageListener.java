package net.azisaba.lgw.core.listeners.others;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.utils.WeaponData;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;

public class WeaponCustomDamageListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWeaponDamage(WeaponDamageEntityEvent e){

        WeaponData data = LeonGunWar.getPlugin().getWeaponConfig().getWeaponData(e.getWeaponTitle());

        if(data != null){

            e.setDamage(data.getDamage());

        }

    }

    @EventHandler
    public void onFinishMatch(MatchFinishedEvent e){

        LeonGunWar.getPlugin().getWeaponConfig().refreshCache();

    }

}

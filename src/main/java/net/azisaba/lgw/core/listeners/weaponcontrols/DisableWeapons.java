package net.azisaba.lgw.core.listeners.weaponcontrols;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;

import net.azisaba.lgw.core.LeonGunWar;

public class DisableWeapons implements Listener {

    @EventHandler
    public void onUse(WeaponPrepareShootEvent e){

        if( !LeonGunWar.getPlugin().getManager().isMatching() ){
            e.setCancelled(true);
        }

    }

}

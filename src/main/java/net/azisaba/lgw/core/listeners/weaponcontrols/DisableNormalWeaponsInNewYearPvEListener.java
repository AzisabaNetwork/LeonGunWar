package net.azisaba.lgw.core.listeners.weaponcontrols;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;

import jp.azisaba.lgw.kdstatus.utils.Chat;

public class DisableNormalWeaponsInNewYearPvEListener implements Listener {

    private final CSDirector cs = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");

    @EventHandler
    public void onWeaponPrepareShoot(WeaponPrepareShootEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if ( world == null || !world.getName().equals("NYPVE") ) {
            return;
        }

        String weapon = event.getWeaponTitle();
        String ctrl = cs.getString(weapon + ".Item_Information.Inventory_Control");

        if ( ctrl == null ) {
            return;
        }

        String[] groups = ctrl.replaceAll(" ", "").split(",");

        if ( !Arrays.asList(groups).contains("PVE_Weapons") ) {
            player.sendMessage(Chat.f("&c正月PvEでは専用アイテムしか使用できません！"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeaponDamageEntity(WeaponDamageEntityEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if ( world == null || !world.getName().equals("NYPVE") ) {
            return;
        }

        String weapon = event.getWeaponTitle();
        String ctrl = cs.getString(weapon + ".Item_Information.Inventory_Control");

        if ( ctrl == null ) {
            return;
        }

        String[] groups = ctrl.replaceAll(" ", "").split(",");

        if ( !Arrays.asList(groups).contains("PVE_Weapons") ) {
            player.sendMessage(Chat.f("&c正月PvEでは専用アイテムしか使用できません！"));
            event.setCancelled(true);
        }
    }
}

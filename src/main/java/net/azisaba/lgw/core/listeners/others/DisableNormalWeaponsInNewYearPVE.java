package net.azisaba.lgw.core.listeners.others;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;

import net.azisaba.lgw.core.LeonGunWar;

import jp.azisaba.lgw.kdstatus.utils.Chat;

public class DisableNormalWeaponsInNewYearPVE implements Listener {

    private final CSDirector cs = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");

    @EventHandler
    public void onWeaponPrepareShoot(WeaponPrepareShootEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if ( world != null && world.getName().equals("NYPVE") ) {
            return;
        }

        String weapon = event.getWeaponTitle();
        String ctrl = cs.getString(weapon + ".Item_Information.Inventory_Control");

        if ( ctrl == null ) {
            return;
        }

        String[] groups = ctrl.replaceAll(" ", "").split(",");

        if ( !Arrays.asList(groups).contains("PVE_Weapons") ) {
            player.sendMessage(Chat.f("{0}&c正月PVEでは限定アイテムしか使用できません！", LeonGunWar.GAME_PREFIX));
            event.setCancelled(true);
        }
    }
}

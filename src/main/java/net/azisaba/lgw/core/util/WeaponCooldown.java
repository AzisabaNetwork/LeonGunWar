package net.azisaba.lgw.core.util;

import org.bukkit.Bukkit;

import com.shampaggon.crackshot.CSDirector;

import lombok.Data;

@Data
public class WeaponCooldown {

    private static final long MILLIS_IN_TICK = 50;

    private final String weaponTitle;
    private long lastUsed;

    public int getWeaponCooldownTicks() {
        if ( Bukkit.getPluginManager().isPluginEnabled("CrackShot") ) {
            CSDirector cs = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");
            String cooldownNode = weaponTitle + ".Shooting.Delay_Between_Shots";
            return cs.getInt(cooldownNode);
        } else {
            return -1;
        }
    }

    public boolean isEnabled() {
        return getWeaponCooldownTicks() > 0;
    }

    public boolean isNowInCooldown() {
        return lastUsed > 0 && lastUsed + MILLIS_IN_TICK * getWeaponCooldownTicks() >= System.currentTimeMillis();
    }
}

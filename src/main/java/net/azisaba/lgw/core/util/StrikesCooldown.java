package net.azisaba.lgw.core.util;

import java.time.Duration;

import org.bukkit.Bukkit;

import com.shampaggon.crackshot.CSDirector;

import lombok.Data;

@Data
public class StrikesCooldown {

    private final String weaponTitle;
    private long lastUsed;

    public long getStrikesCooldown() {
        if ( Bukkit.getPluginManager().isPluginEnabled("CrackShot") ) {
            CSDirector cs = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");
            String cooldownNode = weaponTitle + ".Airstrikes.Multiple_Strikes.Delay_Between_Strikes";
            int cooldown = cs.getInt(cooldownNode);
            return Duration.ofSeconds(cooldown).toMillis();
        } else {
            return -1;
        }
    }

    public boolean isEnabled() {
        return getStrikesCooldown() > 0;
    }

    public boolean isNowInCooldown() {
        return lastUsed > 0 && lastUsed + getStrikesCooldown() >= System.currentTimeMillis();
    }
}

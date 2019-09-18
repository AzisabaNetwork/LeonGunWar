package net.azisaba.lgw.core.util;

import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;

import com.shampaggon.crackshot.CSDirector;

import lombok.Data;

@Data
public class StrikesCooldown {

    private final String weaponTitle;
    private long lastUsed;

    public int getStrikesCooldownSeconds() {
        if ( Bukkit.getPluginManager().isPluginEnabled("CrackShot") ) {
            CSDirector cs = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");
            String cooldownNode = weaponTitle + ".Airstrikes.Multiple_Strikes.Delay_Between_Strikes";
            return cs.getInt(cooldownNode);
        } else {
            return -1;
        }
    }

    public boolean isEnabled() {
        return getStrikesCooldownSeconds() > 0;
    }

    public boolean isNowInCooldown() {
        return lastUsed > 0 && lastUsed + DateUtils.MILLIS_PER_SECOND * getStrikesCooldownSeconds() >= System.currentTimeMillis();
    }
}

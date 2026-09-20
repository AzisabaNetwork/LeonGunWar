package net.azisaba.lgw.core.util;

import net.azisaba.crackshot.CrackShot;
import lombok.Data;
import org.bukkit.Bukkit;

import java.time.Duration;

@Data
public class StrikesCooldown {

    private final String weaponTitle;
    private long lastUsed;

    public long getStrikesCooldown() {
        if (Bukkit.getPluginManager().isPluginEnabled("CrackShot")) {
            CrackShot cs = (CrackShot) Bukkit.getPluginManager().getPlugin("CrackShot");
            String cooldownNode = weaponTitle + ".Airstrikes.Multiple_Strikes.Delay_Between_Strikes";
            int cooldown = cs.data.getInt(cooldownNode);
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

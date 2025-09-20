package net.azisaba.lgw.core.api.integration;

import com.shampaggon.crackshot.CSDirector;
import org.bukkit.Bukkit;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

public class CrackShotAPI {
    private static final CrackShotAPI API = new CrackShotAPI();
    public static CrackShotAPI getApi() {
        return API;
    }

    /**
     * Plugin name of CrackShot
     */
    public static final String CRACKSHOT_PL = "CrackShot";

    public boolean isLoaded() {
        return Bukkit.getPluginManager().isPluginEnabled(CRACKSHOT_PL);
    }

    @Nullable
    public CSDirector crackShot() {
        return (CSDirector) Bukkit.getPluginManager().getPlugin(CRACKSHOT_PL);
    }

    /**
     * Get weapon's strikes cooldown
     * @param weaponTitle title of weapon
     * @return millisecond. if failure, returns -1.
     */
    public long getStrikesCooldown(String weaponTitle) {
        var cs = crackShot();
        if(cs != null) {
            String cooldownNode = weaponTitle + ".Airstrikes.Multiple_Strikes.Delay_Between_Strikes";
            int cooldown = cs.getInt(cooldownNode);
            return Duration.ofSeconds(cooldown).toMillis();
        } else {
            return -1;
        }
    }
}

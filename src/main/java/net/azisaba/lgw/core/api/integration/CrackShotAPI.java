package net.azisaba.lgw.core.api.integration;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.CSUtility;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

/**
 * Entrypoint of CrackShot integration
 */
public class CrackShotAPI {
    private static final CrackShotAPI API = new CrackShotAPI();

    public static CrackShotAPI getApi() {
        return API;
    }

    /**
     * Plugin name of CrackShot
     */
    public static final String CRACKSHOT_PL = "CrackShot";

    private final CSUtility csUtility = new CSUtility();

    protected CrackShotAPI() {
    }

    /**
     * Is CrackShot Loaded
     * @return is loaded
     */
    public boolean isLoaded() {
        return Bukkit.getPluginManager().isPluginEnabled(CRACKSHOT_PL);
    }

    /**
     * Get CSDirector Instance
     * @return {@link CSDirector}. If failure, null.
     */
    @Nullable
    public CSDirector crackShot() {
        return (CSDirector) Bukkit.getPluginManager().getPlugin(CRACKSHOT_PL);
    }

    /**
     * Get weapon's strikes cooldown
     *
     * @param weaponTitle title of weapon
     * @return millisecond. if failure, returns -1.
     */
    public long getStrikesCooldown(String weaponTitle) {
        var cs = crackShot();
        if (cs != null) {
            String cooldownNode = weaponTitle + ".Airstrikes.Multiple_Strikes.Delay_Between_Strikes";
            int cooldown = cs.getInt(cooldownNode);
            return Duration.ofSeconds(cooldown).toMillis();
        } else {
            return -1;
        }
    }

    /**
     * Get string in specific path
     *
     * @param path target path
     * @return literal. If not found, null.
     */
    @Nullable
    public String getString(String path) {
        var cs = crackShot();
        if (cs == null) return null;
        return cs.getString(path);
    }

    /**
     * Wrapper of {@link CSUtility#getWeaponTitle(ItemStack)}
     *
     * @param itemStack target weapon stack
     * @return weapon title of target stack
     */
    public String getWeaponTitle(ItemStack itemStack) {
        return csUtility.getWeaponTitle(itemStack);
    }
}

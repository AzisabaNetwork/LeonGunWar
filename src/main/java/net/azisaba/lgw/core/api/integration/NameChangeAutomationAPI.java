package net.azisaba.lgw.core.api.integration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.azisaba.namechange.config.NameChangeInfoIO;
import net.azisaba.namechange.data.NameChangeInfoData;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

public class NameChangeAutomationAPI {
    private static final NameChangeAutomationAPI API = new NameChangeAutomationAPI();
    public static NameChangeAutomationAPI getApi() {
        return API;
    }

    private final NameChangeInfoIO nameInfoIO = new NameChangeInfoIO();
    private final LoadingCache<String, NameChangeInfoData> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(Duration.ofMinutes(15))
            .build(nameInfoIO::load);

    /**
     * Get base weapon's name
     * @param weaponTitle title of weapon
     * @return name of base weapon. If failure, null.
     */
    @Nullable
    public String getBaseWeapon(String weaponTitle) {
        var nameInfo = cache.get(weaponTitle);
        if(nameInfo == null) return null;
        return nameInfo.getBaseWeapon();
    }
}

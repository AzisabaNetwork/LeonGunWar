package net.azisaba.lgw.core.util;

import lombok.Data;
import net.azisaba.lgw.core.api.integration.CrackShotAPI;

@Data
public class StrikesCooldown {

    private final String weaponTitle;
    private long lastUsed;

    public long getStrikesCooldown() {
        return CrackShotAPI.getApi().getStrikesCooldown(weaponTitle);
    }

    public boolean isEnabled() {
        return getStrikesCooldown() > 0;
    }

    public boolean isNowInCooldown() {
        return lastUsed > 0 && lastUsed + getStrikesCooldown() >= System.currentTimeMillis();
    }
}

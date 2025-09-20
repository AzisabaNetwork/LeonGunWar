package net.azisaba.lgw.core.api.limit;

import net.azisaba.lgw.core.LeonGunWar;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NullMarked
public class LimitActionAPI {
    public static LimitActionAPI getApi() {
        return LeonGunWar.getPlugin().getLimitActionAPI();
    }

    private final List<UUID> allowDropPlayers = new ArrayList<>();
    private final List<UUID> allowBuildPlayers = new ArrayList<>();

    /**
     * ドロップの許可を切り替え、最新の状態を返します
     * @param playerUuid player's uuid
     * @return allowed -> true, else -> false
     */
    public boolean toggleAllowDrop(UUID playerUuid) {
        if(isAllowedDrop(playerUuid)) {
            allowDropPlayers.remove(playerUuid);
        } else {
            allowDropPlayers.add(playerUuid);
        }
        return isAllowedDrop(playerUuid);
    }

    /**
     * 建築の許可を切り替え、最新の状態を返します
     * @param playerUuid player's uuid
     * @return allowed -> true, else false
     */
    public boolean toggleAllowBuild(UUID playerUuid) {
        if(isAllowedBuild(playerUuid)) {
            allowBuildPlayers.remove(playerUuid);
        } else {
            allowBuildPlayers.add(playerUuid);
        }
        return isAllowedBuild(playerUuid);
    }

    /**
     * ドロップが許可されているかどうか
     * @param playerUuid player's uuid
     * @return allowed -> true, else -> false
     */
    public boolean isAllowedDrop(UUID playerUuid) {
        return allowDropPlayers.contains(playerUuid);
    }

    /**
     * 建築が許可されているかどうか
     * @param playerUuid player's uuid
     * @return allowed -> true, else -> false
     */
    public boolean isAllowedBuild(UUID playerUuid) {
        return allowBuildPlayers.contains(playerUuid);
    }

    public void removePlayerData(UUID playerUuid) {
        allowBuildPlayers.remove(playerUuid);
        allowDropPlayers.remove(playerUuid);
    }
}

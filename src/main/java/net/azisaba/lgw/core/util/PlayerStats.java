package net.azisaba.lgw.core.util;

import java.util.UUID;

public class PlayerStats {

    private final UUID uuid;
    private String name;
    private int level;
    private int xps;
    private int coins;
    private int angleOfDeathLevel;

    public PlayerStats(UUID uuid, String name, int level, int xps, int coins, int angleOfDeathLevel){

        this.uuid = uuid;
        this.name = name;
        this.level = level;
        this.xps = xps;
        this.coins = coins;

        this.angleOfDeathLevel = angleOfDeathLevel;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getXps() {
        return xps;
    }

    public int getCoins() {
        return coins;
    }

    public int getAngleOfDeathLevel() {
        return angleOfDeathLevel;
    }
}

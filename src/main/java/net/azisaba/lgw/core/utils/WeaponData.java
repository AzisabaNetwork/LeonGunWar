package net.azisaba.lgw.core.utils;

public class WeaponData {

    private final String name;
    private final double damage;
    private final long renzokuDamageLong;
    private final double renzokuDamageRatio;
    private final long hitgoMuteki;

    public WeaponData(String name, double damage, long renzokuDamageLong, double renzokuDamageRatio,long hitgoMuteki){
        this.name = name;
        this.damage = damage;
        this.renzokuDamageLong = renzokuDamageLong;
        this.renzokuDamageRatio = renzokuDamageRatio;
        this.hitgoMuteki = hitgoMuteki;
    }

    public String getName() {
        return name;
    }

    public double getDamage() {
        return damage;
    }

    public long getRenzokuDamageLong() {
        return renzokuDamageLong;
    }

    public double getRenzokuDamageRatio() {
        return renzokuDamageRatio;
    }

    public long getHitgoMuteki() {
        return hitgoMuteki;
    }
}

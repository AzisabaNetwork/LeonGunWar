package net.azisaba.lgw.core.utils;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Damage {

    public void damageNaturally(LivingEntity entity, double amount, Entity source) {
        double toughness = entity.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
        double defensePoints = entity.getAttribute(Attribute.GENERIC_ARMOR).getValue();
        double damage = amount * (1 - Math.min(20, Math.max(defensePoints / 5, defensePoints - amount / (2 + toughness / 4))) / 25);
        entity.damage(damage, source);
    }
}

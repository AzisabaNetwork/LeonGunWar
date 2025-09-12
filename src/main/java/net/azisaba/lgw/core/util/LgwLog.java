package net.azisaba.lgw.core.util;

import net.azisaba.lgw.core.LeonGunWar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LgwLog {
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(String.format("%s:%s", LeonGunWar.PL_ID, clazz.getName()));
    }
}

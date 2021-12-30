package net.azisaba.lgw.core.configs;

import java.io.File;
import java.util.HashMap;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.WeaponData;

public class WeaponsConfig {

    HashMap<String, WeaponData> weaponsMap = new HashMap<>();
    HashMap<String,WeaponData> cache = new HashMap<>();

    public WeaponsConfig(File dataFolder){

        File weaponsFolder = new File(dataFolder, "weapons/");
        if(!weaponsFolder.exists()) {
            weaponsFolder.mkdir();
        }

        if(weaponsFolder.listFiles().length == 0){
            System.out.print("[LeonGunWar] [WARN] Not found Weapons file.");
        }else {
            for ( File file : weaponsFolder.listFiles() ) {
                if ( file.isFile() ) {
                    Configuration configuration = YamlConfiguration.loadConfiguration(file);
                    configuration.getKeys(true).forEach(key -> {

                        String name = configuration.getString(key);
                        double damage = configuration.getDouble(key + ".damage");
                        long renzokuDamageLong = configuration.getLong(key + ".renzoku-damage-long");
                        double renzokuDamageRatio = configuration.getDouble(key + ".renzoku-damage-ratio");
                        long hitgoMuteki = configuration.getLong(key + ".hit-go-muteki");

                        weaponsMap.put(key,new WeaponData(name,damage,renzokuDamageLong,renzokuDamageRatio,hitgoMuteki));

                    });
                }
            }
            //LeonGunWar.getPlugin().getLogger().info(String.format("[Weapons] Loaded %s weapons data.", weaponsMap.size()));
        }

    }

    public WeaponData getWeaponData(String name){

        if(cache.containsKey(name)){
            return cache.get(name);
        }else {
            WeaponData data = weaponsMap.getOrDefault(name,null);
            cache.put(name,data);
            return data;
        }

    }

    public void refreshCache(){

        cache.clear();

    }

}

package net.azisaba.lgw.core.configs;

import lombok.SneakyThrows;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.configuration.InvalidConfigurationException;

import javax.security.auth.login.Configuration;
import java.io.IOException;
import java.util.HashMap;

public class SyogoConfig extends Config {

    public final HashMap<String,String> syogos = new HashMap<>();

    public SyogoConfig(LeonGunWar plugin) {
        super(plugin, "configs/syogo.yml","syogo.yml");
    }

    @SneakyThrows(value = { Exception.class })
    @Override
    public void loadConfig() throws IOException, InvalidConfigurationException {
        super.loadConfig();
        for ( String syogo : config.getValues(false).keySet() ) {
            syogos.put(syogo,config.getString(syogo));
        }
        LeonGunWar.getPlugin().getLogger().info("称号を " + syogos.size() + " 個読み込みました");
    }

    public void add(String syogo,String display){
        syogos.put(syogo,display);
        config.set(syogo,display);
    }

    public void delete(String syogo){
        syogos.remove(syogo);
        config.set(syogo,null);
    }

}

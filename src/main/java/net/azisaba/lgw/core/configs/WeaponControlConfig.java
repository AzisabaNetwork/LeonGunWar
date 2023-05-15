package net.azisaba.lgw.core.configs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.configuration.ConfigurationSection;

public class WeaponControlConfig extends Config {

  private Map<String, Integer> rateLimitedWeapons;

  public WeaponControlConfig(@NonNull LeonGunWar plugin) {
    super(plugin, "configs/weaponControl.yml", "weaponControl.yml");
  }

  @SneakyThrows(value = {Exception.class})
  @Override
  public void loadConfig() {
    super.loadConfig();

    ConfigurationSection section = config.getConfigurationSection("rateLimitedWeapons");
    if (section == null) {
      rateLimitedWeapons = Collections.emptyMap();
    } else {
      rateLimitedWeapons = new HashMap<>();
      for (String key : section.getKeys(false)) {
        rateLimitedWeapons.put(key, section.getInt(key));
      }

      rateLimitedWeapons = Collections.unmodifiableMap(rateLimitedWeapons);
    }
  }

  public int getMaxUseCount(String weaponName) {
    return rateLimitedWeapons.getOrDefault(weaponName, -1);
  }
}

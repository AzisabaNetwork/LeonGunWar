package net.azisaba.lgw.core.configs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.azisaba.lgw.core.LeonGunWar;

@Getter
public class WeaponControlConfig extends Config {

  @Getter
  private List<String> weaponsLimitOnlyOncePerMatch;

  public WeaponControlConfig(@NonNull LeonGunWar plugin) {
    super(plugin, "configs/weaponControl.yml", "weaponControl.yml");
  }

  @SneakyThrows(value = {Exception.class})
  @Override
  public void loadConfig() {
    super.loadConfig();

    if (!config.isSet("weaponsLimitOnlyOncePerMatch")) {
      weaponsLimitOnlyOncePerMatch = new ArrayList<>();
    } else {
      weaponsLimitOnlyOncePerMatch = Collections.unmodifiableList(
          config.getStringList("weaponsLimitOnlyOncePerMatch"));
    }
  }
}

package net.azisaba.lgw.core.configs;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.TimeCondition;
import org.bukkit.configuration.InvalidConfigurationException;

@Getter
public class KillStreaksConfig extends Config {

  private Map<Integer, Map.Entry<List<String>, List<String>>> streaks;
  private Map<Integer, Map.Entry<List<String>, List<String>>> levels;

  private Map<TimeCondition, Map<Integer, Map.Entry<List<String>, List<String>>>> timeConditionedStreaks;
  private Map<TimeCondition, Map<Integer, Map.Entry<List<String>, List<String>>>> timeConditionedLevels;

  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


  private String removed;

  public KillStreaksConfig(@NonNull LeonGunWar plugin) {
    super(plugin, "configs/killStreaks.yml", "killStreaks.yml");
  }

  @SneakyThrows(value = {Exception.class})
  @Override
  public void loadConfig() throws IOException, InvalidConfigurationException {
    super.loadConfig();

    streaks = new HashMap<>();
    config.getConfigurationSection("streaks").getValues(false).keySet().stream()
        .map(Integer::valueOf)
        .collect(Collectors.toMap(Function.identity(), count -> {
          List<String> messages = config.getStringList("streaks." + count + ".messages");
          List<String> commands = config.getStringList("streaks." + count + ".commands");
          return new AbstractMap.SimpleEntry<>(messages, commands);
        }))
        .forEach(streaks::put);
    streaks = Collections.unmodifiableMap(streaks);

    levels = new HashMap<>();
    config.getConfigurationSection("levels").getValues(false).keySet().stream()
        .map(Integer::valueOf)
        .collect(Collectors.toMap(Function.identity(), count -> {
          List<String> messages = config.getStringList("levels." + count + ".messages");
          List<String> commands = config.getStringList("levels." + count + ".commands");
          return new AbstractMap.SimpleEntry<>(messages, commands);
        }))
        .forEach(levels::put);
    levels = Collections.unmodifiableMap(levels);

    timeConditionedStreaks = new HashMap<>();
    if (config.getConfigurationSection("timeConditionedStreaks") != null) {
      for (String key : config.getConfigurationSection("timeConditionedStreaks").getKeys(false)) {
        Date start = dateFormat.parse(key.split("-")[0].trim());
        Date end = dateFormat.parse(key.split("-")[1].trim());

        if (start == null || end == null) {
          continue;
        }
        if (start.after(end)) {
          continue;
        }
        if (end.getTime() < System.currentTimeMillis()) {
          continue;
        }
        TimeCondition condition = new TimeCondition(start, end);

        config.getConfigurationSection("timeConditionedStreaks." + key).getValues(false).keySet()
            .stream()
            .map(Integer::valueOf)
            .collect(Collectors.toMap(Function.identity(), count -> {
              List<String> messages = config.getStringList(
                  "timeConditionedStreaks." + key + "." + count + ".messages");
              List<String> commands = config.getStringList(
                  "timeConditionedStreaks." + key + "." + count + ".commands");
              return new AbstractMap.SimpleEntry<>(messages, commands);
            }))
            .forEach((count, entry) ->
                timeConditionedStreaks.computeIfAbsent(condition, k -> new HashMap<>())
                    .put(count, entry));
      }
    }

    timeConditionedLevels = new HashMap<>();
    if (config.getConfigurationSection("timeConditionedLevels") != null) {
      for (String key : config.getConfigurationSection("timeConditionedLevels").getKeys(false)) {
        Date start = dateFormat.parse(key.split("-")[0].trim());
        Date end = dateFormat.parse(key.split("-")[1].trim());

        if (start == null || end == null) {
          continue;
        }
        if (start.after(end)) {
          continue;
        }
        if (end.getTime() < System.currentTimeMillis()) {
          continue;
        }
        TimeCondition condition = new TimeCondition(start, end);

        config.getConfigurationSection("timeConditionedLevels." + key).getValues(false).keySet()
            .stream()
            .map(Integer::valueOf)
            .collect(Collectors.toMap(Function.identity(), count -> {
              List<String> messages = config.getStringList(
                  "timeConditionedLevels." + key + "." + count + ".messages");
              List<String> commands = config.getStringList(
                  "timeConditionedLevels." + key + "." + count + ".commands");
              return new AbstractMap.SimpleEntry<>(messages, commands);
            }))
            .forEach((count, entry) ->
                timeConditionedLevels.computeIfAbsent(condition, k -> new HashMap<>())
                    .put(count, entry));
      }
    }

    removed = config.getString("removed");
  }
}

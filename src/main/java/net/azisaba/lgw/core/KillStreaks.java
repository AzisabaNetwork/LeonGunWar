package net.azisaba.lgw.core;

import net.azisaba.lgw.core.util.BroadcastUtils;
import net.azisaba.lgw.core.api.util.Chat;
import net.azisaba.lgw.core.battlesystem.MatchMode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class KillStreaks {

    private final Map<UUID, AtomicInteger> streaksMap = new HashMap<>();

    public void removedBy(Player player, Player killer) {
        int streaks = get(player).get();
        int minStreaks = LeonGunWar.getPlugin().getKillStreaksConfig().getStreaks().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);

        if (killer != null && streaks >= minStreaks) {
            BroadcastUtils.broadcast(
                    Chat.f(LeonGunWar.getPlugin().getKillStreaksConfig().getRemoved(),
                            LeonGunWar.GAME_PREFIX,
                            killer.getPlayerListName(), player.getPlayerListName()));
        }

        streaksMap.remove(player.getUniqueId());
    }

    public AtomicInteger get(Player player) {
        streaksMap.putIfAbsent(player.getUniqueId(), new AtomicInteger(0));
        return streaksMap.get(player.getUniqueId());
    }

    private void giveRewards(int streaks, Player player) {
        LeonGunWar.getPlugin().getKillStreaksConfig().getStreaks().entrySet().stream()
                .filter(entry -> streaks == entry.getKey())
                .map(Map.Entry::getValue)
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .map(command -> Chat.f(command, player.getName()))
                .forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        LeonGunWar.getPlugin().getKillStreaksConfig().getTimeConditionedStreaks().entrySet()
                .stream()
                .filter(entry -> entry.getKey().isDuring())
                .map(Map.Entry::getValue)
                .flatMap(map -> map.entrySet().stream())
                .filter(entry -> streaks == entry.getKey())
                .map(Map.Entry::getValue)
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .map(command -> Chat.f(command, player.getName()))
                .forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

        LeonGunWar.getPlugin().getKillStreaksConfig().getLevels().entrySet().stream()
                .filter(entry -> streaks % entry.getKey() == 0)
                .map(Map.Entry::getValue)
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .map(command -> Chat.f(command, player.getName()))
                .forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        LeonGunWar.getPlugin().getKillStreaksConfig().getTimeConditionedLevels().entrySet().stream()
                .filter(entry -> entry.getKey().isDuring())
                .map(Map.Entry::getValue)
                .flatMap(map -> map.entrySet().stream())
                .filter(entry -> streaks % entry.getKey() == 0)
                .map(Map.Entry::getValue)
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .map(command -> Chat.f(command, player.getName()))
                .forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }

    public void add(Player player) {
        // カウントを追加
        int streaks = get(player).incrementAndGet();

        // 報酬を付与
        if (LeonGunWar.doubleRewardEnable) {
            giveRewards(streaks, player);
        }
        giveRewards(streaks, player);

        if (LeonGunWar.getPlugin().getManager().getMatchMode()
                == MatchMode.LEADER_DEATH_MATCH_POINT) {
            if (LeonGunWar.getPlugin().getManager().getLDMLeaderMap().containsValue(player)) {
                if (LeonGunWar.doubleRewardEnable) {
                    player.sendMessage(
                            Chat.f("{0}&7あなたはリーダーなので &e2倍 &7の報酬を受け取りました!(報酬ブーストは適用されていません)", LeonGunWar.GAME_PREFIX));
                } else {
                    player.sendMessage(
                            Chat.f("{0}&7あなたはリーダーなので &e2倍 &7の報酬を受け取りました！", LeonGunWar.GAME_PREFIX));
                    giveRewards(streaks, player);
                }
            }
        }

        // キルストリークをお知らせ
        LeonGunWar.getPlugin().getKillStreaksConfig().getStreaks().entrySet().stream()
                .filter(entry -> streaks == entry.getKey())
                .map(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .flatMap(List::stream)
                .map(message -> Chat.f(message, LeonGunWar.GAME_PREFIX, player.getPlayerListName()))
                .forEach(BroadcastUtils::broadcast);
        LeonGunWar.getPlugin().getKillStreaksConfig().getTimeConditionedStreaks().entrySet()
                .stream()
                .filter(entry -> entry.getKey().isDuring())
                .map(Map.Entry::getValue)
                .flatMap(map -> map.entrySet().stream())
                .filter(entry -> streaks == entry.getKey())
                .map(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .flatMap(List::stream)
                .map(message -> Chat.f(message, LeonGunWar.GAME_PREFIX, player.getPlayerListName()))
                .forEach(BroadcastUtils::broadcast);
        LeonGunWar.getPlugin().getKillStreaksConfig().getLevels().entrySet().stream()
                .filter(entry -> streaks % entry.getKey() == 0)
                .map(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .flatMap(List::stream)
                .map(message -> Chat.f(message, LeonGunWar.GAME_PREFIX, player.getPlayerListName()))
                .forEach(BroadcastUtils::broadcast);
        LeonGunWar.getPlugin().getKillStreaksConfig().getTimeConditionedLevels().entrySet().stream()
                .filter(entry -> entry.getKey().isDuring())
                .map(Map.Entry::getValue)
                .flatMap(map -> map.entrySet().stream())
                .filter(entry -> streaks % entry.getKey() == 0)
                .map(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .flatMap(List::stream)
                .map(message -> Chat.f(message, LeonGunWar.GAME_PREFIX, player.getPlayerListName()))
                .forEach(BroadcastUtils::broadcast);
    }
}

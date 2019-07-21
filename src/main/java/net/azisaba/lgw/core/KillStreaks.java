package net.azisaba.lgw.core;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.azisaba.lgw.core.utils.Chat;

public class KillStreaks {

    private final Map<UUID, AtomicInteger> streaksMap = new HashMap<>();

    public void removedBy(Player player, Player killer) {
        int streaks = get(player).get();
        int minStreaks = LeonGunWar.getPlugin().getKillStreaksConfig().getStreaks().entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);

        if ( killer != null && streaks >= minStreaks ) {
            Bukkit.broadcastMessage(
                    Chat.f(LeonGunWar.getPlugin().getKillStreaksConfig().getRemoved(), LeonGunWar.GAME_PREFIX,
                            killer.getDisplayName(), player.getDisplayName()));
        }

        streaksMap.remove(player.getUniqueId());
    }

    public AtomicInteger get(Player player) {
        streaksMap.putIfAbsent(player.getUniqueId(), new AtomicInteger(0));
        return streaksMap.get(player.getUniqueId());
    }

    public void add(Player player) {
        // カウントを追加
        int streaks = get(player).incrementAndGet();

        // 報酬を付与
        LeonGunWar.getPlugin().getKillStreaksConfig().getStreaks().entrySet().stream()
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

        // キルストリークをお知らせ
        LeonGunWar.getPlugin().getKillStreaksConfig().getStreaks().entrySet().stream()
                .filter(entry -> streaks == entry.getKey())
                .map(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .flatMap(List::stream)
                .map(message -> Chat.f(message, LeonGunWar.GAME_PREFIX, player.getDisplayName()))
                .forEach(Bukkit::broadcastMessage);
        LeonGunWar.getPlugin().getKillStreaksConfig().getLevels().entrySet().stream()
                .filter(entry -> streaks % entry.getKey() == 0)
                .map(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .flatMap(List::stream)
                .map(message -> Chat.f(message, LeonGunWar.GAME_PREFIX, player.getDisplayName()))
                .forEach(Bukkit::broadcastMessage);
    }
}

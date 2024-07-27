package net.azisaba.lgw.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.azisaba.lgw.core.utils.BroadcastUtils;
import net.azisaba.lgw.core.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AssistStreaks {

    private final Map<UUID, AtomicInteger> streaksMap = new HashMap<>();

    public void removedBy(Player player, Player killer) {
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
        LeonGunWar.getPlugin().getAssistStreaksConfig().getLevels().entrySet().stream()
                .filter(entry -> streaks % entry.getKey() == 0)
                .map(Map.Entry::getValue)
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .map(command -> Chat.f(command, player.getName()))
                .forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

        // アシストストリークをお知らせ
        LeonGunWar.getPlugin().getAssistStreaksConfig().getLevels().entrySet().stream()
            .filter(entry -> streaks % entry.getKey() == 0)
            .map(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .flatMap(List::stream)
            .map(message -> Chat.f(message, LeonGunWar.GAME_PREFIX, player.getPlayerListName()))
            .forEach(BroadcastUtils::broadcast);
    }
}

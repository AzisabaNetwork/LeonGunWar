package net.azisaba.lgw.core.util;

import lombok.Data;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Function;

@Data
public class KillReward {

    private final Function<Player, String> message;
    private final Function<Player, List<String>> commands;
}

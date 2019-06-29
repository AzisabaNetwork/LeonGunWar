package net.azisaba.lgw.core.util;

import java.util.List;
import java.util.function.Function;

import org.bukkit.entity.Player;

import lombok.Data;

@Data
public class KillReward {

    private final Function<Player, String> message;
    private final Function<Player, List<String>> commands;
}

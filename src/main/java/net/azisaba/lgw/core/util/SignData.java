package net.azisaba.lgw.core.util;

import lombok.Data;
import org.bukkit.Location;

import java.util.UUID;

@Data
@Deprecated(forRemoval = true, since = "4.0.0")
public class SignData {

    private final Location location;
    private final String playerName;
    private final UUID author;
    private final long breakAt;
}

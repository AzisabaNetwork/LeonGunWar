package net.azisaba.lgw.core.util;

import java.util.UUID;
import lombok.Data;
import org.bukkit.Location;

@Data
public class SignData {

  private final Location location;
  private final String playerName;
  private final UUID author;
  private final long breakAt;
}

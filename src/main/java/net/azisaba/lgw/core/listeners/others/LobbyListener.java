package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.scoreboard.Team;

public class LobbyListener implements Listener {

    private final Scoreboard scoreboard;

    public LobbyListener() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String prefix = PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix%");
        ChatColor lastColor = getLastColor(prefix);
        if (lastColor == null) {
            lastColor = ChatColor.WHITE;
        }
        if (scoreboard.getTeam(lastColor.name()) == null) {
            Team team = scoreboard.registerNewTeam(lastColor.name());
            team.setColor(lastColor);
            if(prefix.length() <= 16) {
                team.setPrefix(prefix);
            }else{
                team.setPrefix(lastColor.toString());
            }
        }
        scoreboard.getTeam(lastColor.name()).addEntry(player.getName());
        player.setScoreboard(this.scoreboard);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (scoreboard.getEntryTeam(player.getName()) != null) {
            scoreboard.getEntryTeam(player.getName()).removeEntry(player.getName());
        }
    }

    private ChatColor getLastColor(String input) {
        int length = input.length();
        for (int index = length - 1; index >= 0; index--) {
            char section = input.charAt(index);
            if (section == '§' && index < length - 1) {
                char c = input.charAt(index + 1);
                if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9' || c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e' || c == 'f') {
                    return ChatColor.getByChar(c);
                }
            }
        }
        return ChatColor.RESET;
    }

}

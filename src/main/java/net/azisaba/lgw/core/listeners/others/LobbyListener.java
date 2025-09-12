package net.azisaba.lgw.core.listeners.others;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class LobbyListener implements Listener {

    private final Scoreboard scoreboard;

    public LobbyListener() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        LuckPerms lp = LuckPermsProvider.get();
        User user = lp.getUserManager().getUser(player.getUniqueId());
        Group group = lp.getGroupManager().getGroup(user.getPrimaryGroup());
        String prefix = user.getCachedData().getMetaData().getPrefix();
        String teamName = group.getName();
        if (prefix == null) {
            prefix = "";
        }
        if (player.hasPermission("group.nitro")) {
            teamName = player.getName();
        }
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            ChatColor color = this.getLastColor(prefix.replace("&", "§"));
            team.setColor(color);
            if (prefix.length() <= 16) {
                team.setPrefix(prefix.replace("&", "§"));
            } else {
                team.setPrefix(color.toString());
            }
        }
        scoreboard.getTeam(teamName).addEntry(player.getName());
        player.setScoreboard(this.scoreboard);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Team team = scoreboard.getTeam(player.getName());
        if (team != null) {
            team.removeEntry(player.getName());
        }
    }

    private ChatColor getLastColor(String input) {
        int length = input.length();
        for (int index = length - 1; index >= 0; index--) {
            char section = input.charAt(index);
            if (section == '§' && index < length - 1) {
                char c = input.charAt(index + 1);
                if (Character.digit(c, 16) != -1) {
                    return ChatColor.getByChar(c);
                }
            }
        }
        return ChatColor.RESET;
    }

}

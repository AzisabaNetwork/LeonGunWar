package net.azisaba.lgw.core.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class LGWExpansion extends PlaceholderExpansion {
    private final LeonGunWar plugin;

    public LGWExpansion(LeonGunWar plugin){
        this.plugin = plugin;
    }


    @Override
    @NotNull
    public String getAuthor() {
        return "Arisa9006";
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "LGW";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0"; //
    }

    @Override
    public boolean persist() {
        return true; //
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {//
        //各チームに所属していたらプレースホルダに対応したカラーコードを表示させる所属していなかったら白
        if(player.getPlayer().getScoreboard().getEntryTeam(player.getName()) == null){
            return  "";
        }
        if(player.getPlayer().getScoreboard().getEntryTeam(player.getName()).getColor() == ChatColor.DARK_RED){
            return "§4";
        }else
        if(player.getPlayer().getScoreboard().getEntryTeam(player.getName()).getColor() == ChatColor.BLUE){
            return "§9";
        }else {
            return "§f";
        }
    }
}

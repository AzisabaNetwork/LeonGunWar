package net.azisaba.lgw.core.events;

import lombok.Getter;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerKillEvent extends Event {

    @Getter
    private final Player player;
    @Getter
    private final String weaponTitle;
    @Getter
    private final int killStreaks;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public PlayerKillEvent(Player player, String weaponTitle){
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.killStreaks = LeonGunWar.getPlugin().getKillStreaks().get(player).get();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

}

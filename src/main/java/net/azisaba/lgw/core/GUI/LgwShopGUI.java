package net.azisaba.lgw.core.GUI;

import org.bukkit.entity.Player;

public class LgwShopGUI {

    private final Player player;

    public LgwShopGUI(Player player) {
        this.player = player;
    }

    public void open(){



    }

    public enum Phase{

        MENU,
        SR,
        SG,
        LMG,
        SMG,
        AR,
        DMR,
        HG,
        GRENADE,
        KNIFE,
        OTHER

    }

}

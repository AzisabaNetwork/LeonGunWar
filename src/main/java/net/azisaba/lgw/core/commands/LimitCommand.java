package net.azisaba.lgw.core.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

// you can use multiple alias with | ex. limit|limitaction
@CommandAlias("limit")
@CommandPermission(LimitCommand.PERMISSION)
@SuppressWarnings("unused")
public class LimitCommand extends BaseCommand {
    public static final String PERMISSION = "leongunwar.cmd.limit";

    @Default
    private void onDefault(Player player) {
        player.sendMessage(Component.text("Usage: /limit <mode>"));
    }

    @Subcommand("build")
    @Description("Toggle build limit")
    public class Build extends BaseCommand {
        @Dependency
        private LeonGunWar plugin;

        @Default
        private void onDefault(Player player) {
            toggle(player);
        }

        @Subcommand("toggle")
        private void toggle(Player player) {
            boolean newState = plugin.getLimitActionAPI().toggleAllowBuild(player.getUniqueId());
            player.sendMessage(MessageUtil.getAbleComponent("アイテムドロップ", newState));
        }
    }

    @Subcommand("build")
    @Description("Toggle drop limit")
    public class Drop extends BaseCommand {
        @Dependency
        private LeonGunWar plugin;

        @Default
        private void onDefault(Player player) {
            toggle(player);
        }

        @Subcommand("toggle")
        private void toggle(Player player) {
            boolean newState = plugin.getLimitActionAPI().toggleAllowDrop(player.getUniqueId());
            player.sendMessage(MessageUtil.getAbleComponent("建築", newState));
        }
    }
}

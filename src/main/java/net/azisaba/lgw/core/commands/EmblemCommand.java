package net.azisaba.lgw.core.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import net.azisaba.lgw.core.LeonGunWar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("emblem|lsyogo")
public class EmblemCommand extends BaseCommand {
    @Dependency
    private LeonGunWar plugin;

    @Subcommand("give")
    @CommandCompletion("@players @emblems")
    private void give(Player player, OfflinePlayer targetPlayer, String emblemId) {
        String emblemDisplayName = plugin.getSyogoConfig().syogos.get(emblemId);
        if(emblemDisplayName == null) {
            player.sendMessage(Component.text("称号" + emblemId + "が見つかりませんでした。").color(NamedTextColor.RED));
            return;
        }
        // Todo: add emblem logic
    }
}

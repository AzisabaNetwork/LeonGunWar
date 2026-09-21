package net.azisaba.lgw.core.commands;

import net.azisaba.lgw.core.util.AdventureUtil;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.Args;
import net.azisaba.lgw.core.util.BroadcastUtils;
import net.azisaba.lgw.core.util.Chat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SiaiTuutiCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lavel, String[] args) {

        if (!LeonGunWar.getPlugin().getMainConfig().isLobby) {
            return false;
        }

        if (Args.isEmpty(args)) {
            return true;
        }

        // sv1なら
        if (Args.check(args, 0, "sv1")) {
            Component msg = createJoinMessage(1, "lgw2sv1");
            BroadcastUtils.broadcast(msg, Sound.BLOCK_NOTE_BLOCK_PLING);
            return true;
        }
        // sv2なら
        if (Args.check(args, 0, "sv2")) {
            Component msg = createJoinMessage(2, "lgw2sv2");
            BroadcastUtils.broadcast(msg, Sound.BLOCK_NOTE_BLOCK_PLING);
            return true;
        }
        // sv3なら
        if (Args.check(args, 0, "sv3")) {
            Component msg = createJoinMessage(3, "lgw2sv3");
            BroadcastUtils.broadcast(msg, Sound.BLOCK_NOTE_BLOCK_PLING);
            return true;
        }
        return true;
    }

    private Component createJoinMessage(int serverNumber, String serverName) {
        return AdventureUtil.legacy(Chat.f("&a&l試合サーバー{0}で試合が開始されました!", serverNumber))
                .append(AdventureUtil.legacy(Chat.f("&b[クリックで参加]"))
                        .clickEvent(ClickEvent.runCommand("/server " + serverName)));
    }
}

package net.azisaba.lgw.core.commands;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Args;
import net.azisaba.lgw.core.utils.BroadcastUtils;
import net.azisaba.lgw.core.utils.Chat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SiaiTuutiCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,String lavel, String[] args) {

        if(!LeonGunWar.getPlugin().getMainConfig().isLobby){
            return false;
        }

        if ( Args.isEmpty(args) ) {
            return true;
        }

        // sv1なら
        if ( Args.check(args, 0, "sv1") ) {
            Component cmt = Component.text(Chat.f("&a&l試合サーバー1で試合が開始されました!"));
            cmt = cmt.append(Component.text(Chat.f("&b[クリックで参加]"))).clickEvent(ClickEvent.runCommand("/server lgw2sv1"));
            BroadcastUtils.broadcast(cmt, Sound.BLOCK_NOTE_BLOCK_PLING);
            return true;
        }
        // sv2なら
        if ( Args.check(args, 0, "sv2") ) {
            Component cmt = Component.text(Chat.f("&a&l試合サーバー2で試合が開始されました!"));
            cmt = cmt.append(Component.text(Chat.f("&b[クリックで参加]"))).clickEvent(ClickEvent.runCommand("/server lgw2sv2"));
            BroadcastUtils.broadcast(cmt, Sound.BLOCK_NOTE_BLOCK_PLING);
            return true;
        }
        return true;
    }
}

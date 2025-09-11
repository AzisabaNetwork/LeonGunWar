package net.azisaba.lgw.core.commands;

import me.rayzr522.jsonmessage.JSONMessage;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.Args;
import net.azisaba.lgw.core.util.BroadcastUtils;
import net.azisaba.lgw.core.util.Chat;
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
            JSONMessage msg = JSONMessage.create(Chat.f("&a&l試合サーバー1で試合が開始されました!"));
            msg.suggestCommand("")
                    .then(Chat.f("&b[クリックで参加]"))
                    .runCommand("/server lgw2sv1");
            BroadcastUtils.broadcast(msg, Sound.BLOCK_NOTE_BLOCK_PLING);
            return true;
        }
        // sv2なら
        if ( Args.check(args, 0, "sv2") ) {
            JSONMessage msg = JSONMessage.create(Chat.f("&a&l試合サーバー2で試合が開始されました!"));
            msg.suggestCommand("")
                    .then(Chat.f("&b[クリックで参加]"))
                    .runCommand("/server lgw2sv2");
            BroadcastUtils.broadcast(msg, Sound.BLOCK_NOTE_BLOCK_PLING);
            return true;
        }
        // sv3なら
        if ( Args.check(args, 0, "sv3") ) {
            JSONMessage msg = JSONMessage.create(Chat.f("&a&l試合サーバー3で試合が開始されました!"));
            msg.suggestCommand("")
                    .then(Chat.f("&b[クリックで参加]"))
                    .runCommand("/server lgw2sv3");
            BroadcastUtils.broadcast(msg, Sound.BLOCK_NOTE_BLOCK_PLING);
            return true;
        }
        return true;
    }
}

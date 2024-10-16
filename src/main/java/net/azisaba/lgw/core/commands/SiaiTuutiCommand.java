package net.azisaba.lgw.core.commands;

import me.rayzr522.jsonmessage.JSONMessage;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.configs.MainConfig;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.Args;
import net.azisaba.lgw.core.utils.BroadcastUtils;
import net.azisaba.lgw.core.utils.Chat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            BroadcastUtils.broadcast(msg);
            return true;
        }
        // sv2なら
        if ( Args.check(args, 0, "sv2") ) {
            JSONMessage msg = JSONMessage.create(Chat.f("&a&l試合サーバー2で試合が開始されました!"));
            msg.suggestCommand("")
                    .then(Chat.f("&b[クリックで参加]"))
                    .runCommand("/server lgw2sv2");
            BroadcastUtils.broadcast(msg);
            return true;
        }
        return true;
    }
}

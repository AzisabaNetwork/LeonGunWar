package net.azisaba.lgw.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;

public class MapVoteCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // プレイヤーではない場合return
        if ( !(sender instanceof Player) ) {
            sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ実行可能です！"));
            return true;
        }

        // 投票が行われていない場合はreturn
        if ( !LeonGunWar.getPlugin().getMapSelectCountdown().isRunning() ) {
            sender.sendMessage(Chat.f("&c現在マップ投票は行われていません！"));
            return true;
        }

        // 投票先が指定されていない場合はreturn
        if ( args.length <= 0 ) {
            sender.sendMessage(Chat.f("&c投票先を番号で指定してください！"));
            return true;
        }

        // 入力されたStringをintに変換
        int index;
        try {
            index = Integer.parseInt(args[0]) - 1;
        } catch ( NumberFormatException expected ) {
            sender.sendMessage(Chat.f("&c有効な数字を指定してください！"));
            return true;
        }

        // 値が0以下の場合や投票できる最大番号より大きい場合はreturn
        if ( index < 0 || index >= LeonGunWar.getPlugin().getMapSelectCountdown().getMaps().size() ) {
            sender.sendMessage(Chat.f("&c有効な数字を指定してください！"));
            return true;
        }

        // 投票を反映
        LeonGunWar.getPlugin().getMapSelectCountdown().vote((Player) sender, index);

        // メッセージを送信
        String mapName = LeonGunWar.getPlugin().getMapSelectCountdown().getMaps().get(index).getMapName();
        sender.sendMessage(Chat.f("{0} &e{1} &7に投票しました！", LeonGunWar.GAME_PREFIX, mapName));
        return true;
    }
}

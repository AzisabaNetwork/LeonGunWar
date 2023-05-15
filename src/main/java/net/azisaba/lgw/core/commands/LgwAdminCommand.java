package net.azisaba.lgw.core.commands;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import me.rayzr522.jsonmessage.JSONMessage;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.Args;
import net.azisaba.lgw.core.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

public class LgwAdminCommand implements CommandExecutor, TabCompleter {

    // ミスって本家で実行してしまうとまずいので/lgw debug_startにロックをかけれるように
    private static final boolean ALLOW_DEBUG = false;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO helpメッセージ実装
        if ( Args.isEmpty(args) ) {
            return true;
        }

        // debug_startなら
        if ( Args.check(args, 0, "debug_start") ) {

            // allowDebugがfalseならreturn
            if ( !ALLOW_DEBUG ) {
                sender.sendMessage(Chat.f("&cこの引数は現在無効化されているため実行できません！"));
                return true;
            }

            // 試合中ならreturn
            if ( LeonGunWar.getPlugin().getManager().isMatching() ) {
                return true;
            }
            // サーバー内のプレイヤーを試合に参加
            Bukkit.getOnlinePlayers().forEach(p -> LeonGunWar.getPlugin().getManager().addEntryPlayer(p));

            // モード指定されてなければTDMに指定
            if ( LeonGunWar.getPlugin().getManager().getMatchMode() == null ) {
                LeonGunWar.getPlugin().getManager().setMatchMode(MatchMode.TEAM_DEATH_MATCH);
            }

            // カウントダウン終了
            LeonGunWar.getPlugin().getMatchStartCountdown().stopCountdown();

            // 試合開始
            LeonGunWar.getPlugin().getManager().startMatch();
            return true;
        }

        // teleportかtpなら
        if ( Args.check(args, 0, "teleport", "tp") ) {

            // senderがプレイヤーではない場合はreturn
            if ( !(sender instanceof Player) ) {
                sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ有効です！"));
                return true;
            }

            Player p = (Player) sender;

            // マップ名が指定されていない場合はreturn
            if ( args.length <= 1 ) {
                p.sendMessage(Chat.f("&cマップ名を指定してください！"));
                return true;
            }

            // args[1]移行を取得
            String mapName = String.join(" ", args).substring(args[0].length() + 1);

            // 指定されたマップがない場合はreturn
            List<GameMap> correctMapList = LeonGunWar.getPlugin().getMapsConfig().getAllGameMap().stream()
                    .filter(map -> map.getMapName().equalsIgnoreCase(mapName)
                            || map.getMapName().equalsIgnoreCase(mapName.replace(" ", "_")))
                    .collect(Collectors.toList());

            // サイズが1ならテレポート
            if ( correctMapList.size() == 1 ) {
                p.teleport(correctMapList.get(0).getSpawnPoint(BattleTeam.values()[0]));
                p.sendMessage(Chat.f("&e{0} &7にテレポートしました。", correctMapList.get(0).getMapName()));

                // 1より多い場合
            } else if ( correctMapList.size() > 1 ) {
                p.sendMessage(Chat.f("&cマッチしたマップが2つあります"));

                // 各マップのJSONMessageを表示
                correctMapList.forEach(map -> {
                    Location spawn = map.getSpawnPoint(BattleTeam.values()[0]);
                    JSONMessage msg = JSONMessage.create(Chat.f("&7 - &e{0}: &7{1}, {2}, {3} &7({4})", map.getMapName(),
                            spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getWorld().getName()));
                    msg.tooltip(Chat.f("&eクリックでテレポート"));
                    msg.runCommand(Chat.f("/essentials:tppos {0} {1} {2} {3} {4} {5}", spawn.getX(), spawn.getY(),
                            spawn.getZ(), spawn.getYaw(), spawn.getPitch(), spawn.getWorld().getName()));

                    // メッセージを送信
                    msg.send(p);
                });

                // 1より少ない場合 (0以下の場合)
            } else {
                p.sendMessage(Chat.f("&c指定したマップが見つかりませんでした。"));
            }

            return true;
        }

        // reloadなら
        if ( Args.check(args, 0, "reload", "rl") ) {

            try {
                // マップの再読み込み
                LeonGunWar.getPlugin().getMapsConfig().loadConfig();

                // Lobby Spawnの読み込み
                LeonGunWar.getPlugin().getSpawnsConfig().loadConfig();

                // 設定ファイルの読み込み
                LeonGunWar.getPlugin().getKillStreaksConfig().loadConfig();
                LeonGunWar.getPlugin().getAssistStreaksConfig().loadConfig();
                LeonGunWar.getPlugin().getWeaponControlConfig().loadConfig();
            } catch ( IOException | InvalidConfigurationException exception ) {
                exception.printStackTrace();
            }

            sender.sendMessage(Chat.f("{0}&a設定とマップのリロードが完了しました。", LeonGunWar.GAME_PREFIX));
            return true;
        }

        // 1つ目の引数がshowdataの場合
        if ( Args.check(args, 0, "showdata") ) {

            // 試合中ではない場合return
            if ( !LeonGunWar.getPlugin().getManager().isMatching() ) {
                sender.sendMessage(Chat.f("{0}&7現在試合をしていないためマッチデータの閲覧はできません。", LeonGunWar.GAME_PREFIX));
                return true;
            }

            MatchManager manager = LeonGunWar.getPlugin().getManager();

            // マッチタイプを取得
            MatchMode mode = manager.getMatchMode();

            // マッチデータを表示 その1
            sender.sendMessage(Chat.f("{0}&cMatch Data: {1}\n", LeonGunWar.GAME_PREFIX, mode.getModeName()));

            // バトルチームのデータ取得
            for ( BattleTeam team : manager.getTeamPlayers().keySet() ) {

                // プレイヤーの数を取得
                int playerCount = manager.getTeamPlayers().get(team).size();

                // チームパワーレベルを取得
                int teampowerlevel = manager.getTeamPowerLevel(manager.getScoreboardTeam(team));

                // チームエースパワーレベルを取得
                int teamacepowerlevel = manager.getTeamAcePowerLevel(manager.getScoreboardTeam(team));

                // マッチのポイントを取得
                int matchpoint = manager.getCurrentTeamPoint(team);

                // (もしリーダーデスマッチなら)リーダーの名前を取得
                String leadername = Chat.f("&4NOT_LEADER_DEATH_MATCH");

                if ( manager.getMatchMode() == MatchMode.LEADER_DEATH_MATCH ) {

                    // リーダーを取得
                    Player leader = manager.getLDMLeader(team);

                    // リーダーが存在するなら
                    if ( leader != null ) {
                        leadername = leader.getDisplayName();
                    }
                }

                sender.sendMessage(Chat.f("{0} {1}&e データ\n" +
                        "{0}&eチーム人数: §6{2}人\n" +
                        "{0}&eチームパワーレベル: §6{3}\n" +
                        "{0}&eチームエースパワーレベル: §6{4}\n" +
                        "{0}&e現在のポイント: §6{5}\n" +
                        "{0}&eチームリーダー: §6{6}\n", LeonGunWar.GAME_PREFIX, team.getTeamName(), playerCount, teampowerlevel, teamacepowerlevel, matchpoint, leadername));
            }
            sender.sendMessage(Chat.f("{0}&cMatch Data: {1}", LeonGunWar.GAME_PREFIX, mode.getModeName()));
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if ( args.length == 1 ) {
            return Args.complete(args, 0, "debug_start", "teleport", "tp", "reload", "rl");
        }
        if ( args.length == 2 && Args.check(args, 0, "teleport", "tp") ) {
            return Args.complete(args, 1, LeonGunWar.getPlugin().getMapsConfig().getAllGameMap().stream()
                    .map(GameMap::getMapName)
                    .toArray(String[]::new));
        }
        return null;
    }
}

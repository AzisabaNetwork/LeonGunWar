package net.azisaba.lgw.core.commands;

import me.rayzr522.jsonmessage.JSONMessage;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.battlesystem.MatchManager;
import net.azisaba.lgw.core.util.Args;
import net.azisaba.lgw.core.battlesystem.BattleTeam;
import net.azisaba.lgw.core.api.util.Chat;
import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.battlesystem.MatchMode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LgwAdminCommand implements CommandExecutor, TabCompleter {

    // ミスって本家で実行してしまうとまずいので/lgw debug_startにロックをかけれるように
    private static final boolean ALLOW_DEBUG = false;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (Args.isEmpty(args)) {
            // TODO helpメッセージ実装
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            // ==========================
            // debug_start
            // ==========================
            case "debug_start" -> {
                if (!ALLOW_DEBUG) {
                    sender.sendMessage(Chat.f("&cこの引数は現在無効化されているため実行できません！"));
                    return true;
                }
                if (LeonGunWar.getPlugin().getManager().isMatching()) {
                    return true;
                }
                Bukkit.getOnlinePlayers().forEach(p ->
                        LeonGunWar.getPlugin().getManager().addEntryPlayer(p)
                );
                if (LeonGunWar.getPlugin().getManager().getMatchMode() == null) {
                    LeonGunWar.getPlugin().getManager().setMatchMode(MatchMode.TEAM_DEATH_MATCH);
                }
                LeonGunWar.getPlugin().getMatchStartCountdown().stopCountdown();
                LeonGunWar.getPlugin().getManager().startMatch();
                return true;
            }

            // ==========================
            // teleport / tp
            // ==========================
            case "teleport", "tp" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ有効です！"));
                    return true;
                }
                if (args.length <= 1) {
                    p.sendMessage(Chat.f("&cマップ名を指定してください！"));
                    return true;
                }

                String mapName = String.join(" ", args).substring(args[0].length() + 1);

                List<GameMap> correctMapList = LeonGunWar.getPlugin().getMapsConfig()
                        .getAllGameMap().stream()
                        .filter(map -> map.getMapName().equalsIgnoreCase(mapName)
                                || map.getMapName().equalsIgnoreCase(mapName.replace(" ", "_")))
                        .collect(Collectors.toList());

                if (correctMapList.size() == 1) {
                    Location spawn = correctMapList.get(0).getSpawnPoint(BattleTeam.values()[0]);
                    p.teleport(spawn);
                    p.sendMessage(Chat.f("&e{0} &7にテレポートしました。", correctMapList.get(0).getMapName()));
                } else if (correctMapList.size() > 1) {
                    p.sendMessage(Chat.f("&cマッチしたマップが2つあります"));
                    correctMapList.forEach(map -> {
                        Location spawn = map.getSpawnPoint(BattleTeam.values()[0]);
                        JSONMessage msg = JSONMessage.create(Chat.f(
                                "&7 - &e{0}: &7{1}, {2}, {3} &7({4})",
                                map.getMapName(), spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getWorld().getName()
                        ));
                        msg.tooltip(Chat.f("&eクリックでテレポート"));
                        msg.runCommand(Chat.f("/essentials:tppos {0} {1} {2} {3} {4} {5}",
                                spawn.getX(), spawn.getY(), spawn.getZ(),
                                spawn.getYaw(), spawn.getPitch(), spawn.getWorld().getName()
                        ));
                        msg.send(p);
                    });
                } else {
                    p.sendMessage(Chat.f("&c指定したマップが見つかりませんでした。"));
                }
                return true;
            }

            // ==========================
            // reload / rl
            // ==========================
            case "reload", "rl" -> {
                try {
                    LeonGunWar.getPlugin().getMapsConfig().loadConfig();
                    LeonGunWar.getPlugin().getSpawnsConfig().loadConfig();
                    LeonGunWar.getPlugin().getKillStreaksConfig().loadConfig();
                    LeonGunWar.getPlugin().getAssistStreaksConfig().loadConfig();
                    LeonGunWar.getPlugin().getWeaponControlConfig().loadConfig();
                    LeonGunWar.getPlugin().getItemsConfig().loadConfig();
                } catch (IOException | InvalidConfigurationException exception) {
                    exception.printStackTrace();
                }
                sender.sendMessage(Chat.f("{0}&a設定とマップのリロードが完了しました。", LeonGunWar.GAME_PREFIX));
                return true;
            }

            // ==========================
            // showdata
            // ==========================
            case "showdata" -> {
                if (!LeonGunWar.getPlugin().getManager().isMatching()) {
                    sender.sendMessage(Chat.f("{0}&7現在試合をしていないためマッチデータの閲覧はできません。", LeonGunWar.GAME_PREFIX));
                    return true;
                }

                MatchManager manager = LeonGunWar.getPlugin().getManager();
                MatchMode mode = manager.getMatchMode();

                sender.sendMessage(Chat.f("{0}&cMatch Data: {1}\n", LeonGunWar.GAME_PREFIX, mode.getModeName()));

                for (BattleTeam team : manager.getTeamPlayers().keySet()) {
                    int playerCount = manager.getTeamPlayers().get(team).size();
                    int teampowerlevel = manager.getTeamPowerLevel(manager.getScoreboardTeam(team));
                    int teamacepowerlevel = manager.getTeamAcePowerLevel(manager.getScoreboardTeam(team));
                    int matchpoint = manager.getCurrentTeamPoint(team);

                    String leadername = Chat.f("&4NOT_LEADER_DEATH_MATCH");
                    if (manager.getMatchMode() == MatchMode.LEADER_DEATH_MATCH) {
                        Player leader = manager.getLDMLeader(team);
                        if (leader != null) {
                            leadername = leader.getDisplayName();
                        }
                    }

                    sender.sendMessage(Chat.f(
                            "{0} {1}&e データ\n" +
                                    "{0}&eチーム人数: §6{2}人\n" +
                                    "{0}&eチームパワーレベル: §6{3}\n" +
                                    "{0}&eチームエースパワーレベル: §6{4}\n" +
                                    "{0}&e現在のポイント: §6{5}\n" +
                                    "{0}&eチームリーダー: §6{6}\n",
                            LeonGunWar.GAME_PREFIX, team.getTeamName(),
                            playerCount, teampowerlevel, teamacepowerlevel, matchpoint, leadername
                    ));
                }
                sender.sendMessage(Chat.f("{0}&cMatch Data: {1}", LeonGunWar.GAME_PREFIX, mode.getModeName()));
                return true;
            }

            // ==========================
            // 未定義サブコマンド
            // ==========================
            default -> {
                // TODO helpメッセージ
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Args.complete(args, 0, "debug_start", "teleport", "tp", "reload", "rl");
        }
        if (args.length == 2 && Args.check(args, 0, "teleport", "tp")) {
            return Args.complete(args, 1,
                    LeonGunWar.getPlugin().getMapsConfig().getAllGameMap().stream()
                            .map(GameMap::getMapName)
                            .toArray(String[]::new));
        }
        return null;
    }
}

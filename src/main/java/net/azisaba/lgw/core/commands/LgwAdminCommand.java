package net.azisaba.lgw.core.commands;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.util.Args;
import net.azisaba.lgw.core.util.AdventureUtil;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.BroadcastUtils;
import net.azisaba.lgw.core.util.Chat;
import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.util.MatchMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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

public class LgwAdminCommand implements CommandExecutor, TabCompleter {

    // ミスって本家で実行してしまうとまずいので/lgw debug_startにロックをかけれるように
    private static final boolean ALLOW_DEBUG = true;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO helpメッセージ実装
        if (Args.isEmpty(args)) {
            return true;
        }

        if (Args.check(args, 0, "togglekilleffect")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Chat.f("{0}&cプレイヤー専用コマンドです。", LeonGunWar.GAME_PREFIX));
                return true;
            }
            if (!player.hasPermission("leongunwar.command.leongunwaradmin")) {
                player.sendMessage(Chat.f("{0}&c/lgwadmin の権限が必要です。", LeonGunWar.GAME_PREFIX));
                return true;
            }
            if (!LeonGunWar.getPlugin().getMainConfig().isLobby) {
                player.sendMessage(Chat.f("{0}&cロビー鯖で実行してください。", LeonGunWar.GAME_PREFIX));
                return true;
            }
            boolean enabled = LeonGunWar.getPlugin().getKillActionManager().toggleLobbyEffect(player);
            player.sendMessage(Chat.f("{0}&eロビーのキル演出試用を{1}&eにしました。",
                    LeonGunWar.GAME_PREFIX, enabled ? "&aON" : "&cOFF"));
            return true;
        }

        if (Args.check(args, 0, "pvparena")) {
            LeonGunWar.getPlugin().getPvPArenaManager().handleCommand(sender, args);
            return true;
        }

        if (Args.check(args, 0, "cancel_mapvote", "cancelmapvote")) {
            if (!LeonGunWar.getPlugin().getMapSelectCountdown().isRunning()) {
                sender.sendMessage(Chat.f("{0}&c現在マップ投票は行われていません。", LeonGunWar.GAME_PREFIX));
                return true;
            }
            LeonGunWar.getPlugin().getMapSelectCountdown().stopCountdown();
            BroadcastUtils.broadcast(Chat.f("{0}&eマップ投票をキャンセルしました。", LeonGunWar.GAME_PREFIX));
            return true;
        }

        // debug_startなら
        if (Args.check(args, 0, "debug_start")) {

            // allowDebugがfalseならreturn
            if (!ALLOW_DEBUG) {
                sender.sendMessage(Chat.f("&cこの引数は現在無効化されているため実行できません！"));
                return true;
            }

            // 試合中ならreturn
            if (LeonGunWar.getPlugin().getManager().isMatching()) {
                sender.sendMessage(Chat.f("{0}&c試合中は開始できません。", LeonGunWar.GAME_PREFIX));
                return true;
            }
            if (LeonGunWar.getPlugin().getMapsConfig().getAllGameMap().isEmpty()) {
                sender.sendMessage(Chat.f("{0}&c試合マップが設定されていません。", LeonGunWar.GAME_PREFIX));
                return true;
            }
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                sender.sendMessage(Chat.f("{0}&c参加できるプレイヤーがいません。", LeonGunWar.GAME_PREFIX));
                return true;
            }

            List<GameMap> availableMaps = LeonGunWar.getPlugin().getMapsConfig().getReadyMaps();
            if (availableMaps.isEmpty()) {
                sender.sendMessage(Chat.f("{0}&c読み込み済みワールドと両チームのスポーンが設定されたマップがありません。", LeonGunWar.GAME_PREFIX));
                return true;
            }

            MatchManager matchManager = LeonGunWar.getPlugin().getManager();
            LeonGunWar.getPlugin().getMapSelectCountdown().stopCountdown();
            matchManager.finalizeMatch();
            matchManager.setCurrentGameMap(availableMaps.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(availableMaps.size())));
            matchManager.getEntryPlayers().clear();
            Bukkit.getOnlinePlayers().forEach(matchManager::addEntryPlayer);
            matchManager.setMatchMode(MatchMode.TEAM_DEATH_MATCH);

            // カウントダウン終了
            LeonGunWar.getPlugin().getMatchStartCountdown().stopCountdown();

            // 試合開始
            matchManager.startMatch();
            return true;
        }

        // teleportかtpなら
        if (Args.check(args, 0, "teleport", "tp", "setspawn")) {

            // senderがプレイヤーではない場合はreturn
            if (!(sender instanceof Player p)) {
                sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ有効です！"));
                return true;
            }

            boolean settingSpawn = Args.check(args, 0, "setspawn");
            MapSpawnArguments.Selection selection;
            try {
                selection = MapSpawnArguments.resolve(args, LeonGunWar.getPlugin().getMapsConfig().getAllGameMap(), settingSpawn);
            } catch (IllegalArgumentException exception) {
                p.sendMessage(Chat.f("&c{0}\n&e/lgwadmin {1} <map> {2}", exception.getMessage(),
                        settingSpawn ? "setspawn" : "tp", settingSpawn ? "<red|blue>" : "[red|blue]"));
                return true;
            }
            List<GameMap> correctMapList = selection.maps();
            BattleTeam team = selection.team();

            if (settingSpawn && correctMapList.size() <= 1) {
                GameMap gameMap;
                Location spawn = p.getLocation();
                try {
                    if (correctMapList.isEmpty()) {
                        gameMap = LeonGunWar.getPlugin().getMapsConfig().createMapWithSpawn(selection.mapName(), team, spawn);
                    } else {
                        gameMap = correctMapList.get(0);
                        LeonGunWar.getPlugin().getMapsConfig().setSpawnPoint(gameMap, team, spawn);
                    }
                } catch (IOException | InvalidConfigurationException exception) {
                    LeonGunWar.getPlugin().getLogger().log(java.util.logging.Level.SEVERE,
                            "スポーン地点を maps.yml に保存できませんでした: " + selection.mapName(), exception);
                    p.sendMessage(Chat.f("&c保存に失敗しました。maps.yml の書き込み権限とサーバーログを確認してください。"));
                    return true;
                } catch (IllegalArgumentException exception) {
                    p.sendMessage(Chat.f("&c{0}", exception.getMessage()));
                    return true;
                }
                if (correctMapList.isEmpty()) {
                    p.sendMessage(Chat.f("&a新マップ {0} をワールド {1} に登録しました。", gameMap.getMapName(), spawn.getWorld().getName()));
                }
                p.sendMessage(Chat.f("&a{0} の {1}&aスポーンを保存しました。&7 X={2} Y={3} Z={4} / yaw={5} pitch={6}",
                        gameMap.getMapName(), team.getTeamName(), spawn.getX(), spawn.getY(), spawn.getZ(),
                        spawn.getYaw(), spawn.getPitch()));
                if (!gameMap.isReady()) {
                    p.sendMessage(Chat.f("&e赤・青のスポーンが両方そろうと試合候補に入ります。もう片方も /lgwadmin setspawn {0} <red|blue> で設定してください。",
                            gameMap.getMapName()));
                }
                return true;
            }

            // サイズが1ならテレポート
            if (correctMapList.size() == 1) {
                GameMap gameMap = correctMapList.get(0);
                Location spawn = getTeleportSpawn(p, gameMap, team);
                if (spawn == null) {
                    return true;
                }
                if (!p.teleport(spawn)) {
                    p.sendMessage(Chat.f("&cテレポートがキャンセルされました。"));
                    return true;
                }
                p.sendMessage(Chat.f("&e{0} の {1}&7スポーンへテレポートしました。&7 X={2} Y={3} Z={4}",
                        gameMap.getMapName(), team.getTeamName(), spawn.getX(), spawn.getY(), spawn.getZ()));

                // 1より多い場合
            } else if (correctMapList.size() > 1) {
                p.sendMessage(Chat.f("&c複数のマップが一致しました。maps.yml の正確なマップ名を指定してください。"));
                if (settingSpawn) {
                    return true;
                }

                // 各マップへのテレポートリンクを表示
                correctMapList.forEach(map -> {
                    Location spawn = getTeleportSpawn(p, map, team);
                    if (spawn == null) {
                        return;
                    }
                    Component msg = AdventureUtil.legacy(Chat.f("&7 - &e{0}: &7{1}, {2}, {3} &7({4})", map.getMapName(),
                                    spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getWorld().getName()))
                            .hoverEvent(HoverEvent.showText(AdventureUtil.legacy(Chat.f("&eクリックでテレポート"))))
                            .clickEvent(ClickEvent.runCommand(Chat.f("/essentials:tppos {0} {1} {2} {3} {4} {5}",
                                    spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getYaw(), spawn.getPitch(),
                                    spawn.getWorld().getName())));

                    // メッセージを送信
                    p.sendMessage(msg);
                });

                // 1より少ない場合 (0以下の場合)
            } else {
                p.sendMessage(Chat.f("&c指定したマップが見つかりませんでした。"));
            }

            return true;
        }

        // reloadなら
        if (Args.check(args, 0, "reload", "rl")) {

            try {
                // マップの再読み込み
                LeonGunWar.getPlugin().getMapsConfig().loadConfig();

                // Lobby Spawnの読み込み
                LeonGunWar.getPlugin().getSpawnsConfig().loadConfig();

                // 設定ファイルの読み込み
                LeonGunWar.getPlugin().getKillStreaksConfig().loadConfig();
                LeonGunWar.getPlugin().getAssistStreaksConfig().loadConfig();
                LeonGunWar.getPlugin().getWeaponControlConfig().loadConfig();
                LeonGunWar.getPlugin().getItemsConfig().loadConfig();
                LeonGunWar.getPlugin().getKillLogsConfig().loadConfig();
                LeonGunWar.getPlugin().getSyogoConfig().loadConfig();
                LeonGunWar.getPlugin().getKillActionCatalog().reload();
            } catch (IOException | InvalidConfigurationException exception) {
                exception.printStackTrace();
                sender.sendMessage(Chat.f("{0}&c設定の再読み込みに失敗しました。ログを確認してください。", LeonGunWar.GAME_PREFIX));
                return true;
            }

            sender.sendMessage(Chat.f("{0}&a設定とマップのリロードが完了しました。", LeonGunWar.GAME_PREFIX));
            return true;
        }

        // 1つ目の引数がshowdataの場合
        if (Args.check(args, 0, "showdata")) {

            // 試合中ではない場合return
            if (!LeonGunWar.getPlugin().getManager().isMatching()) {
                sender.sendMessage(Chat.f("{0}&7現在試合をしていないためマッチデータの閲覧はできません。", LeonGunWar.GAME_PREFIX));
                return true;
            }

            MatchManager manager = LeonGunWar.getPlugin().getManager();

            // マッチタイプを取得
            MatchMode mode = manager.getMatchMode();

            // マッチデータを表示 その1
            sender.sendMessage(Chat.f("{0}&cMatch Data: {1}\n", LeonGunWar.GAME_PREFIX, mode.getModeName()));

            // バトルチームのデータ取得
            for (BattleTeam team : manager.getTeamPlayers().keySet()) {

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

                if (manager.getMatchMode().isLeaderDeathMatch()) {

                    // リーダーを取得
                    Player leader = manager.getLDMLeader(team);

                    // リーダーが存在するなら
                    if (leader != null) {
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

    private Location getTeleportSpawn(CommandSender sender, GameMap gameMap, BattleTeam team) {
        Location spawn = gameMap.getSpawnPoint(team);
        if (spawn == null) {
            sender.sendMessage(Chat.f("&cマップ {0} の {1}&cスポーンが設定されていません。", gameMap.getMapName(), team.getTeamName()));
            return null;
        }
        if (spawn.getWorld() == null) {
            sender.sendMessage(Chat.f("&cマップ {0} のワールドが読み込まれていません。maps.ymlとワールド管理設定を確認してください。",
                    gameMap.getMapName()));
            return null;
        }
        return spawn;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Args.complete(args, 0, "debug_start", "cancel_mapvote", "cancelmapvote", "pvparena", "teleport", "tp", "setspawn", "reload", "rl", "showdata", "togglekilleffect");
        }
        if (Args.check(args, 0, "pvparena")) {
            return LeonGunWar.getPlugin().getPvPArenaManager().tabComplete(sender, args);
        }
        if (args.length >= 2 && Args.check(args, 0, "teleport", "tp", "setspawn")) {
            return MapSpawnArguments.complete(args, LeonGunWar.getPlugin().getMapsConfig().getAllGameMap(),
                    Args.check(args, 0, "setspawn"));
        }
        return null;
    }
}

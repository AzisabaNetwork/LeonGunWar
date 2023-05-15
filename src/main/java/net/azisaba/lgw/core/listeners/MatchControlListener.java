package net.azisaba.lgw.core.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.rayzr522.jsonmessage.JSONMessage;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.MatchTimeChangedEvent;
import net.azisaba.lgw.core.events.PlayerKickMatchEvent;
import net.azisaba.lgw.core.tasks.RemoveBossBarTask;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.KDPlayerData;
import net.azisaba.lgw.core.utils.BroadcastUtils;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.CustomItem;
import net.azisaba.lgw.core.utils.SecondOfDay;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MatchControlListener implements Listener {

    /**
     * 制限時間が0秒になったときにMatchFinishedEventを呼び出すリスナー
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void matchFinishDetector(MatchTimeChangedEvent e) {
        // 時間を取得して0じゃなかったらreturn
        if ( e.getTimeLeft() > 0 ) {
            return;
        }

        // 一番高いポイントを取得
        int maxPoint = Arrays.stream(BattleTeam.values())
                .map(LeonGunWar.getPlugin().getManager()::getCurrentTeamPoint)
                .max(Comparator.naturalOrder())
                .orElse(-1);

        // 一番高いポイントと同じポイントのチームをList形式で取得
        List<BattleTeam> winners = Arrays.stream(BattleTeam.values())
                .filter(team -> LeonGunWar.getPlugin().getManager().getCurrentTeamPoint(team) == maxPoint)
                .collect(Collectors.toList());

        // イベントを呼び出す
        MatchFinishedEvent event = new MatchFinishedEvent(LeonGunWar.getPlugin().getManager().getCurrentGameMap(),
                winners,
                LeonGunWar.getPlugin().getManager().getTeamPlayers());
        Bukkit.getPluginManager().callEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void setMatchingListener(MatchFinishedEvent e) {
        // 試合中ならfalseにする
        if ( LeonGunWar.getPlugin().getManager().isMatching() ) {
            LeonGunWar.getPlugin().getManager().setMatching(false);
        }
    }

    /**
     * 試合が終わった時の処理を担当するリスナー
     */
    @EventHandler
    public void onMatchFinished(MatchFinishedEvent e) {

        // 試合に参加した全プレイヤーを取得
        List<Player> allPlayers = e.getAllTeamPlayers();

        // MVPのプレイヤーを取得
        List<KDPlayerData> mvpPlayers = LeonGunWar.getPlugin().getManager().getKillDeathCounter().getMVPPlayer();
        // MVPプレイヤーのメッセージ
        List<String> resultMessages = new ArrayList<>(
                Collections.singletonList(Chat.f("&d=== Team Point Information ===")));

        // 各チームのポイントを表示
        for ( BattleTeam team : BattleTeam.values() ) {
            int point = LeonGunWar.getPlugin().getManager().getCurrentTeamPoint(team);
            resultMessages.add(Chat.f("{0} &c{1} Point(s)", team.getTeamName(), point));
        }

        // MVPを表示 (ない場合は何も表示しない)
        if (!mvpPlayers.isEmpty()) {
            for (KDPlayerData data : mvpPlayers) {
                resultMessages.add(Chat.f("&c[MVP] {0} {1} Kill(s), {2} Death(s), {3} Assist(s)",
                    data.getPlayerName(),
                    data.getKills(), data.getDeaths(), data.getAssists()));
            }
        }

        // 結果を全プレイヤーに表示
        for (String msg : resultMessages) {
            BroadcastUtils.broadcast(msg);
        }

        for (Player p : allPlayers) {

            if (p.isDead()) {
                p.spigot().respawn();
            }

            // スポーンにTP
            p.teleport(LeonGunWar.getPlugin().getSpawnsConfig().getLobby());

            // アーマー削除
            p.getInventory().setChestplate(null);

            // 各記録を取得
            int kills = LeonGunWar.getPlugin().getManager().getKillDeathCounter().getKills(p);
            int deaths = LeonGunWar.getPlugin().getManager().getKillDeathCounter().getDeaths(p);
            int assists = LeonGunWar.getPlugin().getManager().getKillDeathCounter().getAssists(p);

            // プレイヤーの戦績を表示
            p.sendMessage(Chat.f("&7[Your Score] {0} {1} Kill(s), {2} Death(s), {3} Assist(s)", p.getName(), kills,
                    deaths, assists));

            // 回復
            p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
            p.setFoodLevel(40);
        }

        // 勝ったチームがあれば勝者の証を付与
        if ( e.getWinners().size() >= 1 ) {

            // 各チームに勝者の証を付与
            e.getWinners().forEach(wonTeam -> {
                // チームメンバーを取得
                List<Player> winnerPlayers = e.getTeamPlayers(wonTeam);

                for ( Player p : winnerPlayers ) {
                    // 勝者の証を付与
                    p.getInventory().addItem(CustomItem.getWonItem());

                    // 勝利タイトルを表示
                    p.sendTitle(Chat.f("&6Victory!"), "", 0, 20 * 3, 10);

                    // 音を鳴らす
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }

                // 勝利メッセージを送信
                BroadcastUtils.broadcast(
                    Chat.f("{0}{1} &7が &6勝利 &7しました！", LeonGunWar.GAME_PREFIX,
                        wonTeam.getTeamName()));
            });
        }
    }

    /**
     * 最後に finalizeMatch メソッドを実行するためのリスナー ついでにQuickメッセージも表示
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void matchFinalizer(MatchFinishedEvent e) {

        LeonGunWar.getPlugin().getManager().finalizeMatch();

        // 全プレイヤーにQuickメッセージを送信
        LeonGunWar.getQuickBar().send(BroadcastUtils.getOnlinePlayers().toArray(new Player[0]));
    }

    /**
     * プレイヤーのスコアボードを更新するためのリスナー
     */
    @EventHandler
    public void scoreboardUpdater(MatchTimeChangedEvent e) {
        // スコアボードをアップデート
        LeonGunWar.getPlugin().getScoreboardDisplayer().updateScoreboard();
    }

    /**
     * プレイヤーのアクションバーを更新するリスナー
     */
    @EventHandler
    public void actionbarUpdater(MatchTimeChangedEvent e) {
        // 試合中のプレイヤーを取得
        List<Player> players = LeonGunWar.getPlugin().getManager().getAllTeamPlayers();

        // アクションバーをアップデート
        for ( Player p : players ) {
            // 表示するメッセージを取得
            String actionBar = LeonGunWar.getPlugin().getManager().getKillDeathCounter().getActionBar(p);

            // nullの場合はデフォルトの内容を表示する
            if ( actionBar == null ) {
                actionBar = LeonGunWar.getPlugin().getManager().getKillDeathCounter().getDefaultActionBar(p);
            }

            // アクションバーに表示
            JSONMessage.create(actionBar).actionbar(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKickedMatch(PlayerKickMatchEvent e) {
        MatchManager manager = LeonGunWar.getPlugin().getManager();

        // 現在のプレイヤーを取得
        Map<BattleTeam, List<Player>> playerMap = manager.getTeamPlayers();

        // もし0人のチームがある場合は試合を強制終了
        for ( List<Player> playerList : playerMap.values() ) {
            if ( playerList.size() <= 0 ) {

                // イベント作成
                MatchFinishedEvent event = new MatchFinishedEvent(manager.getCurrentGameMap(), new ArrayList<>(),
                        playerMap);
                // 呼び出し
                Bukkit.getPluginManager().callEvent(event);
                break;
            }
        }
    }

    @EventHandler
    public void remainTimeMessage(MatchTimeChangedEvent e) {
        int timeLeft = e.getTimeLeft();

        // 残り時間が指定された時間の場合チャット欄でお知らせ
        if ( Arrays.asList(60, 30, 10, 5, 4, 3, 2, 1).contains(timeLeft) ) {
            BroadcastUtils.broadcast(
                Chat.f("{0}&7残り &c{1}&7！", LeonGunWar.GAME_PREFIX, SecondOfDay.f(timeLeft)));
        }

        // 5秒以下なら音を鳴らす
        if ( Arrays.asList(5, 4, 3, 2, 1).contains(timeLeft) ) {
            BroadcastUtils.getOnlinePlayers()
                .forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HAT, 1, 1));
        }
    }

    @EventHandler
    public void remainTimeBossbar(MatchTimeChangedEvent e) {

        final BossBar progressBar;

        // progressBarがnullならバーを作成
        if ( LeonGunWar.getPlugin().getManager().getBossBar() == null ) {
            // 名前は後で設定するので空欄
            progressBar = LeonGunWar.getPlugin().getManager().createEmptyBossBar();
            // 設定
            LeonGunWar.getPlugin().getManager().setBossBar(progressBar);
        } else {
            progressBar = LeonGunWar.getPlugin().getManager().getBossBar();
        }

        // 名前を変更
        progressBar.setTitle(Chat.f("&7残り時間 &a» &f{0}", SecondOfDay.toString(e.getTimeLeft())));
        // 進行度を設定
        double left = e.getTimeLeft();
        double duration = LeonGunWar.getPlugin().getManager().getMatchMode().getDuration().getSeconds();
        progressBar.setProgress(left / duration);

        Bukkit.getOnlinePlayers().forEach(p -> {

            // プレイヤーにボスバーが表示されていなかったら表示
            if ( !progressBar.getPlayers().contains(p) ) {
                progressBar.addPlayer(p);
            }
        });
    }

    @EventHandler
    public void bossBarClearListener(MatchFinishedEvent e) {
        // 遅らせて削除
        new RemoveBossBarTask().runTaskLater(LeonGunWar.getPlugin(), 0);
    }
}

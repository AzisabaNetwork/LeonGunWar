package net.azisaba.lgw.core;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.azisaba.lgw.core.distributors.TeamDistributor;
import net.azisaba.lgw.core.battlesystem.BattleTeam;
import net.azisaba.lgw.core.api.util.Chat;
import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.battlesystem.MatchMode;
import net.azisaba.lgw.core.util.SecondOfDay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class ScoreboardDisplayer {
    private List<String> lastDisplayLines = new ArrayList<>();
    // Objectiveを作成したいスコアボード
    private Scoreboard scoreBoard;

    /**
     * プレイヤーに表示するスコアボードのタイトルを取得します
     *
     * @return スコアボードのタイトル
     */
    private String scoreBoardTitle() {
        return Chat.f("&6LeonGunWarII&a v{0}", LeonGunWar.getPlugin().getDescription().getVersion());
    }

    /**
     * スコアボードに表示したい文章をListで指定する (上から)
     */
    private List<String> boardLines() {
        // 試合中の場合
        if (LeonGunWar.getPlugin().getManager().isMatching()) {

            /*

              残り時間: ?秒

              各チーム: ? Point(s)

              現在のマップ: {マップ名} 現在のモード: {モード} アルゴリズム: {振り分け方式}

              azisaba.net で今すぐ遊べ！
             */

            // マップ名を取得
            String mapName = LeonGunWar.getPlugin().getManager().getCurrentGameMap().getMapName();

            // 残り時間
            int timeLeft = LeonGunWar.getPlugin().getManager().getTimeLeft().get();

            // 試合のモード
            MatchMode mode = LeonGunWar.getPlugin().getManager().getMatchMode();

            // 振り分け方式
            TeamDistributor distributor = LeonGunWar.getPlugin().getManager().getTeamDistributor();

            // 表示するメッセージリストを作成
            List<String> messageList = new ArrayList<>();
            messageList.add("");
            messageList.add(Chat.f("&b残り時間&a: &c{0}", SecondOfDay.f(timeLeft)));
            messageList.add("");

            for (BattleTeam team : BattleTeam.values()) {
                int point = LeonGunWar.getPlugin().getManager().getCurrentTeamPoint(team);
                messageList.add(Chat.f("{0}&a: &e{1} Point(s)", team.getTeamName(), point));
            }

            if (mode == MatchMode.LEADER_DEATH_MATCH_POINT) {
                for (BattleTeam team : BattleTeam.values()) {
                    messageList.add(Chat.f("{0}&6のリーダー&a: &e{1} ", team.getTeamName(), LeonGunWar.getPlugin().getManager().getLDMLeader(team).getName()));
                }
            }

            messageList.add("");
            messageList.add(Chat.f("&7現在のマップ&a: &c{0}", mapName));
            messageList.add(Chat.f("&7現在のモード&a: &c{0}", mode.getShortModeName()));
            messageList.add(Chat.f("&7アルゴリズム&a: &c{0}", distributor.getDistributorName()));
            messageList.add("");
            messageList.add(Chat.f("&7今すぐ &6{0} &7で遊べ！", "azisaba.net"));

            // return
            return messageList;
        }

        // Map選択中の場合
        if (LeonGunWar.getPlugin().getMapSelectCountdown().isRunning()) {
            /*

              マップ投票中
              残り時間: n秒

              1. Map1: n票
              2. Map2: n票
              3. Map3: n票
              4. Map4: n票

              /mapvote [番号] で投票！

              azisaba.net で今すぐ遊べ！
             */
            // カウントダウンを取得
            MapSelectCountdown countdown = LeonGunWar.getPlugin().getMapSelectCountdown();
            // マップListを取得
            List<GameMap> maps = countdown.getMaps();
            // 残り時間を取得
            int timeLeft = countdown.getTimeLeft();


            // 表示するメッセージリストを作成
            List<String> messageList = new ArrayList<>();
            messageList.add("");
            messageList.add(Chat.f("&7マップ投票中"));
            messageList.add(Chat.f("&b残り時間&a: &c{0}", SecondOfDay.f(timeLeft)));
            messageList.add("");

            for (int i = 0, size = maps.size(); i < size; i++) {
                messageList.add(Chat.f("&7{0}. &e{1}&7: &c{2}票", i + 1, maps.get(i).getMapName(), countdown.getVote(i)));
            }

            messageList.add("");
            messageList.add(Chat.f("&7/mapvote [番号] で投票！"));
            messageList.add("");
            messageList.add(Chat.f("&7今すぐ &6{0} &7で遊べ！", "azisaba.net"));

            // return
            return messageList;
        }

        List<String> fallback = new ArrayList<>();
        fallback.add(ChatColor.GRAY + "スコアボードを初期化中...");
        // 試合をしていない場合
        return fallback;
    }

    /**
     * プレイヤーにスコアボードを表示します
     *
     */

    /**
     * public void updateScoreboard() {
     * Preconditions.checkNotNull(scoreBoard, "A scoreboard is not initialized yet.");
     * <p>
     * if ( Bukkit.getOnlinePlayers().size() <= 0 ) {
     * return;
     * }
     * <p>
     * // Objectiveを取得
     * Objective obj = scoreBoard.getObjective("side");
     * <p>
     * // Objectiveが存在しなかった場合は作成
     * if ( obj == null ) {
     * obj = scoreBoard.registerNewObjective("side", "dummy");
     * }
     * <p>
     * // Slotを設定
     * obj.setDisplaySlot(DisplaySlot.SIDEBAR);
     * obj.setDisplayName(scoreBoardTitle());
     * <p>
     * // 行を取得
     * List<String> lines = boardLines();
     * // nullが返ってきた場合は非表示にしてreturn
     * if ( lines == null ) {
     * scoreBoard.clearSlot(DisplaySlot.SIDEBAR);
     * return;
     * }
     * // リスト反転
     * Collections.reverse(lines);
     * <p>
     * // 現在指定されているEntryを全て解除
     * clearEntries();
     * <p>
     * int currentValue = 0;
     * for ( String msg : lines ) {
     * <p>
     * // 行が0の場合は空白にする
     * if ( msg == null ) {
     * msg = "";
     * }
     * <p>
     * // すでに値が設定されている場合は最後に空白を足していく
     * while ( obj.getScore(msg).isScoreSet() ) {
     * msg = msg + " ";
     * }
     * <p>
     * // 値を設定
     * obj.getScore(msg).setScore(currentValue);
     * currentValue++;
     * }
     * <p>
     * // スコアボードを設定する
     * Bukkit.getOnlinePlayers().forEach(p -> {
     * if ( p.getScoreboard() != scoreBoard ) {
     * p.setScoreboard(scoreBoard);
     * }
     * });
     * }
     **/

    public void tickScoreboard() {
        updateScoreboardLines(boardLines());
    }

    private void updateScoreboardLines(List<String> lines) {
        if (scoreBoard == null) return;

        if (lines == null || lines.isEmpty()) {
            lines = List.of(ChatColor.GRAY + "スコアボード待機中...");
        }

        // Objective の準備
        Objective obj = scoreBoard.getObjective("side");
        if (obj == null) {
            obj = scoreBoard.registerNewObjective("side", "dummy", scoreBoardTitleComponent());
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        obj.displayName(scoreBoardTitleComponent());

        // スコア表示用にリスト反転
        List<String> reversed = new ArrayList<>(lines);
        Collections.reverse(reversed);

        // 前回のスコア位置マップ
        Map<String, Integer> oldScoreMap = new HashMap<>();
        for (int i = 0; i < lastDisplayLines.size(); i++) {
            oldScoreMap.put(lastDisplayLines.get(i), i);
        }

        Set<String> usedLines = new HashSet<>();
        List<String> newDisplayLines = new ArrayList<>();

        for (int i = 0; i < reversed.size(); i++) {
            String line = reversed.get(i);
            if (line == null) line = "";

            String displayLine = makeUniqueLine(line, usedLines);
            usedLines.add(displayLine);
            newDisplayLines.add(displayLine);

            Integer oldScore = oldScoreMap.get(displayLine);
            if (oldScore != null && oldScore == i) {
                continue; // 同じスコアなら変更不要
            }

            obj.getScore(displayLine).setScore(i);
        }

        // 差分削除：前回あって今回にない行のみ削除
        for (String oldLine : lastDisplayLines) {
            if (!newDisplayLines.contains(oldLine)) {
                scoreBoard.resetScores(oldLine);
            }
        }

        lastDisplayLines = newDisplayLines;

        // 全プレイヤーにスコアボードを適用
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (p.getScoreboard() != scoreBoard) {
                p.setScoreboard(scoreBoard);
            }
        });
    }

    private Component scoreBoardTitleComponent() {
        return LegacyComponentSerializer.legacySection().deserialize(scoreBoardTitle());
    }

    private String makeUniqueLine(String base, Set<String> used) {
        String result = base;
        int index = 0;
        while (used.contains(result)) {
            result = base + ChatColor.values()[index % ChatColor.values().length];
            index++;
            if (result.length() > 40) {
                result = result.substring(0, 40);
            }
        }
        return result;
    }

    /**
     * 現在設定されているEntryを全てリセットする
     */
    private void clearEntries() {
        Preconditions.checkNotNull(scoreBoard, "A scoreboard is not initialized yet.");

        scoreBoard.getEntries().forEach(scoreBoard::resetScores);
    }

    public void clearSideBar() {
        Preconditions.checkNotNull(scoreBoard, "A scoreboard is not initialized yet.");

        // boardがnullでなければSIDEBARを削除
        scoreBoard.clearSlot(DisplaySlot.SIDEBAR);
    }

    public void setScoreBoard(Scoreboard scoreboard) {

        this.scoreBoard = scoreboard;

    }
}

package net.azisaba.lgw.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.base.Preconditions;

import net.azisaba.lgw.core.distributors.TeamDistributor;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.KillDeathCounter;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.SecondOfDay;

import lombok.Data;

@Data
public class ScoreboardDisplayer {

    /**
     * プレイヤーに表示するスコアボードのタイトルを取得します
     *
     * @return スコアボードのタイトル
     */
    private String scoreBoardTitle() {

        return Chat.f("&6LeonGunWar &c&lBETA");
        //return Chat.f("&6LeonGunWar&a v{0}", LeonGunWar.getPlugin().getDescription().getVersion());
    }

    /**
     * スコアボードに表示したい文章をListで指定する (上から)
     */
    public List<String> matchBoardLines(Player player) {
        // 試合中の場合
        if ( LeonGunWar.getPlugin().getManager().isMatching() ) {

            /*

              21/07/22  lgwsv1

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
            messageList.add(ChatColor.GRAY + new SimpleDateFormat("yy/MM/dd").format(new Date(System.currentTimeMillis()))
                    + "  " + ChatColor.DARK_GRAY + LeonGunWar.getPlugin().getConfig().get("server-name","lgwsv"));
            messageList.add("");

            if(timeLeft > 0){
                messageList.add(Chat.f("残り時間: &a{0}", SecondOfDay.f(timeLeft)));
                messageList.add("");
            }else {
                messageList.add("残り時間: ");
                messageList.add(ChatColor.GREEN + "Game ended!");
            }

            messageList.add("");

            for ( BattleTeam team : BattleTeam.values() ) {
                int point = LeonGunWar.getPlugin().getManager().getCurrentTeamPoint(team);
                messageList.add(Chat.f("{0}&a: &e{1} Points", team.getTeamName(), point));
            }

            messageList.add("");
            messageList.add(ChatColor.GREEN + "キル: " + LeonGunWar.getPlugin().getManager().getKillDeathCounter().getKills(player));

            /*
            messageList.add("");
            messageList.add(Chat.f("マップ: &a{0}", mapName));
            messageList.add(Chat.f("モード: &a{0}", mode.getShortModeName()));
            messageList.add(Chat.f("振り分け: &a{0}", distributor.getDistributorName()));

             */
            messageList.add("");
            messageList.add(ChatColor.YELLOW + "www.azisaba.net");

            // return
            return messageList;
        }

        // 試合をしていない場合
        return null;
    }

    public List<String> queueBordLines(int timeLeft){

        if(!LeonGunWar.getPlugin().getMatchQueueManager().hasQueue()){
            return null;
        }

        MatchQueueManager mqm = LeonGunWar.getPlugin().getMatchQueueManager();

        List<String> messages = new ArrayList<>();

        messages.add(ChatColor.GRAY + new SimpleDateFormat("yy/MM/dd").format(new Date(System.currentTimeMillis()))
                + "  " + ChatColor.DARK_GRAY + LeonGunWar.getPlugin().getConfig().get("server-name","lgwsv"));
        messages.add("");
        messages.add("マップ: " + ChatColor.GREEN + mqm.getMapName());
        messages.add("プレイヤー数: " + ChatColor.GREEN + mqm.getQueueSize());
        messages.add("");

        if(LeonGunWar.getPlugin().getCountdown().isRunning()){
            messages.add("開始まであと: " + ChatColor.GREEN + timeLeft + "秒");
        }else {
            messages.add("開始まであと: " + ChatColor.GREEN + "Waiting...");
        }

        messages.add("");
        messages.add("モード: " + mqm.getMatchMode().getShortModeName());
        messages.add("");
        messages.add(ChatColor.YELLOW + "www.azisaba.net");

        return messages;

    }

    // Objectiveを作成したいスコアボード
    private Scoreboard scoreBoard;

    /**
     * プレイヤーにスコアボードを表示します
     *
     */
    public void updateScoreboard(Player player,List<String> bordLines) {
        Preconditions.checkNotNull(scoreBoard, "A scoreboard is not initialized yet.");

        if ( Bukkit.getOnlinePlayers().size() <= 0 ) {
            return;
        }

        // Objectiveを取得
        Objective obj = scoreBoard.getObjective("side");

        // Objectiveが存在しなかった場合は作成
        if ( obj == null ) {
            obj = scoreBoard.registerNewObjective("side", "dummy");
        }

        // Slotを設定
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(scoreBoardTitle());

        // nullが返ってきた場合は非表示にしてreturn
        if ( bordLines == null ) {
            //scoreBoard.clearSlot(DisplaySlot.SIDEBAR);
            return;
        }
        // リスト反転
        Collections.reverse(bordLines);

        // 現在指定されているEntryを全て解除
        clearEntries();

        int currentValue = 0;
        for ( String msg : bordLines ) {

            // 行が0の場合は空白にする
            if ( msg == null ) {
                msg = "";
            }

            // すでに値が設定されている場合は最後に空白を足していく
            while ( obj.getScore(msg).isScoreSet() ) {
                msg = msg + " ";
            }

            // 値を設定
            obj.getScore(msg).setScore(currentValue);
            currentValue++;
        }

        // スコアボードを設定する
        //Bukkit.getOnlinePlayers()

        if ( player.getScoreboard() != scoreBoard ) {
            player.setScoreboard(scoreBoard);
        }

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

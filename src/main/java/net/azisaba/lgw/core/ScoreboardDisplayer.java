package net.azisaba.lgw.core;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import net.azisaba.lgw.core.distributors.TeamDistributor;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.SecondOfDay;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

@Data
public class ScoreboardDisplayer {

  /**
   * プレイヤーに表示するスコアボードのタイトルを取得します
   *
   * @return スコアボードのタイトル
   */
  private String scoreBoardTitle() {
    return Chat.f("&6LeonGunWar&a v{0}", LeonGunWar.getPlugin().getDescription().getVersion());
  }

  /** スコアボードに表示したい文章をListで指定する (上から) */
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
        messageList.add(
            Chat.f(
                "&7{0}. &e{1}&7: &c{2}票", i + 1, maps.get(i).getMapName(), countdown.getVote(i)));
      }

      messageList.add("");
      messageList.add(Chat.f("&7/mapvote [番号] で投票！"));
      messageList.add("");
      messageList.add(Chat.f("&7今すぐ &6{0} &7で遊べ！", "azisaba.net"));

      // return
      return messageList;
    }

    // 試合をしていない場合
    return null;
  }

  // Objectiveを作成したいスコアボード
  private Scoreboard scoreBoard;

  /** プレイヤーにスコアボードを表示します */
  public void updateScoreboard() {
    Preconditions.checkNotNull(scoreBoard, "A scoreboard is not initialized yet.");

    if (Bukkit.getOnlinePlayers().size() <= 0) {
      return;
    }

    // Objectiveを取得
    Objective obj = scoreBoard.getObjective("side");

    // Objectiveが存在しなかった場合は作成
    if (obj == null) {
      obj = scoreBoard.registerNewObjective("side", "dummy");
    }

    // Slotを設定
    obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    obj.setDisplayName(scoreBoardTitle());

    // 行を取得
    List<String> lines = boardLines();
    // nullが返ってきた場合は非表示にしてreturn
    if (lines == null) {
      scoreBoard.clearSlot(DisplaySlot.SIDEBAR);
      return;
    }
    // リスト反転
    Collections.reverse(lines);

    // 現在指定されているEntryを全て解除
    clearEntries();

    int currentValue = 0;
    for (String msg : lines) {

      // 行が0の場合は空白にする
      if (msg == null) {
        msg = "";
      }

      // すでに値が設定されている場合は最後に空白を足していく
      while (obj.getScore(msg).isScoreSet()) {
        msg = msg + " ";
      }

      // 値を設定
      obj.getScore(msg).setScore(currentValue);
      currentValue++;
    }

    // スコアボードを設定する
    Bukkit.getOnlinePlayers()
        .forEach(
            p -> {
              if (p.getScoreboard() != scoreBoard) {
                p.setScoreboard(scoreBoard);
              }
            });
  }

  /** 現在設定されているEntryを全てリセットする */
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

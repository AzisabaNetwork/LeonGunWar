package net.azisaba.lgw.core.distributors;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;

import jp.azisaba.lgw.kdstatus.KDManager;
import jp.azisaba.lgw.kdstatus.KDUserData;

/**
 *
 * KD依存のチーム振り分けクラス
 *
 * @author Mr_IK
 *
 */
public class KDTeamDistributor implements TeamDistributor {

    /**
     * プレイヤーがAceか否かを判定します。
     *
     * Ace条件: 月のキル数が3000以上 or KD1.2以上
     *
     * 例外: 累計キル数が1000未満の人は除外
     */
    public static boolean isACE(Player p) {
        // プレイヤーの戦績取得
        KDUserData pd = KDManager.getPlayerData(p, true);
        int kills = pd.getKills();
        int deaths = pd.getDeaths();

        // デス数が0以下の場合は1に変更
        if ( deaths <= 0 ) {
            deaths = 1;
        }

        // KD計算
        double kd = (double) kills / (double) deaths;

        // 累計キル数が1000未満の人は除外
        if ( pd.getKills() < 1000 ) {
            return false;
        }

        // 月のキル数が3000以上 or KD1.2以上ならtrue それ以外ならfalse
        return kd >= 1.2 || pd.getMonthlyKills() >= 3000;
    }

    /**
     * プレイヤーのパワーレベルを取得するメソッド
     *
     * 計算式: KDx1000 + 一か月のキル数÷10
     *
     * 例外: 累計キル数が100未満の人は上記の「KDx1000」 を800に固定する
     */
    public static int getPlayerPowerLevel(Player p) {
        int pl = 0;
        // プレイヤーの戦績取得
        KDUserData pd = KDManager.getPlayerData(p, true);
        int kills = pd.getKills();
        int deaths = pd.getDeaths();

        // デス数が0以下の場合は1に変更
        if ( deaths <= 0 ) {
            deaths = 1;
        }

        // KD計算
        double kd = (double) kills / (double) deaths;
        if ( pd.getKills() < 100 ) {
            kd = 0.8;
        }
        // 代入
        pl = (int) (kd * 1000);
        // 今月のキル数を代入
        pl += pd.getMonthlyKills() / 10;
        return pl;
    }

    /**
     * 戦績を参考にし、パワーバランスを同じにする振り分けを行います
     */
    @Override
    public void distributePlayers(List<Player> plist, List<Team> teams) {
        // plistを一応シャッフル
        Collections.shuffle(plist);

        // 分ける
        plist.forEach(player -> distributePlayer(player, teams));
    }

    /**
     * 戦績を参考にし、パワーバランスを同じにする振り分けを行います
     */
    @Override
    public void distributePlayer(Player player, List<Team> teams) {

        MatchManager manager = LeonGunWar.getPlugin().getManager();

        // もしAceなら
        if ( isACE(player) ) {
            // チームエースパワーレベルの少ない方にAceプレイヤーを追加
            // (同じ場合はチームパワーレベルが少ないチームの方、それも同じ場合はエントリーが少ないチームの方、さらにそれも同じ場合はポイントが少ない方、それでも同じなら最初の要素)
            teams.stream()
                    .sorted(Comparator.comparing(manager::getTeamAcePowerLevel).thenComparing(manager::getTeamPowerLevel).thenComparing(Team::getSize).thenComparing(manager::getCurrentTeamPoint))
                    .findFirst()
                    .ifPresent(lowTeam -> lowTeam.addEntry(player.getName()));
            return;
        }

        // チームパワーレベルの少ない方にプレイヤーを追加 (同じ場合はエントリーが少ないチームの方、それも同じ場合はポイントが少ない方、それでも同じなら最初の要素)
        teams.stream()
                .sorted(Comparator.comparing(manager::getTeamPowerLevel).thenComparing(Team::getSize).thenComparing(manager::getCurrentTeamPoint))
                .findFirst()
                .ifPresent(lowTeam -> lowTeam.addEntry(player.getName()));
    }

    @Override
    public String getDistributorName() {
        return "K/D振り分け";
    }
}

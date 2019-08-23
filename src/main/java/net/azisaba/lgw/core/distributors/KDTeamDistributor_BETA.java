package net.azisaba.lgw.core.distributors;

import jp.azisaba.lgw.kdstatus.KDManager;
import jp.azisaba.lgw.kdstatus.KDUserData;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * KD依存のチーム振り分けクラス
 *
 * @author Mr_IK
 *
 */
public class KDTeamDistributor_BETA implements TeamDistributor {

    /**
     * プレイヤーのパワーレベルを取得するメソッド
     *
     * 計算式: KDx1000 + 一か月のキル数
     *
     * 例外: 累計キル数が100未満の人は上記の「KDx1000」 を800に固定する
     */
    public static int getPlayerPowerLevel(Player p){
        int pl = 0;
        //プレイヤーの戦績取得
        KDUserData pd = KDManager.getPlayerData(p,true);
        //KD計算
        double kd = (double) pd.getKills() / (double) pd.getDeaths();
        if(pd.getKills()<100){
            kd = 0.8;
        }
        //代入
        pl = (int)(kd*1000);
        //今月のキル数を代入
        pl = pl + pd.getMonthlyKills();
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

        // エントリーが少ないチームにプレイヤーを追加 (同じ場合はチームレベルの少ない方、それも同じ場合はポイントが少ない方、それでも同じなら最初の要素)
        teams.stream()
                .sorted(Comparator.comparing(Team::getSize).thenComparing(manager::getTeamPowerLevel).thenComparing(manager::getCurrentTeamPoint))
                .findFirst()
                .ifPresent(lowTeam -> lowTeam.addEntry(player.getName()));
    }
}

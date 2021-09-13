package net.azisaba.lgw.core.distributors;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;

import lombok.RequiredArgsConstructor;

import jp.azisaba.lgw.kdstatus.KDStatusReloaded;
import jp.azisaba.lgw.kdstatus.sql.KDUserData;
import jp.azisaba.lgw.kdstatus.utils.TimeUnit;

/**
 *
 * KD依存のチーム振り分けクラス
 *
 * @author Mr_IK
 *
 */
@RequiredArgsConstructor
public class KDTeamDistributor implements TeamDistributor {

    private static KDStatusReloaded kdsPlugin;

    /**
     * プレイヤーがAceか否かを判定します。
     *
     * Ace条件: 月のキル数が3000以上 or KD1.2以上
     *
     * 例外: 累計キル数が1000未満の人は除外
     */
    public static boolean isACE(Player p) {
        // KDStatusReloadedがない場合は取得
        if ( kdsPlugin == null || !kdsPlugin.isEnabled() ) {
            // 取得し、失敗したらエラー
            if ( !getKDSPlugin() ) {
                throw new IllegalStateException("Failed to get plugin \"KDStatusReloaded\"");
            }
        }

        // プレイヤーの戦績取得
        KDUserData pd = kdsPlugin.getKdDataContainer().getPlayerData(p, true);
        int kills = pd.getKills(TimeUnit.LIFETIME);
        int deaths = pd.getDeaths();

        // デス数が0以下の場合は1に変更
        if ( deaths <= 0 ) {
            deaths = 1;
        }

        // KD計算
        double kd = (double) kills / (double) deaths;

        // 累計キル数が1000未満の人は除外
        if ( pd.getKills(TimeUnit.LIFETIME) < 1000 ) {
            return false;
        }

        // 月のキル数が3000以上 or KD1.2以上ならtrue それ以外ならfalse
        return kd >= 1.2 || pd.getKills(TimeUnit.MONTHLY) >= 3000;
    }

    /**
     * プレイヤーのパワーレベルを取得するメソッド
     *
     * 計算式: KDx1000 + 一か月のキル数÷10
     *
     * 例外: 累計キル数が100未満の人は上記の「KDx1000」 を800に固定する
     */
    public static int getPlayerPowerLevel(Player p) {
        // KDStatusReloadedがない場合は取得
        if ( kdsPlugin == null || !kdsPlugin.isEnabled() ) {
            // 取得し、失敗したらエラー
            if ( !getKDSPlugin() ) {
                throw new IllegalStateException("Failed to get plugin \"KDStatusReloaded\"");
            }
        }

        int pl;
        // プレイヤーの戦績取得
        KDUserData pd = kdsPlugin.getKdDataContainer().getPlayerData(p, true);
        int kills = pd.getKills(TimeUnit.LIFETIME);
        int deaths = pd.getDeaths();

        // デス数が0以下の場合は1に変更
        if ( deaths <= 0 ) {
            deaths = 1;
        }

        // KD計算
        double kd = (double) kills / (double) deaths;
        if ( pd.getKills(TimeUnit.LIFETIME) < 100 ) {
            kd = 0.8;
        }
        // 代入
        pl = (int) (kd * 1000);
        // 今月のキル数を代入
        pl += pd.getKills(TimeUnit.MONTHLY) / 10;
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
                    .min(Comparator.comparing(manager::getTeamAcePowerLevel).thenComparing(manager::getTeamPowerLevel).thenComparing(Team::getSize).thenComparing(manager::getCurrentTeamPoint))
                    .ifPresent(lowTeam -> lowTeam.addEntry(player.getName()));
            return;
        }

        // チームパワーレベルの少ない方にプレイヤーを追加 (同じ場合はエントリーが少ないチームの方、それも同じ場合はポイントが少ない方、それでも同じなら最初の要素)
        teams.stream()
                .min(Comparator.comparing(manager::getTeamPowerLevel).thenComparing(Team::getSize).thenComparing(manager::getCurrentTeamPoint))
                .ifPresent(lowTeam -> lowTeam.addEntry(player.getName()));
    }

    @Override
    public String getDistributorName() {
        return "K/D";
    }

    private static boolean getKDSPlugin() {
        // Pluginを取得
        Plugin pl = Bukkit.getPluginManager().getPlugin("KDStatusReloaded");
        // nullならreturn false
        if ( pl == null ) {
            return false;
        }
        // 代入
        kdsPlugin = (KDStatusReloaded) pl;
        // 無効化されていたらreturn false
        return kdsPlugin.isEnabled();
    }
}

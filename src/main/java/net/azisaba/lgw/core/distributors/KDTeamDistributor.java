package net.azisaba.lgw.core.distributors;

import lombok.RequiredArgsConstructor;
import net.azisaba.kdstatusreloaded.KDStatusReloaded;
import net.azisaba.kdstatusreloaded.playerkd.model.KDUserData;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * KD依存のチーム振り分けクラス
 *
 * @author Mr_IK
 */
@RequiredArgsConstructor
public class KDTeamDistributor implements TeamDistributor {

    private static KDStatusReloaded kdsPlugin;

    /**
     * プレイヤーがAceか否かを判定します。
     * <p>
     * Ace条件: 月のキル数が3000以上 or KD1.2以上
     * <p>
     * 例外: 累計キル数が1000未満の人は除外
     */
    public static boolean isACE(Player p) {
        // KDStatusReloadedがない場合は取得
        if (kdsPlugin == null || !kdsPlugin.isEnabled()) {
            // 取得し、失敗したらエラー
            if (!getKDSPlugin()) {
                throw new IllegalStateException("Failed to get plugin \"KDStatusReloaded\"");
            }
        }

        // プレイヤーの戦績取得
        KDUserData pd = kdsPlugin.getPlayerKd().getPlayerData(p.getUniqueId());
        int kills = pd.totalKills;
        int deaths = pd.deaths;

        // デス数が0以下の場合は1に変更
        if (deaths <= 0) {
            deaths = 1;
        }

        // KD計算
        double kd = (double) kills / (double) deaths;

        // 累計キル数が1000未満の人は除外
        if (pd.totalKills < 1000) {
            return false;
        }

        // 月のキル数が3000以上 or KD1.2以上ならtrue それ以外ならfalse
        return kd >= 1.2 || pd.monthlyKills >= 3000;
    }

    /**
     * プレイヤーのパワーレベルを取得するメソッド
     * <p>
     * 計算式: KDx1000 + 一か月のキル数÷10
     * <p>
     * 例外: 累計キル数が100未満の人は上記の「KDx1000」 を800に固定する
     */
    public static int getPlayerPowerLevel(Player p) {
        // KDStatusReloadedがない場合は取得
        if (kdsPlugin == null || !kdsPlugin.isEnabled()) {
            // 取得し、失敗したらエラー
            if (!getKDSPlugin()) {
                throw new IllegalStateException("Failed to get plugin \"KDStatusReloaded\"");
            }
        }

        int pl;
        // プレイヤーの戦績取得
        KDUserData pd = kdsPlugin.getPlayerKd().getPlayerData(p.getUniqueId());
        int kills = pd.totalKills;
        int deaths = pd.deaths;

        // デス数が0以下の場合は1に変更
        if (deaths <= 0) {
            deaths = 1;
        }

        // KD計算
        double kd = (double) kills / (double) deaths;
        if (pd.totalKills < 100) {
            kd = 0.8;
        }
        // 代入
        pl = (int) (kd * 1000);
        // 今月のキル数を代入
        pl += pd.totalKills / 10;
        return pl;
    }

    private static boolean getKDSPlugin() {
        // Pluginを取得
        Plugin pl = Bukkit.getPluginManager().getPlugin("KDStatusReloaded");
        // nullならreturn false
        if (pl == null) {
            return false;
        }
        // 代入
        kdsPlugin = KDStatusReloaded.getPlugin();
        // 無効化されていたらreturn false
        return kdsPlugin.isEnabled();
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
        if (isACE(player)) {
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
        return "K/D振り分け";
    }
}

package net.azisaba.lgw.core.distributors;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;

/**
 *
 * デフォルトのチーム振り分けクラス
 *
 * @author siloneco
 *
 */
public class DefaultTeamDistributor implements TeamDistributor {

    /**
     * 戦績に関係なく、ただ人数比を同じにする振り分けを行います
     */
    @Override
    public void distributePlayers(List<Player> plist, List<Team> teams) {
        // plistをシャッフル
        Collections.shuffle(plist);

        // 均等に分ける
        plist.forEach(player -> distributePlayer(player, teams));
    }

    /**
     * 戦績に関係なく、ただ人数比を同じにする振り分けを行います
     */
    @Override
    public void distributePlayer(Player player, List<Team> teams) {

        MatchManager manager = LeonGunWar.getPlugin().getManager();

        // エントリーが少ないチームにプレイヤーを追加 (同じ場合はポイントが少ない方、それでも同じなら最初の要素)
        teams.stream()
                .sorted(Comparator.comparing(Team::getSize).thenComparing(manager::getCurrentTeamPoint))
                .findFirst()
                .ifPresent(lowTeam -> lowTeam.addEntry(player.getName()));
    }
}

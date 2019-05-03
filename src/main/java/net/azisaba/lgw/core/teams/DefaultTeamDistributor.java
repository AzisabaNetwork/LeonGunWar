package net.azisaba.lgw.core.teams;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

/**
 *
 * デフォルトのチーム振り分けクラス
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

		// エントリーが少ないチームにプレイヤーを追加 (同じ場合は最初の要素)
		teams.stream()
				.sorted(Comparator.comparing(Team::getSize).reversed())
				.findFirst()
				.ifPresent(lowTeam -> lowTeam.addEntry(player.getName()));
	}
}

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
		plist.forEach(player -> {

			// distributePlayer(player, teams)にて振り分ける
			distributePlayer(player, teams);

		});
	}

	/**
	 * 戦績に関係なく、ただ人数比を同じにする振り分けを行います
	 */
	@Override
	public void distributePlayer(Player player, List<Team> teams) {

		// エントリーが少ないチームを取得 (同じ場合はランダム)
		Team lowTeam = teams.stream()
				.sorted(Comparator.comparing(team -> team.getEntries().size()))
				.sorted(Collections.reverseOrder())
				.findFirst()
				.orElse(null);

		// プレイヤーを追加
		if (lowTeam != null) {
			lowTeam.addEntry(player.getName());
		}
	}
}

package net.azisaba.lgw.core.teams;

import java.util.Collections;
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
	public void distributePlayers(List<Player> plist, Team red, Team blue) {
		// plistをシャッフル
		Collections.shuffle(plist);

		// 均等に分ける
		plist.forEach(player -> {

			// distributePlayer(player, red, blue)にて振り分ける
			distributePlayer(player, red, blue);

		});
	}

	/**
	 * 戦績に関係なく、ただ人数比を同じにする振り分けを行います
	 */
	@Override
	public void distributePlayer(Player player, Team red, Team blue) {

		// エントリーが少ないチームを取得 (同じ場合はred)
		Team lowTeam = red;
		if (blue.getEntries().size() < red.getEntries().size()) {
			lowTeam = blue;
		}

		// プレイヤーを追加
		lowTeam.addEntry(player.getName());
	}
}

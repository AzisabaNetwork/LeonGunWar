package net.azisaba.lgw.core.teams;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

/**
 * プレイヤーを2チームに分けるクラスのためのインターフェース
 * @author siloneco
 *
 */
public interface TeamDistributor {

	/**
	 * プレイヤーを2チームに均等に振り分けます
	 * 基本は同じ人数になるように振り分けしますが、今後KDによって振り分けるときのためにインターフェースで実装します
	 *
	 * @param plist 振り分けたいプレイヤーのリスト
	 * @param red 赤のスコアボードのチーム
	 * @param blue 青のスコアボードのチーム
	 */
	abstract public void distributePlayer(List<Player> plist, Team red, Team blue);

}

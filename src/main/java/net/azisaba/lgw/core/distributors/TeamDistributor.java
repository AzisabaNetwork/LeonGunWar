package net.azisaba.lgw.core.distributors;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

/**
 * プレイヤーをチームに振り分けるクラスのためのインターフェース
 *
 * @author siloneco
 *
 */
public interface TeamDistributor {

    /**
     * プレイヤーをチームに均等に振り分けます 基本は同じ人数になるように振り分けしますが、今後KDによって振り分けるときのためにインターフェースで実装します
     *
     * @param plist 振り分けたいプレイヤーのリスト
     * @param teams チームのリスト
     */
    void distributePlayers(List<Player> plist, List<Team> teams);

    /**
     * 指定されたプレイヤーをどちらかのチームに振り分けます
     *
     * @param p     振り分けたいプレイヤー
     * @param teams チームのリスト
     */
    void distributePlayer(Player p, List<Team> teams);
}

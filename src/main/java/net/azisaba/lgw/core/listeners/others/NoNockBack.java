package net.azisaba.lgw.core.listeners.others;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerVelocityEvent;

/**
 * ノックバックを無効化するためのクラス
 * @author siloneco
 *
 */
public class NoNockBack implements Listener {

	/**
	 * プレイヤーがノックバックしたときに呼び出されるイベント
	 * @param e ノックバックしたイベント
	 */
	@EventHandler
	public final void onVelocity(PlayerVelocityEvent e) {
		e.setCancelled(true);
	}
}

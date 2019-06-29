package net.azisaba.lgw.core.listeners.others;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;

/**
 * CrackShotのアイテムの連打を無効化するリスナー
 * 
 * @author siloneco
 *
 */
public class CrackShotLimitListener implements Listener {

    // コンバットナイフ用のクールタイムMap
    private final Map<Player, Long> knifeMap = new HashMap<>();
    // コンバットナイフのクールダウンの秒数
    private final double knifeCooldown;

    // ライトニングストライク用のクールタイムMap
    private final Map<Player, Long> storoboMap = new HashMap<>();
    // ライトニングストライクのクールダウンの秒数
    private final double storoboCooldown;

    // インスタンス作成時にナイフとライトニングストライクのクールダウン時間を取得する
    public CrackShotLimitListener() {
        CSDirector cs = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");

        knifeCooldown = cs.getDouble("Combat_Knife.Shooting.Delay_Between_Shots") / 20;
        storoboCooldown = cs.getDouble("STOROBO2.Airstrikes.Multiple_Strikes.Delay_Between_Strikes") / 20;
    }

    /**
     * ナイフによってEntityが傷つけられたときに、クールタイム中ならキャンセルするListener
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onWeaponDamage(WeaponDamageEntityEvent e) {
        // ナイフではない場合はreturn
        if ( !e.getWeaponTitle().equals("Combat_Knife") ) {
            return;
        }

        Player p = e.getPlayer();
        // クールダウン中ならキャンセルしてreturn
        if ( knifeMap.getOrDefault(p, 0L) + 1000 * knifeCooldown > System.currentTimeMillis() ) {
            e.setCancelled(true);
            return;
        }

        // 最終使用時刻を設定
        knifeMap.put(p, System.currentTimeMillis());
    }

    /**
     * ライトニングストライクが撃たれたときに、クールタイム中ならキャンセルするListener
     * 
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onShoot(WeaponPreShootEvent e) {
        // ライトニングストライクではない場合はreturn
        if ( !e.getWeaponTitle().equals("STOROBO2") ) {
            return;
        }

        Player p = e.getPlayer();
        // クールタイム中ならキャンセル
        if ( storoboMap.getOrDefault(p, 0L) + 1000 * storoboCooldown > System.currentTimeMillis() ) {
            e.setCancelled(true);
            return;
        }

        // 最終使用時刻を設定
        storoboMap.put(p, System.currentTimeMillis());
    }

    /**
     * プレイヤーが退出したときにデータが永遠と残っていると無駄なので削除するListener
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if ( knifeMap.containsKey(p) ) {
            knifeMap.remove(p);
        }
        if ( storoboMap.containsKey(p) ) {
            storoboMap.remove(p);
        }
    }
}

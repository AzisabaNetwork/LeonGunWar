package net.azisaba.lgw.core.listeners.modes;

import lombok.Getter;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.util.MatchMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.HashMap;
import java.util.Map;

public class AssassinationMatchListener implements Listener {
    @Getter
    Map<Player,Double> AttackDamage = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWitherTargetEntity (EntityTargetEvent e){
        if(e.getEntityType() == EntityType.WITHER){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onWitherAttackDetector (EntityDamageByEntityEvent e){

        MatchManager manager = LeonGunWar.getPlugin().getManager();
        // ASNではなければreturn
        if ( manager.getMatchMode() != MatchMode.ASSASSINATION_MATCH) {
            return;
        }

        // 攻撃されたエンティティを取得
        Entity damagedEntity = e.getEntity();
        //攻撃されたEntityがWitherではないならReturn
        if (!(damagedEntity instanceof Wither)){
            return;
        }

        // 攻撃したエンティティを取得
        Entity damager = e.getDamager();
        //攻撃したEntityがPlayerではない場合はreturn
        Player attacker = null;
        if(damager instanceof Player){
            attacker = (Player) damager;
        }else{
            return;
        }

        //AttackDamageに累計ダメージを代入
        double alldamage;
        if(AttackDamage.containsKey(attacker)){
            alldamage = AttackDamage.get(attacker);
            alldamage += e.getDamage();
        }else{
            alldamage = e.getDamage();
        }
        this.AttackDamage.put(attacker,alldamage);

    }

    @EventHandler
    public void onWitherDeathDetector (EntityDeathEvent e) {
        MatchManager manager = LeonGunWar.getPlugin().getManager();
        // ASNではなければreturn
        if ( manager.getMatchMode() != MatchMode.ASSASSINATION_MATCH) {
            return;
        }

        //死んだEntityがWitherではない場合return
        if(!(e.getEntity() instanceof Wither)){
            return;
        }


    }
}

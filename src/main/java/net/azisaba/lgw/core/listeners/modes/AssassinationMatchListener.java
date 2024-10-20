package net.azisaba.lgw.core.listeners.modes;

import lombok.Getter;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.tasks.AssasinationWitherDeathCountdown;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.BroadcastUtils;
import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssassinationMatchListener implements Listener {
    @Getter
    Map<Player,Double> AttackDamage = new HashMap<>();

    private final BukkitRunnable redTask = new AssasinationWitherDeathCountdown(BattleTeam.BLUE);
    private final BukkitRunnable blueTask = new AssasinationWitherDeathCountdown(BattleTeam.RED);

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
        Bukkit.getLogger().info("うごごごｇ");
        MatchManager manager = LeonGunWar.getPlugin().getManager();
        // ASNではなければreturn
        if ( manager.getMatchMode() != MatchMode.ASSASSINATION_MATCH) {
            return;
        }

        //死んだEntityがWitherではない場合return
        if(!(e.getEntity() instanceof Wither)){
            return;
        }
        String customName = e.getEntity().getCustomName();

        if (customName != null) {
            if (customName.contains(BattleTeam.RED.getEngTeamName())) {
                if (!redTask.isCancelled()) {
                    redTask.cancel(); // 既存のタスクを停止
                }
                // Team REDのウィザーが死亡
                redTask.runTask(LeonGunWar.getPlugin());
                BroadcastUtils.broadcast(BattleTeam.RED.getTeamName() + "の目標が破壊されました 60秒以内に5キルストリークして復活させない場合敗北します");
            } else if (customName.contains(BattleTeam.BLUE.getEngTeamName())) {
                if (!blueTask.isCancelled()) {
                    blueTask.cancel(); // 既存のタスクを停止
                }
                // Team BLURのウィザーが死亡
                blueTask.runTask(LeonGunWar.getPlugin());
                BroadcastUtils.broadcast(BattleTeam.BLUE.getTeamName() + "の目標が破壊されました 60秒以内に5キルストリークして復活させない場合敗北します");
            }
        }


    }

    @EventHandler
    public void onMatchFinish(MatchFinishedEvent e){
        if (!blueTask.isCancelled()) {
            blueTask.cancel(); // 青チームのタスクをキャンセル
        }
        if (!redTask.isCancelled()) {
            redTask.cancel(); // 赤チームのタスクをキャンセル
        }
        List<Wither> summonedWithers = LeonGunWar.getPlugin().getManager().getSummonedBOSS();
        for (Wither wither : summonedWithers) {
            if (wither.isValid()) {
                wither.remove(); // エンティティを削除
            }
        }
        summonedWithers.clear(); // リストをクリア
    }
}

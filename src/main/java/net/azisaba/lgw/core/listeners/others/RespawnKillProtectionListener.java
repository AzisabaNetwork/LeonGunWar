package net.azisaba.lgw.core.listeners.others;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.events.MatchStartedEvent;
import net.azisaba.lgw.core.events.PlayerRejoinMatchEvent;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;

public class RespawnKillProtectionListener implements Listener {

    private final RateLimiter protectedThrottle = RateLimiter.create(1);
    private final RateLimiter victimProtectedThrottle = RateLimiter.create(1);

    private final long duration = 5;

    private final Map<Player, Instant> remainTimes = new HashMap<>();
    private final Map<Player, BukkitTask> taskMap = new HashMap<>();
    private final Map<Player, BossBar> bossBars = new HashMap<>();

    private final List<Player> invincibleQueue = new ArrayList<>();

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        // 試合が行われていなければreturn
        if (!LeonGunWar.getPlugin().getManager().isMatching()) {
            return;
        }
        // ダメージを受けたEntityがプレイヤーでなければreturn
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) e.getEntity();

        // リスポーンから5秒以内ならキャンセル
        if (LeonGunWar.getPlugin().getManager().getRespawnProtection().isProtected(victim)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        // 試合が行われていなければreturn
        if (!LeonGunWar.getPlugin().getManager().isMatching()) {
            return;
        }
        // ダメージを受けたEntityがプレイヤーでなければreturn
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) e.getEntity();

        // リスポーンから5秒以内ならキャンセル
        if (LeonGunWar.getPlugin().getManager().getRespawnProtection().isProtected(victim)) {
            e.setCancelled(true);
            LeonGunWar.getPlugin().getManager().getRespawnProtection().sendProtected(victim);

            Player attacker = null;
            // 攻撃したEntityがプレイヤーならメッセージ送信対象に指定
            if (e.getDamager() instanceof Player) {
                attacker = (Player) e.getDamager();
            } else if (e.getDamager() instanceof Projectile) {
                // 攻撃したEntityが投げ物なら、投げたEntityを取得
                ProjectileSource shooter = ((Projectile) e.getDamager()).getShooter();

                // shooterがプレイヤーならメッセージ送信対象に指定
                if (shooter instanceof Player) {
                    attacker = (Player) shooter;
                }
            }

            // attackerがnullではない場合、メッセージを送信
            if ( attacker != null ) {
                LeonGunWar.getPlugin().getManager().getRespawnProtection()
                    .sendVictimProtected(attacker, victim);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        // 試合が行われていなければreturn
        if (!LeonGunWar.getPlugin().getManager().isMatching()) {
            return;
        }
        Player p = e.getPlayer();

        LeonGunWar.getPlugin().getManager().getRespawnProtection().respawned(p);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        // 試合が行われていなければreturn
        if (!LeonGunWar.getPlugin().getManager().isMatching()) {
            return;
        }

        LeonGunWar.getPlugin().getManager().getRespawnProtection().detectedAction(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        // 試合が行われていなければreturn
        if (!LeonGunWar.getPlugin().getManager().isMatching()) {
            return;
        }

        LeonGunWar.getPlugin().getManager().getRespawnProtection().detectedAction(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // 試合が行われていなければreturn
        if (!LeonGunWar.getPlugin().getManager().isMatching()) {
            return;
        }

        LeonGunWar.getPlugin().getManager().getRespawnProtection().terminate(e.getPlayer());
    }

    // 試合開始時にカウントダウンを開始
    @EventHandler
    public void onMatchStarted(MatchStartedEvent e) {
        e.getAllTeamPlayers()
            .forEach(p -> LeonGunWar.getPlugin().getManager().getRespawnProtection().respawned(p));
    }

    // 途中参加時にカウントダウンを開始
    @EventHandler
    public void onPlayerRejoinMatch(PlayerRejoinMatchEvent e) {
        Optional.of(e.getPlayer())
            .ifPresent(
                p -> LeonGunWar.getPlugin().getManager().getRespawnProtection().respawned(p));
    }
}

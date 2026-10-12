package net.azisaba.lgw.core.killaction;

import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/** Plays selected effects on the main server thread. */
public final class KillActionEffects {
    private final LeonGunWar plugin;
    private final KillActionCatalog catalog;

    public KillActionEffects(LeonGunWar plugin, KillActionCatalog catalog) {
        this.plugin = plugin;
        this.catalog = catalog;
    }

    public void playSounds(Player killer, Location death, KillActionCatalog.Entry entry, Player preview) {
        Location origin = death.clone();
        for (KillActionCatalog.SoundCue cue : entry.sounds()) {
            Runnable play = () -> {
                if (!plugin.isEnabled() || !killer.isOnline()) return;
                if (preview != null) {
                    if (preview.isOnline()) preview.playSound(preview.getLocation(), cue.sound(), cue.volume(), cue.pitch());
                    return;
                }
                playKillSound(killer, origin, cue.sound(), cue.volume(), cue.pitch());
            };
            if (cue.tick() == 0) play.run();
            else Bukkit.getScheduler().runTaskLater(plugin, play, cue.tick());
        }
    }

    public void playKillSound(Player shooter, Location victimLocation, Sound sound, float volume, float pitch) {
        double radiusSquared = (double) catalog.soundRadius() * catalog.soundRadius();
        for (Player viewer : victimLocation.getWorld().getPlayers()) {
            if (viewer == shooter) continue;
            if (viewer.getLocation().distanceSquared(victimLocation) <= radiusSquared) {
                viewer.playSound(victimLocation, sound, volume, pitch);
            }
        }
        if (shooter.isOnline()) {
            shooter.playSound(shooter.getLocation(), sound, volume, pitch);
        }
    }

    public void playParticles(Player killer, Location death, KillActionCatalog.Entry entry, Player preview) {
        Location victimOrigin = death.clone().add(0, 1, 0);
        int finalTick = 0;
        for (KillActionCatalog.ParticleEffect effect : entry.effects()) {
            if (effect.shape().equals("points")) {
                for (KillActionCatalog.Frame frame : effect.frames()) finalTick = Math.max(finalTick, frame.tick());
            } else finalTick = Math.max(finalTick, effect.startTick() + effect.durationTick() - 1);
        }
        final int lastTick = Math.min(finalTick, catalog.maxAnimationTicks());
        new BukkitRunnable() {
            int tick;
            int emitted;

            @Override
            public void run() {
                if (!plugin.isEnabled() || !killer.isOnline() || tick > lastTick
                        || (preview != null && !preview.isOnline())) { cancel(); return; }
                for (KillActionCatalog.ParticleEffect effect : entry.effects()) {
                    List<KillActionCatalog.Point> points;
                    if (effect.shape().equals("points")) {
                        points = effect.frames().stream().filter(frame -> frame.tick() == tick)
                                .flatMap(frame -> frame.points().stream()).toList();
                    } else if (tick >= effect.startTick() && tick < effect.startTick() + effect.durationTick()
                            && (tick - effect.startTick()) % effect.intervalTick() == 0) {
                        points = ParticlePattern.points(effect, tick);
                    } else continue;
                    Location origin = preview != null ? preview.getLocation().add(0, 1, 0)
                            : effect.killerOrigin() ? killer.getLocation().add(0, 1, 0) : victimOrigin;
                    double radiusSquared = (double) catalog.particleRadius() * catalog.particleRadius();
                    List<Player> viewers = preview != null ? List.of(preview) : origin.getWorld().getPlayers().stream()
                            .filter(viewer -> viewer.getLocation().distanceSquared(origin) <= radiusSquared).toList();
                    for (KillActionCatalog.Point point : points) {
                        if (emitted >= catalog.maxParticlesPerKill()) { cancel(); return; }
                        Location location = origin.clone().add(point.x(), point.y(), point.z());
                        for (Player viewer : viewers) send(viewer, effect, location);
                        emitted++;
                    }
                }
                tick++;
                if (tick > lastTick) cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void send(Player viewer, KillActionCatalog.ParticleEffect effect, Location location) {
        if (effect.data() == null) viewer.spawnParticle(effect.particle(), location, 1, 0, 0, 0, effect.speed());
        else viewer.spawnParticle(effect.particle(), location, 1, 0, 0, 0, effect.speed(), effect.data());
    }
}

package net.azisaba.lgw.core.tasks;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.Chat;
import net.azisaba.lgw.core.util.LgwLog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public final class AfkKickMonitoringTask extends BukkitRunnable {
    private static final long AFK_TIMEOUT_MS = 60_000; // ★コメントとズレないよう定数化（例:60秒）
    private final LeonGunWar plugin;
    private final ConcurrentHashMap<UUID, Long> lastMoved; // ★非同期判定と相性の良いConcurrent*
    private final Logger logger = LgwLog.getLogger(getClass());

    public AfkKickMonitoringTask(LeonGunWar plugin, ConcurrentHashMap<UUID, Long> lastMoved) {
        this.plugin = plugin;
        this.lastMoved = lastMoved;
    }

    @Override
    public void run() {
        // 1) 同期で“必要最小限の情報だけ”をスナップショット
        List<Candidate> snapshot = null;
        try {
            snapshot = Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                var mgr = LeonGunWar.getPlugin().getManager();
                List<Candidate> list = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    boolean matching = mgr.isPlayerMatching(p);
                    boolean entrying = mgr.isEntryPlayer(p);
                    // “試合/エントリに関係ないプレイヤー”は最初から除外しておく
                    if (!matching && !entrying) continue;
                    if (!matching) continue;
                    list.add(new Candidate(p.getUniqueId(), matching, entrying));
                }
                return list;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        long now = System.currentTimeMillis();
        List<UUID> toKick = new ArrayList<>();

        // 2) 非同期側でAFK判定（lastMoved との単純比較だけ）
        for (Candidate c : snapshot) {
            long last = lastMoved.getOrDefault(c.id, 0L);
            if (last + AFK_TIMEOUT_MS > now) continue; // まだタイムアウトしていない
            // 例：権限免除を入れるなら、ここでは判定せず“同期適用時”に `hasPermission` を見る
            toKick.add(c.id);
        }

        if (toKick.isEmpty()) return;

        // 3) 実処理（キック/メッセージ/プラグインメッセージ送信）は同期で一括適用
        Bukkit.getScheduler().runTask(plugin, () -> {
            var mgr = LeonGunWar.getPlugin().getManager();
            for (UUID id : toKick) {
                Player p = Bukkit.getPlayer(id);
                if (p == null || !p.isOnline()) continue;

                // 免除権限があるならここで早期continue
                // if (p.hasPermission("leongunwar.afkkick.exempt")) continue;

                // 試合から退出 & エントリー解除（同期API）
                mgr.removeEntryPlayer(p);
                mgr.kickPlayer(p);

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF("lgw2");
                p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());

                p.sendMessage(Chat.f("{0}&7放置と判定されたため試合から退出しました", LeonGunWar.GAME_PREFIX));
                logger.info(Chat.f("{0} を試合から退出させました", p.getName()));
            }
        });
    }

    private record Candidate(UUID id, boolean matching, boolean entrying) {
    }
}

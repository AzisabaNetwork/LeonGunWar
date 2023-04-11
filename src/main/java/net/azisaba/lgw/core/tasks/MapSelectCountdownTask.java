package net.azisaba.lgw.core.tasks;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.BroadcastUtils;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.SecondOfDay;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MapSelectCountdownTask extends BukkitRunnable {

    @Getter
    private final List<GameMap> maps;
    @Getter
    private final MatchMode mode;

    private final HashMap<Integer, AtomicInteger> votes = new HashMap<>();

    @Getter
    private final AtomicInteger timeLeft = new AtomicInteger(20);

    public MapSelectCountdownTask(Set<GameMap> maps, MatchMode mode) {
        this.maps = new ArrayList<>(maps);
        this.mode = mode;
    }

    @Override
    public void run() {
        // 0の場合カウントダウン終了処理
        if ( timeLeft.get() <= 0 ) {
            // カウントダウンを停止
            LeonGunWar.getPlugin().getMapSelectCountdown().stopCountdown();

            //票数に応じてランダムにマップを決定
            List<GameMap> random = new ArrayList<>();

            for (Integer i : votes.keySet()) {
                for (int count = 0; count < getVoteFor(i); count++) {
                    random.add(maps.get(i));
                }
            }

            if(random.isEmpty()){
                random.addAll(maps);
            }

            Collections.shuffle(random);

            GameMap m = random.get(0);

            LeonGunWar.getPlugin().getManager().setCurrentGameMap(m);
            BroadcastUtils.broadcast(
                Chat.f("{0}&7Mapが &e{1} &7に決定！", LeonGunWar.GAME_PREFIX, m.getMapName()));

            /*
            // 一番票数が多かったものに決定
            maps.stream()
                    .max(Comparator.comparingInt(o -> votes.getOrDefault(maps.indexOf(o), new AtomicInteger()).get()))
                    .ifPresent((m) -> {
                        LeonGunWar.getPlugin().getManager().setCurrentGameMap(m);
                        BroadcastUtils.broadcast(Chat.f("{0}&7Mapが &e{1} &7に決定！", LeonGunWar.GAME_PREFIX, m.getMapName()));
                    });

             */

            // 試合開始のカウントダウンのトリガー
            LeonGunWar.getPlugin().getManager().setMatchMode(mode);

            // 5秒後にスコアボードを消す
            Bukkit.getScheduler().runTaskLater(LeonGunWar.getPlugin(), () -> {
                LeonGunWar.getPlugin().getScoreboardDisplayer().clearSideBar();
            }, 20L * 5);

            // 表示内容を取得
            String distributorName = LeonGunWar.getPlugin().getManager().getTeamDistributor()
                .getDistributorName();
            String mapName = Optional.ofNullable(
                    LeonGunWar.getPlugin().getManager().getCurrentGameMap())
                .map(GameMap::getMapName).orElse("ランダム");

            // メッセージを表示
            BroadcastUtils.broadcast(
                Chat.f("{0}&7{1}", LeonGunWar.GAME_PREFIX, Strings.repeat("=", 40)));
            BroadcastUtils.broadcast(
                Chat.f("{0}&7モード   {1}", LeonGunWar.GAME_PREFIX, mode.getModeName()));
            BroadcastUtils.broadcast(
                Chat.f("{0}&7振り分け  {1}", LeonGunWar.GAME_PREFIX, distributorName));
            BroadcastUtils.broadcast(Chat.f("{0}&7マップ  {1}", LeonGunWar.GAME_PREFIX, mapName));
            BroadcastUtils.broadcast(Chat.f("{0}&7まもなく試合を開始します", LeonGunWar.GAME_PREFIX));
            BroadcastUtils.broadcast(
                Chat.f("{0}&7{1}", LeonGunWar.GAME_PREFIX, Strings.repeat("=", 40)));

            // 音を鳴らす
            Bukkit.getOnlinePlayers().forEach(
                player -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1));

            // 全プレイヤーにQuickメッセージを送信
            LeonGunWar.getQuickBar().send(Bukkit.getOnlinePlayers().toArray(new Player[0]));
            return;
        }

        // chatがtrueの場合表示
        if ( timeLeft.get() <= 5 ) {
            String msg = Chat.f("{0}&7Map決定まで残り &c{1}&7", LeonGunWar.GAME_PREFIX, SecondOfDay.f(timeLeft.get()));
            BroadcastUtils.broadcast(msg);
        }

        // 1秒減らす
        timeLeft.decrementAndGet();
        // スコアボードを更新
        LeonGunWar.getPlugin().getScoreboardDisplayer().updateScoreboard();
    }

    public int getVoteFor(int index) {
        return votes.getOrDefault(index, new AtomicInteger(0)).get();
    }

    public void incrementVoteFor(int index) {
        if ( votes.containsKey(index) ) {
            votes.get(index).incrementAndGet();
        } else {
            votes.put(index, new AtomicInteger(1));
        }
    }

    public void decrementVoteFor(int index) {
        if ( votes.containsKey(index) ) {
            votes.get(index).decrementAndGet();
        }
    }
}

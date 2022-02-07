package net.azisaba.lgw.core;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.entity.Player;

import net.azisaba.lgw.core.tasks.MapSelectCountdownTask;
import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.util.MatchMode;

public class MapSelectCountdown {

    private final AtomicReference<MapSelectCountdownTask> task = new AtomicReference<>();
    private final ConcurrentHashMap<UUID, Integer> votedIndexMap = new ConcurrentHashMap<>();

    /**
     * カウントダウンが行われていない場合、カウントダウンをスタートします
     *
     * @param maps ランダム抽選するためのマップのSet
     * @param mode 次試合の試合モード
     */
    public void startCountdown(Set<GameMap> maps, MatchMode mode) {
        // すでにタイマースタートしている場合はreturn
        // Runnable取得してスタート
        task.compareAndSet(null, new MapSelectCountdownTask(maps, mode));
        Optional.ofNullable(task.get()).ifPresent((task) -> task.runTaskTimer(LeonGunWar.getPlugin(), 0, 20));

    }

    /**
     * 指定したインデックスのマップの投票数を取得します
     * @param index 取得したいマップのindex
     * @return 取得したいマップの投票数を返す。投票が進行中ではない場合は -1
     */
    public int getVote(int index) {
        if ( !isRunning() ) {
            return -1;
        }

        return Optional.ofNullable(task.get())
                .map(task -> task.getVoteFor(index))
                .orElse(-1);
    }

    /**
     * 指定したインデックスのマップに投票します
     * @param player 投票するプレイヤー
     * @param index 投票するマップのインデックス
     */
    public void vote(Player player, int index) {
        // すでに投票していた場合にキャンセルする
        cancelVote(player);

        // 投票情報を保存
        votedIndexMap.put(player.getUniqueId(), index);
        Optional.ofNullable(task.get()).ifPresent((task) -> task.incrementVoteFor(index));
    }

    /**
     * 指定したインデックスのマップに投票します
     * @param player 投票をキャンセルするプレイヤー
     */
    public void cancelVote(Player player) {
        if ( !votedIndexMap.containsKey(player.getUniqueId()) ) {
            return;
        }
        int index = votedIndexMap.remove(player.getUniqueId());
        Optional.ofNullable(task.get()).ifPresent((task) -> task.decrementVoteFor(index));
    }

    /**
     * 現在進行中のタスクがあれば、マップのリストを返します
     * @return カウントダウンが進行中の場合抽選中のマップList、なければ空のリスト
     */
    public List<GameMap> getMaps() {
        return Optional.ofNullable(task.get())
                .map(MapSelectCountdownTask::getMaps)
                .orElse(Collections.emptyList());
    }

    /**
     * カウントダウンが実行されている場合、残り時間を取得します
     * @return カウントダウンが実行されている場合は残り時間を返す。実行されていない場合は -1 を返す
     */
    public int getTimeLeft() {
        if ( !isRunning() ) {
            return -1;
        }
        return Optional.ofNullable(task.get())
                .map(task -> task.getTimeLeft().get())
                .orElse(-1);
    }

    /**
     * 投票情報をすべてリセットします
     */
    public void resetAllVotes() {
        votedIndexMap.clear();
    }

    /**
     * カウントダウンが実行されていた場合、カウントダウンを停止します
     */
    public void stopCountdown() {
        Optional.ofNullable(task.getAndSet(null)).ifPresent(MapSelectCountdownTask::cancel);
    }

    /**
     * 現在カウントダウンが行われているかどうかを返します
     *
     * @return カウントダウン中かどうか
     */
    public boolean isRunning() {
        return task.get() != null;
    }
}

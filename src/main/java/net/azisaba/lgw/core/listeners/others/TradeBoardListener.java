package net.azisaba.lgw.core.listeners.others;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.SignData;
import net.azisaba.lgw.core.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * 掲示板を管理するListener
 *
 * <p>1 看板が設置されたときに登録 2 プレイヤーによって破壊されたときにキャンセル 3 右クリックで作者表示
 *
 * <p>などの機能
 *
 * @author siloneco
 */
public class TradeBoardListener implements Listener {

  // 1週間をミリ秒で取得
  private final long expireMilliSeconds = ChronoUnit.WEEKS.getDuration().toMillis();

  private final List<String> denySigns = Arrays.asList("[entry]", "[leave]", "[rejoin]", "[mode]");

  /** 看板を設置したときに内容を読み取り、登録やキャンセルをするListener */
  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerPlaceSignEvent(SignChangeEvent e) {
    Player p = e.getPlayer();
    Block b = e.getBlock();

    // プレイヤーがクリエイティブなら登録せずにreturn
    if (p.getGameMode() == GameMode.CREATIVE) {
      return;
    }
    // 取得したブロックが看板ではない場合はreturn
    if (b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN) {
      return;
    }

    // 武器交換掲示板のエリアではない場合はreturn
    if (!inTradeBoardRegion(b.getLocation())) {
      return;
    }

    // 空白の看板なら破壊
    if (isEmpty(e.getLines())) {
      b.breakNaturally();
      p.sendMessage(Chat.f("&c空白の看板なため破壊しました。"));
      return;
    }

    // 機能付き看板なら破壊
    if (e.getLine(0) != null && denySigns.contains(Chat.r(e.getLine(0).toLowerCase().trim()))) {
      b.breakNaturally();
      p.sendMessage(Chat.f("&c無効な内容の看板なため破壊しました。"));
      return;
    }

    // 登録処理

    String playerName = p.getName();
    UUID uuid = p.getUniqueId();
    long expire = System.currentTimeMillis() + expireMilliSeconds;

    // 登録
    boolean success =
        LeonGunWar.getPlugin()
            .getTradeBoardManager()
            .addSignData(b.getLocation(), playerName, uuid, expire);

    // 成功か失敗かで分岐
    if (success) {
      p.sendMessage(Chat.f("&a看板を正常に登録しました！"));
    } else {
      // 失敗したら破壊してメッセージを表示
      b.breakNaturally();
      p.sendMessage(Chat.f("&c看板の登録に失敗しました。申し訳ありませんが別の場所を利用してください。"));
    }
  }

  /** 掲示板のエリアにブロックが置かれたときに、それが看板ではない場合はキャンセルする */
  @EventHandler(priority = EventPriority.HIGH)
  public void onBlockPlace(BlockPlaceEvent e) {
    Player p = e.getPlayer();
    Block b = e.getBlock();

    // クリエイティブモードなら無条件で許可
    if (p.getGameMode() == GameMode.CREATIVE) {
      return;
    }

    // 掲示板エリアではない場合はreturn
    if (!inTradeBoardRegion(b.getLocation())) {
      return;
    }

    // 看板ではない場合はキャンセル
    if (b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN) {
      e.setCancelled(true);
    }
  }

  /** 看板を破壊したときに、自分の看板以外の看板であったらキャンセルし、自分の看板ならスルーするListener */
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBreakSign(BlockBreakEvent e) {
    Player p = e.getPlayer();
    Block b = e.getBlock();

    // 掲示板エリアではない場合はreturn
    if (!inTradeBoardRegion(b.getLocation())) {
      return;
    }

    // クリエイティブなら無条件で許可
    if (p.getGameMode() == GameMode.CREATIVE) {
      LeonGunWar.getPlugin().getTradeBoardManager().removeSignData(b.getLocation());
      return;
    }

    // 掲示板のデータを取得
    SignData data = LeonGunWar.getPlugin().getTradeBoardManager().getSignData(b.getLocation());
    // データがない場合は運営が設置した看板と判定しキャンセル
    if (data == null) {
      e.setCancelled(true);
      p.sendMessage(Chat.f("&c自分の設置した看板のみ破壊することができます！"));
      return;
    }

    // 破壊したプレイヤーと設置したプレイヤーとが同じならば破壊を許可
    if (data.getAuthor().equals(p.getUniqueId())) {
      LeonGunWar.getPlugin().getTradeBoardManager().removeSignData(b.getLocation());
      return;
    }

    // 他のプレイヤーの看板であるためキャンセルし、メッセージを表示
    e.setCancelled(true);
    p.sendMessage(Chat.f("&cこの看板は&e{0}&cによって作成されたものです！", data.getPlayerName()));
  }

  private final Map<Player, SignData> lastClicked = new HashMap<>();
  private final Map<Player, Long> lastClickedMilli = new HashMap<>();

  /** 右クリックで看板の作者を表示するListener */
  @EventHandler
  public void onClickSign(PlayerInteractEvent e) {

    // ブロックを右クリックしていなければreturn
    if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    Player p = e.getPlayer();
    Block b = e.getClickedBlock();

    // 看板のデータを取得
    SignData data = LeonGunWar.getPlugin().getTradeBoardManager().getSignData(b.getLocation());
    // データがないならreturn
    if (data == null) {
      return;
    }

    // クリック連打を対策
    // 前回クリックしたデータと同じデータであり、3秒以上たっていない場合はreturn
    if (Objects.equals(lastClicked.getOrDefault(p, null), data)
        && lastClickedMilli.getOrDefault(p, 0L) + 3000 > System.currentTimeMillis()) {
      return;
    }

    // 作成者を表示
    p.sendMessage(Chat.f("&a作成者: &e{0}", data.getPlayerName()));

    // クリック情報を保存
    lastClicked.put(p, data);
    lastClickedMilli.put(p, System.currentTimeMillis());
  }

  /**
   * 看板の4行がすべて空白であるかどうかを確認します
   *
   * @param lines 確認したいStringの配列
   * @return 全て空白ならtrue、1文字でも入っていればfalse
   */
  private boolean isEmpty(String[] lines) {
    return Arrays.stream(lines)
        .filter(Objects::nonNull)
        .map(String::trim)
        .allMatch(String::isEmpty);
  }

  /**
   * 武器交換掲示板かどうか、WorldGuardから確認する
   *
   * @param loc 確認したい座標
   * @return 武器交換掲示板のエリアならtrue、そうでなければfalse
   */
  private boolean inTradeBoardRegion(Location loc) {
    WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
    ApplicableRegionSet regions = wg.getRegionManager(loc.getWorld()).getApplicableRegions(loc);

    for (ProtectedRegion rg : regions) {
      if (rg.getId().toLowerCase().startsWith("keiziban")) {
        return true;
      }
    }

    return false;
  }
}

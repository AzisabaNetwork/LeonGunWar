package net.azisaba.lgw.core.tasks;

import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 定期的に期限切れの看板を探し、あった場合は削除するタスク
 *
 * @author siloneco
 */
public class SignRemoveTask extends BukkitRunnable {

  @Override
  public void run() {

    // 期限切れの看板をfilterメソッドで割り出し、AIRに変更する
    LeonGunWar.getPlugin().getTradeBoardManager().getAllSignData().stream()
        .filter(data -> data.getBreakAt() < System.currentTimeMillis())
        .forEach(
            data -> {

              // 座標からブロックを取得
              Block b = data.getLocation().getBlock();
              // 壁の看板か床に置いてある看板ならAIRに変更する
              if (b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
                b.setType(Material.AIR);
              }

              // 登録されているデータを削除する
              LeonGunWar.getPlugin().getTradeBoardManager().removeSignData(data.getLocation());
            });
  }
}

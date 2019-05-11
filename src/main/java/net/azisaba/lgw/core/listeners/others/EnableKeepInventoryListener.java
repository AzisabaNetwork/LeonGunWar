package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

import net.azisaba.lgw.core.LeonGunWar;

public class EnableKeepInventoryListener implements Listener {

	public EnableKeepInventoryListener() {
		// load: STARTUP
		// にしないためにプラグイン有効化時に全ワールドのKeepInventoryを有効化
		Bukkit.getWorlds().forEach(this::setEnableKeepInventory);
	}

	public void setEnableKeepInventory(World world) {
		// 既にKeepInventoryがtrueになってる場合はreturn
		if (world.getGameRuleValue("keepInventory").equals("true")) {
			return;
		}

		// KeepInventoryを有効化
		world.setGameRuleValue("keepInventory", "true");

		// コンソールに有効化したよと表示
		LeonGunWar.getPlugin().getLogger().info(world.getName() + " ワールドの keepInventory を true に設定したよ(´・ω・`)");
	}

	@EventHandler
	public void onWorldInit(WorldInitEvent e) {
		// PlayerDeathEventで処理しても、CrackShotが勝手に処理してアイテムが消えるため、
		// ワールド初期化時にゲームルールからKeepInventoryを有効化する
		setEnableKeepInventory(e.getWorld());
	}
}

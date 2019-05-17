package net.azisaba.lgw.core.listeners.others;

import java.util.HashMap;
import java.util.UUID;

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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.SignData;
import net.md_5.bungee.api.ChatColor;

public class TradeBoardListener implements Listener {

	private final long expireMilliSeconds = 1000L * 60L * 60L * 24L * 7L;

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerPlaceSignEvent(SignChangeEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();

		if (p.getGameMode() == GameMode.CREATIVE) {
			return;
		}
		if (!b.getType().toString().startsWith("SIGN") && !b.getType().toString().endsWith("SIGN")) {
			return;
		}

		if (!inKeizibanRegion(b.getLocation())) {
			return;
		}

		if (isEmpty(e.getLines())) {
			b.breakNaturally();
			p.sendMessage(ChatColor.RED + "空白の看板なため破壊しました");
			return;
		}

		String playerName = p.getName();
		UUID uuid = p.getUniqueId();
		long expire = System.currentTimeMillis() + expireMilliSeconds;

		boolean success = LeonGunWar.getPlugin().getTradeBoardManager().addSignData(b.getLocation(), playerName, uuid,
				expire);
		if (success) {
			p.sendMessage(ChatColor.GREEN + "看板を正常に登録しました！");
		} else {
			b.breakNaturally();
			p.sendMessage(ChatColor.RED + "看板の登録に失敗しました。申し訳ありませんが別の場所を利用してください。");
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();

		if (p.getGameMode() == GameMode.CREATIVE) {
			return;
		}

		if (!inKeizibanRegion(b.getLocation())) {
			return;
		}

		if (b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBreakSign(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();

		if (!inKeizibanRegion(b.getLocation())) {
			return;
		}

		if (p.getGameMode() == GameMode.CREATIVE) {
			LeonGunWar.getPlugin().getTradeBoardManager().removeSignData(b.getLocation());
			return;
		}

		SignData data = LeonGunWar.getPlugin().getTradeBoardManager().getSignData(b.getLocation());
		if (data == null) {
			e.setCancelled(true);
			p.sendMessage(ChatColor.RED + "自分の設置した看板のみ破壊することができます！");
			return;
		}

		if (data.getAuthor().equals(p.getUniqueId()) || p.getGameMode() == GameMode.CREATIVE) {
			LeonGunWar.getPlugin().getTradeBoardManager().removeSignData(b.getLocation());
			return;
		}

		e.setCancelled(true);
		p.sendMessage(
				ChatColor.RED + "この看板は" + ChatColor.YELLOW + data.getPlayerName() + ChatColor.RED
						+ "によって作成されたものです！");
	}

	private final HashMap<Player, SignData> lastClicked = new HashMap<>();
	private final HashMap<Player, Long> lastClickedMilli = new HashMap<>();

	@EventHandler
	public void onClickSign(PlayerInteractEvent e) {

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		if (b == null) {
			return;
		}

		SignData data = LeonGunWar.getPlugin().getTradeBoardManager().getSignData(b.getLocation());
		if (data == null) {
			return;
		}

		if (lastClicked.containsKey(p) && lastClickedMilli.containsKey(p)) {
			if (lastClicked.get(p).equals(data) && lastClickedMilli.get(p) + 3000 > System.currentTimeMillis()) {
				return;
			}
		}

		p.sendMessage(ChatColor.GREEN + "作成者: " + ChatColor.YELLOW + data.getPlayerName());

		lastClicked.put(p, data);
		lastClickedMilli.put(p, System.currentTimeMillis());
	}

	private boolean isEmpty(String[] lines) {
		for (String line : lines) {
			if (line == null) {
				continue;
			}
			if (!line.trim().equals("")) {
				return false;
			}
		}

		return true;
	}

	private boolean inKeizibanRegion(Location loc) {
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

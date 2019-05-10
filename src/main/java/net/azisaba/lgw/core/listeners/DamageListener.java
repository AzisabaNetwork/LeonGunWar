package net.azisaba.lgw.core.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.CSUtility;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.teams.BattleTeam;

public class DamageListener implements Listener {

	private final CSUtility crackShot = new CSUtility();

	/**
	 * プレイヤーを殺したことを検知するリスナー
	 * 死亡したプレイヤーの処理は他のリスナーで行います
	 */
	@EventHandler(ignoreCancelled = false)
	public void onKill(PlayerDeathEvent e) {
		// 試合中でなければreturn
		if (!MatchManager.isMatching()) {
			return;
		}

		// 殺したプレイヤーを取得
		Player killer = e.getEntity().getKiller();

		// 殺したプレイヤーがいない場合はreturn
		if (killer == null) {
			return;
		}

		// チームを取得
		BattleTeam killerTeam = MatchManager.getBattleTeam(killer);

		// killerTeamがnullの場合return
		if (killerTeam == null) {
			return;
		}

		// ポイントを追加
		MatchManager.addTeamPoint(killerTeam);
		// 個人キルを追加
		MatchManager.getKillDeathCounter().addKill(killer);

		// タイトルを表示
		killer.sendTitle("", ChatColor.RED + "+1 kill", 0, 20, 10);
		// 音を鳴らす
		killer.playSound(killer.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
	}

	/**
	 * 試合中のプレイヤーが死亡した場合、死亡カウントを増加させ、即時リスポーンさせます
	 */
	@EventHandler(ignoreCancelled = false)
	public void onDeath(PlayerDeathEvent e) {
		Player deathPlayer = e.getEntity();

		// チームを取得
		BattleTeam deathPlayerTeam = MatchManager.getBattleTeam(deathPlayer);

		// deathPlayerTeamがnullの場合return
		if (deathPlayerTeam == null) {
			return;
		}

		// 死亡数を追加
		MatchManager.getKillDeathCounter().addDeath(deathPlayer);

		// 即時リスポーン (座標指定は別リスナーで)
		deathPlayer.spigot().respawn();
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();

		// チームを取得
		BattleTeam playerTeam = MatchManager.getBattleTeam(p);
		// スポーン地点
		Location spawnPoint = null;

		// チームがnullではないならそのチームのスポーン地点にTPする
		if (playerTeam != null) {
			spawnPoint = MatchManager.getCurrentGameMap().getSpawnPoint(playerTeam);
		}

		// それでもまだspawnPointがnullの場合lobbyのスポーン地点を指定
		if (spawnPoint == null) {
			spawnPoint = MatchManager.getLobbySpawnLocation();
		}

		e.setRespawnLocation(spawnPoint);

		// 消火
		new BukkitRunnable() {
			@Override
			public void run() {
				p.setFireTicks(0);
			}
		}.runTaskLater(LeonGunWar.getPlugin(), 1);
	}

	/**
	 * キルログを変更するListener
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void deathMessageChanger(PlayerDeathEvent e) {
		Player p = e.getEntity();

		// 殺したEntityが居ない場合自滅とする
		if (p.getKiller() == null) {

			// チーム取得
			BattleTeam deathTeam = MatchManager.getBattleTeam(p);

			ChatColor nameColor = null;
			// チームがない場合グレー
			if (deathTeam == null) {
				nameColor = ChatColor.GRAY;
			} else {
				nameColor = deathTeam.getChatColor();
			}

			e.setDeathMessage(nameColor + p.getName() + ChatColor.GRAY + "は自滅した！");
			return;
		}

		Player killer = e.getEntity().getKiller();

		// 殺したアイテム
		ItemStack item = killer.getInventory().getItemInMainHand();

		// アイテム名を取得
		String itemName = "";
		if (item == null || item.getType() == Material.AIR) { // null または Air なら素手
			itemName = ChatColor.GOLD + "素手";
		} else if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) { // DisplayNameが指定されている場合
			// CrackShot Pluginを取得
			CSDirector crackshot = (CSDirector) LeonGunWar.getPlugin().getServer().getPluginManager()
					.getPlugin("CrackShot");

			// 銃ID取得
			String nodes = crackShot.getWeaponTitle(item);
			// DisplayNameを取得
			itemName = crackshot.getString(nodes + ".Item_Information.Item_Name");
		} else { // それ以外
			itemName = ChatColor.GOLD + item.getType().name();
		}

		// killerのチーム
		BattleTeam killerTeam = MatchManager.getBattleTeam(killer);
		// pのチーム (死んだプレイヤーのチーム)
		BattleTeam deathTeam = MatchManager.getBattleTeam(p);

		StringBuilder builder = new StringBuilder();

		// killerTeamがnullではない場合は色を取得 (nullなら何もしない)
		if (killerTeam != null) {
			builder.append(killerTeam.getChatColor() + "");
		}

		// プレイヤー名とキルしたアイテムを表示
		builder.append(killer.getName() + " " + ChatColor.GRAY + "━━━[" + ChatColor.RESET + itemName + ChatColor.GRAY
				+ "]━━━> ");

		// deathTeamがnullではない場合は色を取得
		if (deathTeam != null) {
			builder.append(deathTeam.getChatColor() + "");
		} else { // その他の場合は白
			builder.append(ChatColor.RESET + "");
		}

		// プレイヤー名表示
		builder.append(p.getName());

		// メッセージ変更
		e.setDeathMessage(builder.toString());
	}

	@EventHandler
	public void onFireworksDamage(EntityDamageByEntityEvent e) {
		// Entitiyによる爆発ではない場合はreturn
		if (e.getCause() != DamageCause.ENTITY_EXPLOSION) {
			return;
		}

		// ダメージを受けたEntityがPlayerでなければreturn
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		// ダメージを与えたEntityが花火でなければreturn
		if (!(e.getDamager() instanceof Firework)) {
			return;
		}

		// キャンセル
		e.setCancelled(true);
	}
}

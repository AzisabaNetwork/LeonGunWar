package net.azisaba.lgw.core.listeners.weapons;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.Damage;

/**
 * ライトニングストライクをCrackShotで実装するとキャンセルできないのでこっちで実装
 *
 * @author siloneco
 *
 */
public class LightningStrikeListener implements Listener {

    private final int RADIUS = 15;
    private final int TNT_AMOUNT = 5;
    private final int PHASE = 5;

    private final HashMap<UUID, Long> cooldown = new HashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack clicked = e.getItem();

        if ( !e.getAction().toString().startsWith("RIGHT_CLICK") ) {
            return;
        }

        // 持っているアイテムがnullの場合return
        if ( clicked == null ) {
            return;
        }
        // metaがない場合return
        if ( !clicked.hasItemMeta() ) {
            return;
        }

        if ( !isLightningStrikeItem(clicked) ) {
            return;
        }

        if ( cooldown.getOrDefault(p.getUniqueId(), 0L) + 30000L > System.currentTimeMillis() ) {
            return;
        }

        cooldown.put(p.getUniqueId(), System.currentTimeMillis());

        clicked.setAmount(clicked.getAmount() - 1);

        Bukkit.getOnlinePlayers().forEach(online -> {
            online.playSound(p.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 2, 0.5f);
            online.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2, 0.5f);
        });

        ItemStack looks = new ItemStack(Material.REDSTONE_TORCH_ON);
        ItemMeta looksMeta = looks.getItemMeta();
        looksMeta.setDisplayName(System.currentTimeMillis() + "");
        looks.setItemMeta(looksMeta);

        Item dropItem = p.getWorld().dropItemNaturally(p.getEyeLocation(), looks);
        dropItem.setVelocity(p.getLocation().getDirection().multiply(1));
        dropItem.setMetadata("UnpickableItem", new FixedMetadataValue(LeonGunWar.getPlugin(), true));

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Chat.f("say &e{0} &5のライトニングストライクが到着、爆撃に備えろ", p.getName()));

        new BukkitRunnable() {

            private int counter = 0;
            private final Item dropItemCache = dropItem;

            @Override
            public void run() {
                if ( counter >= 5 ) {
                    cancel();
                    return;
                }

                if ( counter == 2 ) {
                    p.sendMessage(Chat.f("&e了解。攻撃を行う{0}、巻き込まれるなよ...オーバー", p.getName()));
                }

                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.getWorld() == dropItemCache.getWorld() && p.getLocation().distance(dropItemCache.getLocation()) <= 100)
                        .forEach(p -> {
                            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMEN_STARE, 2, 2f);
                        });
                counter++;
            }
        }.runTaskTimer(LeonGunWar.getPlugin(), 20, 20);

        Bukkit.getScheduler().runTaskLater(LeonGunWar.getPlugin(), new Runnable() {

            private final Item droppedItem = dropItem;

            @Override
            public void run() {

                Location loc = droppedItem.getLocation();

                if ( !droppedItem.isDead() ) {
                    droppedItem.remove();
                }

                spawnTNT(loc, p);
            }
        }, 20 * 2);
    }

    @EventHandler
    public void onJoin(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if ( cooldown.containsKey(uuid) ) {
            cooldown.remove(uuid);
        }
    }

    @EventHandler
    public void disablePickup(EntityPickupItemEvent e) {
        Item item = e.getItem();
        if ( item.hasMetadata("UnpickableItem") ) {
            if ( item.getMetadata("UnpickableItem").get(0).asBoolean() == true ) {
                e.setCancelled(true);
            }
        }
    }

    private void spawnTNT(Location loc, Player p) {
        new BukkitRunnable() {

            private final Location middleLoc = loc.clone();
            private int processed = 0;

            @SuppressWarnings("deprecation")
            @Override
            public void run() {

                if ( processed >= PHASE ) {
                    cancel();
                    return;
                }

                for ( int i = 0; i < TNT_AMOUNT; i++ ) {
                    Location loc = getRandomLocation(middleLoc.clone());
                    loc.setY(middleLoc.getY() + 70);

                    FallingBlock fall = loc.getWorld().spawnFallingBlock(loc, Material.TNT, (byte) 0);
                    fall.setHurtEntities(false);
                    fall.setDropItem(false);
                    fall.setMetadata("CreateExplosion", new FixedMetadataValue(LeonGunWar.getPlugin(), true));
                    fall.setMetadata("RequestedPlayer", new FixedMetadataValue(LeonGunWar.getPlugin(), p.getName()));
                }

                processed++;
            }

        }.runTaskTimer(LeonGunWar.getPlugin(), 20, 20);
    }

    @EventHandler
    public void onTNTPlaced(EntityChangeBlockEvent e) {
        Entity ent = e.getEntity();
        if ( !(ent instanceof FallingBlock) ) {
            return;
        }

        if ( ent.hasMetadata("CreateExplosion") ) {
            if ( ent.getMetadata("CreateExplosion").get(0).asBoolean() == true ) {
                e.setCancelled(true);

                Player p = Bukkit.getPlayerExact(ent.getMetadata("RequestedPlayer").get(0).asString());

                if ( p == null ) {
                    return;
                }

                Location loc = e.getEntity().getLocation();
                loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 3, false, false);
                TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
                tnt.setFuseTicks(0);
                tnt.setMetadata("PrimedPlayer", new FixedMetadataValue(LeonGunWar.getPlugin(), p.getName()));

                Bukkit.getOnlinePlayers().forEach(online -> {
                    online.playSound(loc, Sound.ENTITY_ENDERDRAGON_HURT, 1, 1.5f);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity damaged = e.getEntity();

        if ( !(damaged instanceof LivingEntity) || !(damager instanceof TNTPrimed) ) {
            return;
        }

        if ( !damager.hasMetadata("PrimedPlayer") ) {
            return;
        }

        Player p = Bukkit.getPlayerExact(damager.getMetadata("PrimedPlayer").get(0).asString());

        if ( p == null ) {
            return;
        }

        e.setCancelled(true);

        Damage.damageNaturally((LivingEntity) damaged, e.getDamage(), p);
    }

    private ItemStack lightningStrike = null;

    private boolean isLightningStrikeItem(ItemStack item) {
        ItemStack real = getLightningStrike();

        if ( !item.hasItemMeta() ) {
            return false;
        }
        ItemMeta itemMeta = item.getItemMeta();
        ItemMeta realMeta = real.getItemMeta();

        if ( !realMeta.getDisplayName().equals(itemMeta.getDisplayName()) ) {
            return false;
        }
        if ( !realMeta.getLore().equals(itemMeta.getLore()) ) {
            return false;
        }

        return true;
    }

    private ItemStack getLightningStrike() {
        if ( lightningStrike == null ) {
            lightningStrike = new ItemStack(Material.BLAZE_POWDER);
            ItemMeta meta = lightningStrike.getItemMeta();
            meta.setDisplayName(Chat.f("&6ライトニングストライク&6"));
            meta.setLore(Arrays.asList(Chat.f("&c救援用の爆弾を複数回分けて投下する")));
            lightningStrike.setItemMeta(meta);
        }

        return lightningStrike;
    }

    private Location getRandomLocation(Location middle) {
        int x = getRandom(RADIUS * -1, RADIUS);
        int z = getRandom(RADIUS * -1, RADIUS);

        middle.add(x, 0, z);

        return middle;
    }

    private int getRandom(int from, int to) {
        if ( to - from <= 0 ) {
            throw new IllegalArgumentException();
        }

        return new Random().nextInt(to - from + 1) + from;
    }
}

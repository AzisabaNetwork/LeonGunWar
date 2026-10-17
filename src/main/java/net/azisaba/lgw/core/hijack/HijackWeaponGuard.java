package net.azisaba.lgw.core.hijack;

import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Remote C4 has no cancellable CrackShot API and may ignore Bukkit cancellation.
 * Guard only its existing use handlers before they consume/place/detonate anything.
 * Restore original listeners on disable, preserving their priority/cancellation policy.
 */
public final class HijackWeaponGuard implements Listener, AutoCloseable {
    private record Wrapped(HandlerList list, RegisteredListener original, RegisteredListener wrapper) { }
    private final List<Wrapped> wrapped = new ArrayList<>();
    private final HijackController controller;
    private final Function<ItemStack, String> identify;
    private final Function<String, String> setting;

    public HijackWeaponGuard(HijackController controller, Function<ItemStack, String> identify,
                             Function<String, String> setting) {
        this.controller = controller;
        this.identify = identify;
        this.setting = setting;
    }

    public void install() {
        if (!wrapped.isEmpty()) throw new IllegalStateException("Weapon guard already installed");
        for (HandlerList list : List.of(PlayerInteractEvent.getHandlerList(), BlockPlaceEvent.getHandlerList(), PlayerDropItemEvent.getHandlerList(), EntityDamageByEntityEvent.getHandlerList())) {
            boolean found = false;
            for (RegisteredListener original : list.getRegisteredListeners()) {
                if (!original.getPlugin().getName().equals("CrackShot")) continue;
                found = true;
                RegisteredListener wrapper = new RegisteredListener(original.getListener(), (listener, event) -> {
                    if (deny(event)) ((Cancellable) event).setCancelled(true);
                    else original.callEvent(event);
                }, original.getPriority(), original.getPlugin(), original.isIgnoringCancelled());
                list.unregister(original);
                list.register(wrapper);
                wrapped.add(new Wrapped(list, original, wrapper));
            }
            if (!found && (list == PlayerInteractEvent.getHandlerList() || list == BlockPlaceEvent.getHandlerList())) {
                close();
                throw new IllegalStateException("CrackShotの使用/設置リスナーが見つからず、HIJACKの武器制限を適用できません。");
            }
        }
        if (wrapped.isEmpty()) throw new IllegalStateException("CrackShotの使用リスナーが見つからず、HIJACKの武器制限を適用できません。");
    }

    boolean deny(Event event) {
        Player player;
        ItemStack item;
        if (event instanceof PlayerInteractEvent interact) {
            if (interact.getAction() == org.bukkit.event.block.Action.PHYSICAL) return false;
            player = interact.getPlayer(); item = player.getInventory().getItemInMainHand();
        } else if (event instanceof BlockPlaceEvent place) {
            player = place.getPlayer(); item = place.getItemInHand();
        } else if (event instanceof PlayerDropItemEvent drop) {
            player = drop.getPlayer(); item = drop.getItemDrop().getItemStack();
        } else if (event instanceof EntityDamageByEntityEvent damage && damage.getDamager() instanceof Player attacker) {
            // CrackShot can call its interact handler directly from its melee handler.
            player = attacker; item = player.getInventory().getItemInMainHand();
        } else return false;
        if (!controller.isParticipant(player) || !blocked(identify.apply(item))) return false;
        player.sendMessage("§cHIJACKではこの武器の投擲・設置・起爆はできません。");
        return true;
    }

    public boolean blocked(String id) {
        if (id == null || !controller.isRunning()) return false;
        HijackRules rules = controller.getActive();
        if (rules.blockedIds().stream().anyMatch(id::equalsIgnoreCase)) return true;
        String projectile = setting.apply(id + ".Shooting.Projectile_Type");
        String device = setting.apply(id + ".Explosive_Devices.Device_Type");
        boolean deviceEnabled = Boolean.parseBoolean(setting.apply(id + ".Explosive_Devices.Enable"));
        return (rules.flag("weapons.block-grenades") && ("grenade".equalsIgnoreCase(projectile)
                || (deviceEnabled && "itembomb".equalsIgnoreCase(device))))
                || (rules.flag("weapons.block-c4") && deviceEnabled && "remote".equalsIgnoreCase(device));
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepare(WeaponPrepareShootEvent event) {
        if (controller.isParticipant(event.getPlayer()) && blocked(event.getWeaponTitle())) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreShoot(WeaponPreShootEvent event) {
        if (controller.isParticipant(event.getPlayer()) && blocked(event.getWeaponTitle())) event.setCancelled(true);
    }
    @Override public void close() {
        for (Wrapped entry : wrapped) {
            entry.list().unregister(entry.wrapper());
            if (entry.original().getPlugin().isEnabled()) entry.list().register(entry.original());
        }
        wrapped.clear();
    }
}

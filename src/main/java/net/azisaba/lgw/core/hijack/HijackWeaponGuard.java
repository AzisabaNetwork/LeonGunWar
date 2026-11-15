package net.azisaba.lgw.core.hijack;

import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;
import org.bukkit.Material;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Remote C4 has no cancellable CrackShot API and may ignore Bukkit cancellation.
 * Guard only its existing use handlers before they consume/place/detonate anything.
 * Restore original listeners on disable, preserving their priority/cancellation policy.
 */
public final class HijackWeaponGuard implements Listener, AutoCloseable {
    private record Wrapped(HandlerList list, RegisteredListener original, RegisteredListener wrapper) { }
    private record ItemKey(Material material, short durability, String displayName) { }

    private static final int TITLE_CACHE_SIZE = 256;

    private final List<Wrapped> wrapped = new ArrayList<>();
    private final HijackController controller;
    private final Function<ItemStack, String> identify;
    private final Function<String, String> setting;

    // CrackShot's getWeaponTitle clones the item meta and, for named items, allocates a
    // scratch ItemStack just to normalize the display name. Memoize the title by the fields
    // CrackShot itself uses to identify a weapon so repeated events on the same item stay cheap.
    // CrackShot 0.98.11 builds parentlist/convIDs on enable and has no runtime reload command
    // or event, so the mapping is stable for the JVM lifetime and this cache cannot go stale.
    private final Map<ItemKey, String> titleCache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<ItemKey, String> eldest) {
            return size() > TITLE_CACHE_SIZE;
        }
    };

    // Rules are immutable snapshots; refresh the derived flags/set only when the active snapshot
    // changes (begin/applySettings). The weapon-title cache stays valid because the item signature
    // already captures what CrackShot maps to a title.
    private HijackRules cachedRules;
    private boolean cachedRestrictsWeapons;
    private List<String> cachedBlockedIds = List.of();

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
        if (!controller.isParticipant(player) || !restrictsWeapons()) return false;
        String id = weaponTitle(item);
        if (id == null || !blocked(id)) return false;
        player.sendMessage("§cHIJACKではこの武器の投擲・設置・起爆はできません。");
        return true;
    }

    /** {@code identify.apply} is the hot path; bypass it entirely when the current rules block nothing. */
    private boolean restrictsWeapons() {
        refreshRules();
        return cachedRestrictsWeapons;
    }

    private void refreshRules() {
        HijackRules rules = controller.getActive();
        if (rules == cachedRules) return;
        cachedRules = rules;
        if (rules == null) {
            cachedRestrictsWeapons = false;
            cachedBlockedIds = List.of();
            return;
        }
        cachedRestrictsWeapons = rules.flag("weapons.block-grenades")
                || rules.flag("weapons.block-c4")
                || !rules.blockedIds().isEmpty();
        // blockedIds() is already an immutable copy; cache the list as-is and keep
        // equalsIgnoreCase matching so lookup semantics are identical to the old stream.
        cachedBlockedIds = rules.blockedIds();
    }

    private String weaponTitle(ItemStack item) {
        // Null/no-meta items are delegated to identify: the production identify returns null here,
        // but an injected title function may still resolve one, and this keeps the fallback
        // equivalent to the original unconditional identify.apply call.
        if (item == null || !item.hasItemMeta()) return identify.apply(item);
        ItemMeta meta = item.getItemMeta();
        // CrackShot's getWeaponTitle requires a display name (itemIsSafe); no name means no weapon.
        if (meta == null || !meta.hasDisplayName()) return null;
        String displayName = meta.getDisplayName();
        // Attachment names dispatch through a CrackShot event, so keep the original live lookup.
        if (displayName.indexOf('◊') >= 0) return identify.apply(item);
        ItemKey key = new ItemKey(item.getType(), item.getDurability(), displayName);
        String title = titleCache.get(key);
        if (title != null || titleCache.containsKey(key)) return title;
        title = identify.apply(item);
        titleCache.put(key, title);
        return title;
    }

    public boolean blocked(String id) {
        if (id == null || !controller.isRunning()) return false;
        refreshRules();
        if (!cachedRestrictsWeapons) return false;
        return computeBlocked(cachedRules, id);
    }

    private boolean computeBlocked(HijackRules rules, String id) {
        for (String blocked : cachedBlockedIds) {
            if (blocked.equalsIgnoreCase(id)) return true;
        }
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
        titleCache.clear();
    }
}

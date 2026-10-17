package net.azisaba.lgw.core.hijack;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Collection;

/** One lightweight task for the match floor outline and private, time-limited admin previews. */
public final class HijackAreaDisplay implements AutoCloseable {
    private static final Particle DUST = Particle.REDSTONE;
    private record Preview(HijackController.AreaView view, List<HijackOutline.Point> points, int remaining, boolean active) { }
    private final LeonGunWar plugin;
    private final HijackController controller;
    private final Map<UUID, Preview> previews = new HashMap<>();
    private BukkitTask task;
    private int pulses;
    private HijackController.AreaView floorView;
    private List<HijackOutline.Point> floorPoints = List.of();

    public HijackAreaDisplay(LeonGunWar plugin, HijackController controller) {
        this.plugin = plugin;
        this.controller = controller;
    }
    public void start() {
        if (task == null) task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::pulse, 10L, 10L);
    }
    public List<String> mapNames() {
        return plugin.getMapsConfig().getAllGameMap().stream().filter(map -> !map.getHijackAreas().isEmpty())
                .map(GameMap::getMapName).sorted().toList();
    }
    public void preview(Player player, boolean next, String mapName) {
        HijackController.AreaView view;
        if (!next) {
            if (!mapName.isBlank()) throw new IllegalArgumentException("activeにはマップ名を指定しません。");
            view = controller.activeAreaView();
            if (view == null) throw new IllegalArgumentException("HIJACK試合中ではありません。preview next <マップ名> を使ってください。");
        } else {
            GameMap current = plugin.getManager().getCurrentGameMap();
            String name = mapName.isBlank() && current != null ? current.getMapName() : mapName;
            if (name.isBlank()) throw new IllegalArgumentException("preview next <マップ名> を指定してください。");
            List<GameMap> matches = plugin.getMapsConfig().getAllGameMap().stream()
                    .filter(map -> map.getMapName().equalsIgnoreCase(name)).toList();
            if (matches.size() != 1) throw new IllegalArgumentException("マップ名が見つからないか重複しています: " + name);
            view = controller.nextAreaView(matches.get(0));
        }
        if (view.areas().isEmpty()) throw new IllegalArgumentException("このマップには占領範囲がありません。");
        if (!player.getWorld().getUID().equals(view.worldId())) {
            throw new IllegalArgumentException("対象マップと同じワールドで実行してください。");
        }
        previews.put(player.getUniqueId(), new Preview(view, HijackOutline.points(view.areas(), false), 60, !next));
        player.sendMessage("§b" + (next ? "読込済み設定" : "試合に適用中") + "の占領範囲: " + view.mapName()
                + "（自分だけに30秒間表示。/lgwadmin hijack preview stop で停止）");
        for (int i = 0; i < view.areas().size(); i++) {
            var area = view.areas().get(i);
            var min = area.getMin(); var max = area.getMax();
            player.sendMessage(String.format(Locale.ROOT, "§7範囲%d: X %.2f～%.2f / Y %.2f～%.2f / Z %.2f～%.2f",
                    i + 1, min.getX(), max.getX(), min.getY(), max.getY(), min.getZ(), max.getZ()));
        }
        Location feet = player.getLocation();
        boolean inside = view.areas().stream().anyMatch(area -> area.isInArea(feet.toVector()));
        player.sendMessage(String.format(Locale.ROOT, "§7現在の足元: X %.2f / Y %.2f / Z %.2f（%s）",
                feet.getX(), feet.getY(), feet.getZ(), inside ? "範囲内" : "範囲外"));
    }
    public void stopPreview(Player player) { previews.remove(player.getUniqueId()); }

    void pulse() {
        HijackController.AreaView view = controller.activeAreaView();
        if (!Objects.equals(floorView, view)) {
            floorView = view;
            floorPoints = view == null ? List.of() : HijackOutline.points(view.areas(), true);
        }
        if (view != null && pulses++ % 2 == 0) {
            World world = plugin.getServer().getWorld(view.worldId());
            Color color = floorColor(controller.occupants().values());
            if (world != null) for (Player viewer : world.getPlayers()) draw(viewer, floorPoints, color, .9f, 64, .1);
        }
        var iterator = previews.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Preview preview = entry.getValue();
            if (preview.active) {
                if (view == null) { iterator.remove(); continue; }
                if (!Objects.equals(preview.view, view)) preview = new Preview(view, HijackOutline.points(view.areas(), false), preview.remaining, true);
            }
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player == null || !player.isOnline() || !player.hasPermission("leongunwar.command.leongunwaradmin")
                    || !player.getWorld().getUID().equals(preview.view.worldId()) || preview.remaining <= 0) {
                iterator.remove(); continue;
            }
            draw(player, preview.points, Color.WHITE, .9f, 96, 0);
            entry.setValue(new Preview(preview.view, preview.points, preview.remaining - 1, preview.active));
        }
    }
    private static Color floorColor(Collection<BattleTeam> teams) {
        boolean red = teams.contains(BattleTeam.RED), blue = teams.contains(BattleTeam.BLUE);
        if (red && blue) return Color.fromRGB(255, 235, 32);
        if (red) return Color.fromRGB(255, 64, 64);
        if (blue) return Color.fromRGB(64, 144, 255);
        return Color.WHITE;
    }

    private static void draw(Player player, List<HijackOutline.Point> points, Color color, float size, double radius, double heightOffset) {
        Location origin = player.getLocation();
        Particle.DustOptions dust = new Particle.DustOptions(color, size);
        // Bound packets per viewer even when many areas are configured, and only send nearby points.
        int stride = Math.max(1, (int) Math.ceil(points.size() / 512.0));
        for (int i = 0; i < points.size(); i += stride) {
            var point = points.get(i);
            Location location = new Location(player.getWorld(), point.x(), point.y() + heightOffset, point.z());
            if (origin.distanceSquared(location) <= radius * radius) {
                player.spawnParticle(DUST, location, 1, 0, 0, 0, 0, dust);
            }
        }
    }
    @Override public void close() {
        if (task != null) { task.cancel(); task = null; }
        previews.clear(); floorView = null; floorPoints = List.of();
    }
}

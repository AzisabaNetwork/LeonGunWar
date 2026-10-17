package net.azisaba.lgw.core.hijack;

import net.azisaba.lgw.core.util.Area3D;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/** Samples only the edges; even a very large map cannot create an unbounded edge loop. */
final class HijackOutline {
    record Point(double x, double y, double z) { }
    static List<Point> points(List<Area3D> areas, boolean floorOnly) {
        List<Point> result = new ArrayList<>();
        for (Area3D area : areas) {
            Vector min = area.getMin(), max = area.getMax();
            min.checkFinite(); max.checkFinite();
            Vector[] corners = {
                    new Vector(min.getX(), min.getY(), min.getZ()), new Vector(max.getX(), min.getY(), min.getZ()),
                    new Vector(max.getX(), min.getY(), max.getZ()), new Vector(min.getX(), min.getY(), max.getZ()),
                    new Vector(min.getX(), max.getY(), min.getZ()), new Vector(max.getX(), max.getY(), min.getZ()),
                    new Vector(max.getX(), max.getY(), max.getZ()), new Vector(min.getX(), max.getY(), max.getZ())
            };
            for (int i = 0; i < 4; i++) {
                edge(result, corners[i], corners[(i + 1) % 4], floorOnly ? 1.5 : .75);
                if (!floorOnly) {
                    edge(result, corners[i + 4], corners[(i + 1) % 4 + 4], .75);
                    edge(result, corners[i], corners[i + 4], .75);
                }
            }
        }
        return List.copyOf(result);
    }
    private static void edge(List<Point> points, Vector from, Vector to, double spacing) {
        int steps = Math.max(1, (int) Math.min(32, Math.ceil(from.distance(to) / spacing)));
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            points.add(new Point(from.getX() + (to.getX() - from.getX()) * t,
                    from.getY() + (to.getY() - from.getY()) * t, from.getZ() + (to.getZ() - from.getZ()) * t));
        }
    }
}

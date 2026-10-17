package net.azisaba.lgw.core.hijack;

import net.azisaba.lgw.core.util.Area3D;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HijackOutlineTest {
    @Test void floorAndBoxUseTheExactMapsBoundsWithoutHeightOrWidthOverrides() {
        Area3D area = new Area3D(new Vector(-9, 48, -1741), new Vector(-14, 52, -1736));
        var min = area.getMin(); var max = area.getMax();
        assertEquals(48, min.getY()); assertEquals(52, max.getY());
        assertEquals(5, max.getX() - min.getX());
        var floor = HijackOutline.points(List.of(area), true);
        var box = HijackOutline.points(List.of(area), false);
        assertTrue(floor.stream().allMatch(p -> p.y() == 48));
        assertTrue(box.stream().anyMatch(p -> p.y() == 52));
        assertTrue(box.stream().allMatch(p -> p.y() >= 48 && p.y() <= 52));
        assertTrue(box.contains(new HijackOutline.Point(min.getX(), min.getY(), min.getZ())));
        assertTrue(box.contains(new HijackOutline.Point(max.getX(), max.getY(), max.getZ())));
        for (var p : box) {
            int boundaryAxes = (p.x() == min.getX() || p.x() == max.getX() ? 1 : 0)
                    + (p.y() == min.getY() || p.y() == max.getY() ? 1 : 0)
                    + (p.z() == min.getZ() || p.z() == max.getZ() ? 1 : 0);
            assertTrue(boundaryAxes >= 2, "Only edges may be drawn");
        }
    }
    @Test void hugeAndFlatAreasHaveBoundedSamplingAndBoundsCannotBeMutatedThroughGetters() {
        Area3D area = new Area3D(new Vector(-1000000, 64, -1000000), new Vector(1000000, 64, 1000000));
        assertTrue(HijackOutline.points(List.of(area), true).size() <= 132);
        assertTrue(HijackOutline.points(List.of(area), false).size() <= 396);
        area.getMin().setY(999);
        assertEquals(64, area.getMin().getY());
        assertTrue(HijackOutline.points(List.of(area), false).stream().allMatch(p -> p.y() == 64));
    }
}

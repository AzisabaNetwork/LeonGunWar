package net.azisaba.lgw.core.util;

import org.bukkit.util.Vector;

public class Area3D {
    private Vector min = new Vector(0, 0, 0);
    private Vector max = new Vector(0, 0, 0);

    public Area3D(Vector min, Vector max) {
        this.min = new Vector(
            Math.min(min.getX(), max.getX()),
            Math.min(min.getY(), max.getY()),
            Math.min(min.getZ(), max.getZ())
        );
        this.max = new Vector(
            Math.max(min.getX(), max.getX()),
            Math.max(min.getY(), max.getY()),
            Math.max(min.getZ(), max.getZ())
        );
    }

    public boolean isInArea(Vector point) {
        return point.getX() >= min.getX() && point.getX() <= max.getX()
            && point.getY() >= min.getY() && point.getY() <= max.getY()
            && point.getZ() >= min.getZ() && point.getZ() <= max.getZ();
    }

    public Vector getMin() { return min.clone(); }
    public Vector getMax() { return max.clone(); }

    /** Horizontal distance to the rectangle, only inside its allowed vertical band. */
    public double distanceWithinHeight(Vector point) {
        if (point.getY() < min.getY() || point.getY() > max.getY()) return Double.POSITIVE_INFINITY;
        double dx = Math.max(Math.max(min.getX() - point.getX(), point.getX() - max.getX()), 0);
        double dz = Math.max(Math.max(min.getZ() - point.getZ(), point.getZ() - max.getZ()), 0);
        return Math.hypot(dx, dz);
    }
}

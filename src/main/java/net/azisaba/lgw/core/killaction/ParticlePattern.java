package net.azisaba.lgw.core.killaction;

import java.util.ArrayList;
import java.util.List;

/** Coordinates for the built-in shapes; each frame is relative to its origin. */
public final class ParticlePattern {
    private ParticlePattern() { }

    public static List<KillActionCatalog.Point> points(KillActionCatalog.ParticleEffect effect, int tick) {
        int count = effect.count();
        double progress = effect.durationTick() <= 1 ? 0 : (double) (tick - effect.startTick()) / (effect.durationTick() - 1);
        double rotation = progress * Math.PI * 2;
        double radius = effect.radius();
        double height = effect.height();
        List<KillActionCatalog.Point> points = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double u = (double) i / count;
            double angle = u * Math.PI * 2 + rotation;
            double x = 0, y = 0, z = 0;
            switch (effect.shape()) {
                case "burst" -> {
                    double spread = radius * Math.min(1, .2 + progress);
                    double elevation = Math.asin(2 * pseudo(i, tick) - 1);
                    x = spread * Math.cos(angle) * Math.cos(elevation);
                    y = spread * Math.sin(elevation);
                    z = spread * Math.sin(angle) * Math.cos(elevation);
                }
                case "ring" -> { x = radius * Math.cos(angle); z = radius * Math.sin(angle); }
                case "sphere" -> {
                    double latitude = Math.acos(1 - 2 * (i + .5) / count);
                    double longitude = i * 2.399963229728653 + rotation;
                    x = radius * Math.sin(latitude) * Math.cos(longitude);
                    y = radius * Math.cos(latitude);
                    z = radius * Math.sin(latitude) * Math.sin(longitude);
                }
                case "spiral" -> {
                    double r = radius * u;
                    x = r * Math.cos(angle * 2); y = height * u; z = r * Math.sin(angle * 2);
                }
                case "double_helix" -> {
                    double a = u * Math.PI * 4 + rotation + (i % 2) * Math.PI;
                    x = radius * Math.cos(a); y = height * u; z = radius * Math.sin(a);
                }
                case "heart" -> {
                    double a = u * Math.PI * 2;
                    x = radius * Math.pow(Math.sin(a), 3);
                    y = height * (13 * Math.cos(a) - 5 * Math.cos(2 * a) - 2 * Math.cos(3 * a) - Math.cos(4 * a)) / 18;
                    z = .15 * Math.sin(rotation);
                }
                case "star" -> {
                    double r = radius * (.55 + .45 * Math.cos(5 * angle));
                    x = r * Math.cos(angle); z = r * Math.sin(angle);
                }
                case "flower" -> {
                    double r = radius * Math.cos(6 * angle);
                    x = r * Math.cos(angle); z = r * Math.sin(angle);
                    y = .15 * Math.sin(rotation);
                }
                case "crown" -> {
                    x = radius * Math.cos(angle); z = radius * Math.sin(angle);
                    y = height * (.3 + .7 * Math.pow(Math.abs(Math.sin(5 * angle / 2)), 3));
                }
                case "vortex" -> {
                    double r = radius * (1 - .8 * u);
                    x = r * Math.cos(angle * 3); y = height * u; z = r * Math.sin(angle * 3);
                }
                case "fountain" -> {
                    double r = radius * u;
                    x = r * Math.cos(angle); z = r * Math.sin(angle);
                    y = height * 4 * u * (1 - u) * (1 + .2 * progress);
                }
                case "wave" -> {
                    x = radius * (2 * u - 1);
                    y = height * .4 * Math.sin(2 * Math.PI * (u * 2 - progress));
                    z = .3 * Math.sin(2 * Math.PI * u);
                }
                case "comet" -> {
                    double tail = radius * u;
                    x = tail * Math.cos(rotation); z = tail * Math.sin(rotation);
                    y = height * progress + (pseudo(i, tick + 1) - .5) * .4;
                }
                case "lightning" -> {
                    y = height * u;
                    x = radius * (pseudo(i / 3, tick) - .5);
                    z = radius * (pseudo(i / 3, tick + 99) - .5);
                }
                case "cube" -> {
                    int edge = i % 12;
                    double v = (double) (i / 12) / Math.max(1, (count + 11) / 12 - 1);
                    double a = (edge & 1) == 0 ? -radius : radius;
                    double b = (edge & 2) == 0 ? -radius : radius;
                    if (edge < 4) { x = -radius + 2 * radius * v; y = a; z = b; }
                    else if (edge < 8) { x = a; y = -radius + 2 * radius * v; z = b; }
                    else { x = a; y = b; z = -radius + 2 * radius * v; }
                }
                default -> throw new IllegalArgumentException("unknown shape " + effect.shape());
            }
            points.add(new KillActionCatalog.Point(x, y, z));
        }
        return points;
    }

    private static double pseudo(int index, int tick) {
        long value = (index * 1103515245L + tick * 12345L) ^ 0x9E3779B97F4A7C15L;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        return (value & 0xffffL) / 65535.0;
    }
}

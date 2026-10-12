package net.azisaba.lgw.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public final class AnimatedVoteTitle {
    public static final int PERIOD_TICKS = 120;
    private static final int[] COLORS = {0xBABCBF, 0xB5DFE8, 0xEBC7EB};

    private AnimatedVoteTitle() {
    }

    public static Component render(String text, long tick) {
        int[] characters = text.codePoints().toArray();
        double phase = Math.floorMod(tick, PERIOD_TICKS) * (double) COLORS.length / PERIOD_TICKS;
        TextComponent.Builder title = Component.text();
        for (int i = 0; i < characters.length; i++) {
            // タイトル全体に3色を広げ、時間とともに循環させる。
            double position = characters.length <= 1 ? 0 : i * 2.0 / (characters.length - 1);
            title.append(Component.text(new String(Character.toChars(characters[i])), colorAt(position + phase)));
        }
        return title.build();
    }

    static TextColor colorAt(double position) {
        double wrapped = position - Math.floor(position / COLORS.length) * COLORS.length;
        int index = (int) wrapped;
        double fraction = wrapped - index;
        // 境界で速度も連続になる補間。最後の色から最初の色にも滑らかにつなぐ。
        double blend = (1 - Math.cos(Math.PI * fraction)) / 2;
        int from = COLORS[index];
        int to = COLORS[(index + 1) % COLORS.length];
        return TextColor.color(
                interpolate((from >> 16) & 255, (to >> 16) & 255, blend),
                interpolate((from >> 8) & 255, (to >> 8) & 255, blend),
                interpolate(from & 255, to & 255, blend));
    }

    private static int interpolate(int from, int to, double blend) {
        return (int) Math.round(from + (to - from) * blend);
    }
}

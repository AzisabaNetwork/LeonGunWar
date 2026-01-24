package net.azisaba.lgw.core.listeners.others;

import net.azisaba.lgw.core.util.Chat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.stream.IntStream;

public class SignWithColorListener implements Listener {

    @EventHandler
    public void onSignWithColor(SignChangeEvent event) {
        String[] lines = event.getLines();
        IntStream.range(0, lines.length)
                .forEach(i -> event.setLine(i, Chat.f(lines[i])));
    }
}

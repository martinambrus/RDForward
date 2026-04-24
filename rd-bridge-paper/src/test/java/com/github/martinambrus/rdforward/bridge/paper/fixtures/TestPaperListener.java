package com.github.martinambrus.rdforward.bridge.paper.fixtures;

import com.github.martinambrus.rdforward.bridge.paper.AdventureTranslator;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Fixture Paper listener — handles {@link AsyncChatEvent} and records what
 * it saw. Tests check {@link TestPaperPlugin#PROP_CHAT} for the sender
 * name and {@link TestPaperPlugin#PROP_ADVENTURE_PLAIN} for the Component's
 * legacy serialisation.
 */
public class TestPaperListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        System.setProperty(TestPaperPlugin.PROP_CHAT, event.getPlayer().getName());
        System.setProperty(TestPaperPlugin.PROP_ADVENTURE_PLAIN,
                AdventureTranslator.toPlainText(event.message()));
    }
}

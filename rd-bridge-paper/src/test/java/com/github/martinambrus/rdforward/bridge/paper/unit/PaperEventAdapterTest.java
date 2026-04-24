package com.github.martinambrus.rdforward.bridge.paper.unit;

import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter;
import com.github.martinambrus.rdforward.bridge.paper.PaperEventAdapter;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaperEventAdapterTest {

    @BeforeEach
    void clearBefore() {
        ServerEvents.clearAll();
        BukkitEventAdapter.resetWarnedPlugins();
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        BukkitEventAdapter.resetWarnedPlugins();
    }

    static final class ChatListener implements Listener {
        final AtomicReference<String> last = new AtomicReference<>();

        @EventHandler(priority = org.bukkit.event.EventPriority.HIGH)
        public void onChat(AsyncChatEvent e) {
            last.set(e.message().content());
        }
    }

    @Test
    void registerPaperOnlyBindsAsyncChatEvent() {
        PaperEventAdapter.registerPaperOnly(new ChatListener(), "paper-plugin");
        assertEquals(1, ServerEvents.CHAT.getListenerInfo().size());
        assertEquals(EventPriority.HIGH, ServerEvents.CHAT.getListenerInfo().get(0).priority());
    }

    @Test
    void firedChatReachesPaperListenerAndReceivesPlainContent() {
        ChatListener l = new ChatListener();
        PaperEventAdapter.registerPaperOnly(l, "paper-plugin");

        ServerEvents.CHAT.invoker().onChat("alice", "hello paper");
        assertEquals("hello paper", l.last.get());
    }

    @Test
    void cancelledAsyncChatEventBubblesBack() {
        class CancellingListener implements Listener {
            @EventHandler
            public void onChat(AsyncChatEvent e) { e.setCancelled(true); }
        }
        PaperEventAdapter.registerPaperOnly(new CancellingListener(), "paper-plugin");
        EventResult r = ServerEvents.CHAT.invoker().onChat("alice", "blocked");
        assertEquals(EventResult.CANCEL, r);
    }

    @Test
    void registerAlsoCallsBukkitAdapterPath() {
        class HybridListener implements Listener {
            @EventHandler(ignoreCancelled = true) public void onBreak(BlockBreakEvent e) {}
            @EventHandler public void onChat(AsyncChatEvent e) {}
        }
        PaperEventAdapter.register(new HybridListener(), "paper-plugin");
        assertEquals(1, ServerEvents.BLOCK_BREAK.getListenerInfo().size(),
                "Bukkit-side @EventHandler must also be wired through the Paper adapter");
        assertEquals(1, ServerEvents.CHAT.getListenerInfo().size(),
                "Paper-only AsyncChatEvent must also be wired");
    }

    @Test
    void nonPaperEventHandlersAreIgnoredByRegisterPaperOnly() {
        class OnlyBukkit implements Listener {
            @EventHandler(ignoreCancelled = true) public void onBreak(BlockBreakEvent e) {}
        }
        PaperEventAdapter.registerPaperOnly(new OnlyBukkit(), "paper-plugin");
        assertEquals(0, ServerEvents.CHAT.getListenerInfo().size());
        assertEquals(0, ServerEvents.BLOCK_BREAK.getListenerInfo().size(),
                "registerPaperOnly must not touch the Bukkit path");
    }

    @Test
    void priorityMappingCoversAllEnumValues() {
        class MultiPrio implements Listener {
            @EventHandler(priority = org.bukkit.event.EventPriority.LOWEST) public void a(AsyncChatEvent e) {}
        }
        PaperEventAdapter.registerPaperOnly(new MultiPrio(), "paper-plugin");
        assertEquals(EventPriority.LOWEST, ServerEvents.CHAT.getListenerInfo().get(0).priority());

        ServerEvents.clearAll();
        class MonPrio implements Listener {
            @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR) public void m(AsyncChatEvent e) {}
        }
        PaperEventAdapter.registerPaperOnly(new MonPrio(), "paper-plugin");
        assertEquals(EventPriority.MONITOR, ServerEvents.CHAT.getListenerInfo().get(0).priority());
    }
}

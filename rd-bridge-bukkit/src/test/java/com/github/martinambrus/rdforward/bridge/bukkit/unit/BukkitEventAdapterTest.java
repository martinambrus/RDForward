package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.ListenerInfo;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BukkitEventAdapterTest {

    @BeforeEach
    void clear() {
        ServerEvents.clearAll();
        BukkitEventAdapter.resetWarnedPlugins();
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        BukkitEventAdapter.resetWarnedPlugins();
    }

    static final class AllEventsListener implements Listener {
        volatile int breakHits;
        volatile int placeHits;
        volatile int chatHits;
        volatile int joinHits;
        volatile int quitHits;
        volatile int moveHits;

        @EventHandler(priority = org.bukkit.event.EventPriority.LOW, ignoreCancelled = true)
        public void onBreak(BlockBreakEvent e) { breakHits++; }

        @EventHandler(priority = org.bukkit.event.EventPriority.HIGH, ignoreCancelled = true)
        public void onPlace(BlockPlaceEvent e) { placeHits++; }

        @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
        public void onChat(AsyncPlayerChatEvent e) { chatHits++; }

        @EventHandler
        public void onJoin(PlayerJoinEvent e) { joinHits++; }

        @EventHandler
        public void onQuit(PlayerQuitEvent e) { quitHits++; }

        @EventHandler(ignoreCancelled = true)
        public void onMove(PlayerMoveEvent e) { moveHits++; }

        public void notAHandler(BlockBreakEvent e) { /* no @EventHandler -> ignored */ }

        @EventHandler
        public void wrongArity() { /* 0 params -> ignored */ }

        @EventHandler
        public void wrongArity(BlockBreakEvent a, BlockBreakEvent b) { /* 2 params -> ignored */ }
    }

    @Test
    void registersEachSupportedEventTypeAtMappedPriority() {
        AllEventsListener listener = new AllEventsListener();
        BukkitEventAdapter.register(listener, "test-plugin");

        assertEquals(1, ServerEvents.BLOCK_BREAK.getListenerInfo().size());
        assertEquals(1, ServerEvents.BLOCK_PLACE.getListenerInfo().size());
        assertEquals(1, ServerEvents.CHAT.getListenerInfo().size());
        assertEquals(1, ServerEvents.PLAYER_JOIN.listenerCount());
        assertEquals(1, ServerEvents.PLAYER_LEAVE.listenerCount());
        assertEquals(1, ServerEvents.PLAYER_MOVE.listenerCount());

        assertEquals(EventPriority.LOW, ServerEvents.BLOCK_BREAK.getListenerInfo().get(0).priority());
        assertEquals(EventPriority.HIGH, ServerEvents.BLOCK_PLACE.getListenerInfo().get(0).priority());
        assertEquals(EventPriority.MONITOR, ServerEvents.CHAT.getListenerInfo().get(0).priority());
    }

    @Test
    void firesRegisteredCallback() {
        AllEventsListener listener = new AllEventsListener();
        BukkitEventAdapter.register(listener, "test-plugin");

        ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("alice", 1, 2, 3, 1);
        ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("bob", 4, 5, 6, 2);
        ServerEvents.CHAT.invoker().onChat("carol", "hi");
        ServerEvents.PLAYER_JOIN.invoker().onPlayerJoin("dave", null);
        ServerEvents.PLAYER_LEAVE.invoker().onPlayerLeave("eve");
        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove("frank", (short) 0, (short) 0, (short) 0, (byte) 0, (byte) 0);

        assertEquals(1, listener.breakHits);
        assertEquals(1, listener.placeHits);
        assertEquals(1, listener.chatHits);
        assertEquals(1, listener.joinHits);
        assertEquals(1, listener.quitHits);
        assertEquals(1, listener.moveHits);
    }

    @Test
    void methodsWithoutEventHandlerAreSkipped() {
        class OnlyUnannotated implements Listener {
            int calls;
            public void onBreak(BlockBreakEvent e) { calls++; }
        }
        BukkitEventAdapter.register(new OnlyUnannotated(), "test");
        assertEquals(0, ServerEvents.BLOCK_BREAK.getListenerInfo().size());
    }

    @Test
    void methodsWithWrongArityAreSkipped() {
        class BadArity implements Listener {
            @EventHandler public void zeroArg() {}
            @EventHandler public void twoArg(BlockBreakEvent a, BlockBreakEvent b) {}
        }
        BukkitEventAdapter.register(new BadArity(), "test");
        assertEquals(0, ServerEvents.BLOCK_BREAK.getListenerInfo().size());
    }

    static final class WithoutIgnoreCancelled implements Listener {
        @EventHandler
        public void onBreak(BlockBreakEvent e) {}
    }

    @Test
    void ignoreCancelledWarningFiresOncePerPlugin() {
        Logger log = Logger.getLogger("RDForward/BukkitBridge");
        AtomicInteger warningCount = new AtomicInteger();
        Handler h = new Handler() {
            @Override public void publish(LogRecord r) { warningCount.incrementAndGet(); }
            @Override public void flush() {}
            @Override public void close() {}
        };
        log.addHandler(h);
        try {
            BukkitEventAdapter.register(new WithoutIgnoreCancelled(), "plugin-A");
            BukkitEventAdapter.register(new WithoutIgnoreCancelled(), "plugin-A");
            BukkitEventAdapter.register(new WithoutIgnoreCancelled(), "plugin-B");
            assertEquals(2, warningCount.get(), "one warning per plugin name");
        } finally {
            log.removeHandler(h);
        }
    }

    @Test
    void monitorPriorityDoesNotTriggerIgnoreCancelledWarning() {
        class MonitorListener implements Listener {
            @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
            public void onBreak(BlockBreakEvent e) {}
        }
        Logger log = Logger.getLogger("RDForward/BukkitBridge");
        AtomicInteger warningCount = new AtomicInteger();
        Handler h = new Handler() {
            @Override public void publish(LogRecord r) { warningCount.incrementAndGet(); }
            @Override public void flush() {}
            @Override public void close() {}
        };
        log.addHandler(h);
        try {
            BukkitEventAdapter.register(new MonitorListener(), "mon-plugin");
            assertEquals(0, warningCount.get(), "MONITOR is exempt from the ignoreCancelled warning");
        } finally {
            log.removeHandler(h);
        }
    }

    @Test
    void cancellationBubblesBackAsCancelResult() {
        class CancellingListener implements Listener {
            @EventHandler(ignoreCancelled = true)
            public void onBreak(BlockBreakEvent e) { e.setCancelled(true); }
        }
        BukkitEventAdapter.register(new CancellingListener(), "cancel-plugin");
        EventResult r = ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("x", 0, 0, 0, 1);
        assertEquals(EventResult.CANCEL, r);
    }

    @Test
    void nonCancellingListenerReturnsPass() {
        class PassiveListener implements Listener {
            @EventHandler(ignoreCancelled = true)
            public void onBreak(BlockBreakEvent e) {}
        }
        BukkitEventAdapter.register(new PassiveListener(), "pass-plugin");
        EventResult r = ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("x", 0, 0, 0, 1);
        assertEquals(EventResult.PASS, r);
    }

    @Test
    void nullPluginNameAllowedForTestHarness() {
        AllEventsListener listener = new AllEventsListener();
        BukkitEventAdapter.register(listener);
        assertEquals(1, ServerEvents.BLOCK_BREAK.getListenerInfo().size());
        ListenerInfo info = ServerEvents.BLOCK_BREAK.getListenerInfo().get(0);
        assertNotNull(info.listenerClass());
    }
}

package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.api.event.server.ServerListPingHook;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bridge-level wiring check for SERVER_LIST_PING. Confirms that:
 * <ul>
 *   <li>A plugin that registers a {@link ServerListPingEvent} handler
 *       triggers the lazy {@code ensureServerListPingInstalled} install.</li>
 *   <li>The bridge wraps the rd-api {@link ServerListPingHook.PingContext}
 *       in a Bukkit event whose iterator is backed by the SAME mutable
 *       player-name list — so {@code Iterator.remove()} drops the name
 *       from {@code ctx.playerNames}, mirroring VanishNoPacket's idiom.</li>
 *   <li>{@code setMotd}/{@code setMaxPlayers} land back in the context.</li>
 * </ul>
 */
class ServerListPingWiringTest {

    @BeforeEach
    void clear() {
        ServerEvents.clearAll();
        BukkitEventAdapter.clearAll();
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        BukkitEventAdapter.clearAll();
    }

    static final class VanishStyleListener implements Listener {
        @EventHandler
        public void onPing(ServerListPingEvent e) {
            // Drop the second name (simulates a vanished player).
            Iterator<org.bukkit.entity.Player> it = e.iterator();
            int idx = 0;
            while (it.hasNext()) {
                it.next();
                if (idx == 1) it.remove();
                idx++;
            }
            e.setMotd("rewritten by vanish");
            e.setMaxPlayers(50);
        }
    }

    @Test
    void pluginCanDropPlayerNamesViaIterator() {
        BukkitEventAdapter.register(new VanishStyleListener(), "vanish");

        List<String> names = new ArrayList<>(Arrays.asList("Alice", "Hidden", "Bob"));
        ServerListPingHook.PingContext ctx =
                new ServerListPingHook.PingContext(null, names, 20, "default motd");

        ServerEvents.SERVER_LIST_PING.invoker().onPing(ctx);

        assertEquals(2, ctx.playerNames.size(),
                "Iterator.remove() must propagate back to ctx.playerNames");
        assertTrue(ctx.playerNames.contains("Alice"));
        assertFalse(ctx.playerNames.contains("Hidden"));
        assertTrue(ctx.playerNames.contains("Bob"));
        assertEquals("rewritten by vanish", ctx.motd);
        assertEquals(50, ctx.maxPlayers);
    }

    static final class CountObservingListener implements Listener {
        int observed;
        @EventHandler
        public void onPing(ServerListPingEvent e) {
            observed = e.getNumPlayers();
        }
    }

    @Test
    void getNumPlayersReflectsLiveListSize() {
        CountObservingListener l = new CountObservingListener();
        BukkitEventAdapter.register(l, "counter");

        List<String> names = new ArrayList<>(Arrays.asList("A", "B", "C"));
        ServerListPingHook.PingContext ctx =
                new ServerListPingHook.PingContext(null, names, 100, "");
        ServerEvents.SERVER_LIST_PING.invoker().onPing(ctx);
        assertEquals(3, l.observed);
    }

    static final class NoOpListener implements Listener {
        @EventHandler
        public void onPing(ServerListPingEvent e) {
            // Touch nothing — defaults must pass through untouched.
        }
    }

    @Test
    void noOpListenerLeavesContextIntact() {
        BukkitEventAdapter.register(new NoOpListener(), "noop");
        List<String> names = new ArrayList<>(Arrays.asList("X", "Y"));
        ServerListPingHook.PingContext ctx =
                new ServerListPingHook.PingContext(null, names, 7, "kept");
        ServerEvents.SERVER_LIST_PING.invoker().onPing(ctx);
        assertEquals(2, ctx.playerNames.size());
        assertEquals(7, ctx.maxPlayers);
        assertEquals("kept", ctx.motd);
    }

    @Test
    void listenerNotInstalledUntilPluginRegisters() {
        // No plugin registered yet — invoking SERVER_LIST_PING should be a no-op.
        List<String> names = new ArrayList<>(Arrays.asList("A"));
        ServerListPingHook.PingContext ctx =
                new ServerListPingHook.PingContext(null, names, 20, "raw");
        ServerEvents.SERVER_LIST_PING.invoker().onPing(ctx);
        assertEquals("raw", ctx.motd);
        assertEquals(1, ctx.playerNames.size());
    }
}

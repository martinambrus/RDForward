package com.github.martinambrus.rdforward.api.event.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the mutate-in-place semantics of
 * {@link ServerEvents#SERVER_LIST_PING}: every listener sees the same
 * {@link ServerListPingHook.PingContext}, removals from
 * {@code ctx.playerNames} drop the player from the displayed count,
 * and writes to {@code ctx.maxPlayers}/{@code ctx.motd} land back in
 * the host's response. The Bukkit bridge depends on this contract so
 * VanishNoPacket-style iterator-removal works end-to-end.
 */
class ServerListPingHookTest {

    @BeforeEach
    void clear() {
        ServerEvents.SERVER_LIST_PING.clearListeners();
    }

    @AfterEach
    void cleanup() {
        ServerEvents.SERVER_LIST_PING.clearListeners();
    }

    @Test
    void noListenersLeavesContextUntouched() {
        List<String> names = new ArrayList<>(Arrays.asList("Alice", "Bob"));
        ServerListPingHook.PingContext ctx =
                new ServerListPingHook.PingContext(null, names, 20, "default motd");
        ServerEvents.SERVER_LIST_PING.invoker().onPing(ctx);
        assertEquals(2, ctx.playerNames.size());
        assertEquals(20, ctx.maxPlayers);
        assertEquals("default motd", ctx.motd);
    }

    @Test
    void listenerSeesSameMutableList() {
        List<String> names = new ArrayList<>(Arrays.asList("Alice", "Bob"));
        ServerListPingHook.PingContext ctx =
                new ServerListPingHook.PingContext(null, names, 20, "motd");
        ServerEvents.SERVER_LIST_PING.register(received -> {
            assertSame(names, received.playerNames,
                    "Listener must receive the same list instance the host owns");
        });
        ServerEvents.SERVER_LIST_PING.invoker().onPing(ctx);
    }

    @Test
    void removeFromPlayerNamesDropsCount() {
        List<String> names = new ArrayList<>(Arrays.asList("Visible", "Hidden", "Visible2"));
        ServerListPingHook.PingContext ctx =
                new ServerListPingHook.PingContext(null, names, 20, "motd");
        ServerEvents.SERVER_LIST_PING.register(received ->
                received.playerNames.removeIf(n -> n.equals("Hidden")));
        ServerEvents.SERVER_LIST_PING.invoker().onPing(ctx);
        assertEquals(2, ctx.playerNames.size());
        assertEquals(2, names.size(), "Mutations must propagate to the host's list reference");
        assertTrue(ctx.playerNames.contains("Visible"));
        assertTrue(ctx.playerNames.contains("Visible2"));
    }

    @Test
    void multipleListenersAccumulateMutations() {
        List<String> names = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        ServerListPingHook.PingContext ctx =
                new ServerListPingHook.PingContext(null, names, 10, "first");
        ServerEvents.SERVER_LIST_PING.register(c -> {
            c.motd = "second";
            c.playerNames.remove("A");
        });
        ServerEvents.SERVER_LIST_PING.register(c -> {
            c.maxPlayers = 99;
            c.playerNames.remove("B");
        });
        ServerEvents.SERVER_LIST_PING.invoker().onPing(ctx);
        assertEquals("second", ctx.motd);
        assertEquals(99, ctx.maxPlayers);
        assertEquals(2, ctx.playerNames.size());
        assertTrue(ctx.playerNames.contains("C"));
        assertTrue(ctx.playerNames.contains("D"));
    }

    @Test
    void contextExposesAddressForListenerInspection() throws Exception {
        InetAddress addr = InetAddress.getByName("127.0.0.1");
        ServerListPingHook.PingContext ctx =
                new ServerListPingHook.PingContext(addr, new ArrayList<>(), 0, "");
        ServerEvents.SERVER_LIST_PING.register(received ->
                assertNotNull(received.address));
        ServerEvents.SERVER_LIST_PING.invoker().onPing(ctx);
        assertSame(addr, ctx.address);
    }

    @Test
    void clearAllResetsPingListeners() {
        ServerEvents.SERVER_LIST_PING.register(c -> c.maxPlayers = 1);
        ServerEvents.clearAll();
        ServerListPingHook.PingContext ctx =
                new ServerListPingHook.PingContext(null, new ArrayList<>(), 50, "");
        ServerEvents.SERVER_LIST_PING.invoker().onPing(ctx);
        assertEquals(50, ctx.maxPlayers, "clearAll must drop the registered listener");
    }
}

package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.ChatContext;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.api.world.Location;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The bridge populates a chat event's {@code getRecipients()} with the
 * live online roster, fires the listener, then diffs the surviving
 * recipients against the original to drive
 * {@link ChatContext#excluded()}. Server-side connection handlers read
 * that exclusion + the (possibly rewritten) message and pass them to
 * {@code PlayerManager.broadcastChat}. This test pins the diff/rewrite
 * contract so Essentials's {@code /ignore} pipeline keeps working.
 *
 * <p>Uses the production {@link BukkitBridge#install} path so
 * {@code Bukkit.getServer().getOnlinePlayers()} returns real bridge
 * proxies wrapped around {@link StubRdServer.StubRdPlayer} backings —
 * mirroring what bindChat sees at runtime.
 */
class BukkitChatRecipientExclusionTest {

    private StubRdServer rd;

    @BeforeEach
    void setup() {
        ServerEvents.clearAll();
        BukkitEventAdapter.clearAll();
        BukkitBridge.uninstall();
        rd = new StubRdServer();
        BukkitBridge.install(rd);
    }

    @AfterEach
    void teardown() {
        ServerEvents.clearAll();
        BukkitEventAdapter.clearAll();
        BukkitBridge.uninstall();
        ChatContext stale = ChatContext.current();
        if (stale != null) stale.close();
    }

    @Test
    void asyncListenerRemovingRecipientPopulatesExclusion() {
        addPlayer("alice");
        addPlayer("bob");

        // Listener removes 'bob' the way Essentials's /ignore does.
        BukkitEventAdapter.register(new Listener() {
            @EventHandler
            public void onChat(AsyncPlayerChatEvent ev) {
                ev.getRecipients().removeIf(p -> "bob".equals(p.getName()));
            }
        }, "ignore-stub");

        try (ChatContext ctx = ChatContext.begin("hello")) {
            EventResult r = ServerEvents.CHAT.invoker().onChat("alice", "hello");
            assertEquals(EventResult.PASS, r);
            assertTrue(ctx.excluded().contains("bob"),
                    "removed recipient must surface as excluded name");
            assertTrue(!ctx.excluded().contains("alice"),
                    "surviving recipient must not be excluded");
        }
    }

    @Test
    void legacyListenerRemovingRecipientPopulatesExclusion() {
        addPlayer("alice");
        addPlayer("carol");

        // Essentials 2.8.x mute/ignore listens to the LEGACY event,
        // not AsyncPlayerChatEvent — verify both shapes route through.
        BukkitEventAdapter.register(new Listener() {
            @EventHandler
            public void onChat(PlayerChatEvent ev) {
                ev.getRecipients().removeIf(p -> "carol".equals(p.getName()));
            }
        }, "legacy-ignore-stub");

        try (ChatContext ctx = ChatContext.begin("yo")) {
            ServerEvents.CHAT.invoker().onChat("alice", "yo");
            assertTrue(ctx.excluded().contains("carol"));
        }
    }

    @Test
    void messageRewriteWritesIntoChatContext() {
        addPlayer("alice");

        BukkitEventAdapter.register(new Listener() {
            @EventHandler
            public void onChat(AsyncPlayerChatEvent ev) {
                ev.setMessage("[censored]");
            }
        }, "rewrite-stub");

        try (ChatContext ctx = ChatContext.begin("orig")) {
            ServerEvents.CHAT.invoker().onChat("alice", "orig");
            assertEquals("[censored]", ctx.message(),
                    "setMessage in listener must propagate to ChatContext for downstream broadcast");
        }
    }

    @Test
    void cancelStillReturnsCancelResult() {
        addPlayer("alice");

        BukkitEventAdapter.register(new Listener() {
            @EventHandler
            public void onChat(AsyncPlayerChatEvent ev) { ev.setCancelled(true); }
        }, "cancel-stub");

        try (ChatContext ctx = ChatContext.begin("muted")) {
            EventResult r = ServerEvents.CHAT.invoker().onChat("alice", "muted");
            assertEquals(EventResult.CANCEL, r);
        }
    }

    @Test
    void worksWithoutChatContextOutsideServerCallSite() {
        // Tests / unit harnesses that fire CHAT directly without
        // wrapping in ChatContext.begin must still link cleanly — the
        // bridge writes only when a context is current.
        addPlayer("alice");

        BukkitEventAdapter.register(new Listener() {
            @EventHandler
            public void onChat(AsyncPlayerChatEvent ev) { ev.getRecipients().clear(); }
        }, "no-ctx-stub");

        ServerEvents.CHAT.invoker().onChat("alice", "anything");
        // Reaches here without NPE — the assertion is the absence of failure.
    }

    private Player addPlayer(String name) {
        Location loc = new Location("stub", 0, 0, 0, 0f, 0f);
        StubRdServer.StubRdPlayer backing = new StubRdServer.StubRdPlayer(name, loc);
        rd.players.put(name, backing);
        // Return the bridge-wrapped Bukkit Player so callers can compare.
        return Bukkit.getServer().getPlayer(name);
    }
}

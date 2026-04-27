package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.api.event.server.ServerListPingHook;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.api.ServerProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link PlayerManager#firePingHook} seeds the
 * {@link ServerListPingHook.PingContext} from current online players +
 * server properties and that listener mutations propagate. The four
 * ping/banner sites (Netty SLP, Bedrock pong, MCPE pong, EaglerCraft
 * query) call this and read the post-mutation values, so listener
 * removals from {@code playerNames} drop vanished players from every
 * pong format simultaneously.
 */
class PlayerManagerPingHookTest {

    @BeforeEach
    void setup() {
        ServerEvents.SERVER_LIST_PING.clearListeners();
        ServerProperties.set("max-players", "100");
        ServerProperties.set("motd", "Test MOTD");
        PlayerManager.setMaxPlayers(100);
    }

    @AfterEach
    void teardown() {
        ServerEvents.SERVER_LIST_PING.clearListeners();
    }

    @Test
    void contextSeedsFromOnlinePlayers() {
        PlayerManager pm = new PlayerManager();
        pm.addPlayer("Alice", null, null, ProtocolVersion.BEDROCK);
        pm.addPlayer("Bob", null, null, ProtocolVersion.BEDROCK);

        ServerListPingHook.PingContext ctx = pm.firePingHook(null);
        assertEquals(2, ctx.playerNames.size());
        assertTrue(ctx.playerNames.contains("Alice"));
        assertTrue(ctx.playerNames.contains("Bob"));
        assertEquals(100, ctx.maxPlayers);
        assertEquals("Test MOTD", ctx.motd);
    }

    @Test
    void emptyServerYieldsEmptyNamesList() {
        PlayerManager pm = new PlayerManager();
        ServerListPingHook.PingContext ctx = pm.firePingHook(null);
        assertEquals(0, ctx.playerNames.size());
        assertEquals(100, ctx.maxPlayers);
    }

    @Test
    void listenerRemovalDropsFromContext() {
        PlayerManager pm = new PlayerManager();
        pm.addPlayer("Visible", null, null, ProtocolVersion.BEDROCK);
        pm.addPlayer("Hidden", null, null, ProtocolVersion.BEDROCK);

        ServerEvents.SERVER_LIST_PING.register(c ->
                c.playerNames.removeIf(n -> n.equals("Hidden")));

        ServerListPingHook.PingContext ctx = pm.firePingHook(null);
        assertEquals(1, ctx.playerNames.size());
        assertTrue(ctx.playerNames.contains("Visible"));
    }

    @Test
    void listenerCanRewriteMotdAndMax() {
        PlayerManager pm = new PlayerManager();
        ServerEvents.SERVER_LIST_PING.register(c -> {
            c.motd = "rewritten";
            c.maxPlayers = 7;
        });
        ServerListPingHook.PingContext ctx = pm.firePingHook(null);
        assertEquals("rewritten", ctx.motd);
        assertEquals(7, ctx.maxPlayers);
    }
}

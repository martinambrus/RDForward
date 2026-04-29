package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Essentials's {@code /s} (sudo) target lookup goes through
 * {@code Server.getPlayerExact(String)}. Without it the dispatch
 * NoSuchMethodErrors on every invocation. Real Bukkit's
 * {@code getPlayer(String)} is a fuzzy prefix match while
 * {@code getPlayerExact} requires an exact name; rd-api's
 * {@code Server.getPlayer(String)} is already exact-match by name,
 * so the default delegates verbatim.
 */
class ServerGetPlayerExactTest {

    private static final Player ALICE = (Player) java.lang.reflect.Proxy.newProxyInstance(
            ServerGetPlayerExactTest.class.getClassLoader(),
            new Class<?>[]{Player.class},
            (p, m, a) -> "getName".equals(m.getName()) ? "Alice" : null);

    private static final class CountingServer implements Server {
        int getPlayerCalls;
        String lastName;
        @Override public Player getPlayer(String name) {
            getPlayerCalls++;
            lastName = name;
            return "Alice".equals(name) ? ALICE : null;
        }
        @Override public Collection<Player> getOnlinePlayers() { return List.of(ALICE); }
        @Override public String getName() { return "test"; }
        @Override public String getVersion() { return "test"; }
        @Override public String getBukkitVersion() { return "test"; }
        @Override public java.util.logging.Logger getLogger() { return java.util.logging.Logger.getLogger("test"); }
        @Override public int broadcastMessage(String message) { return 0; }
        @Override public org.bukkit.plugin.PluginManager getPluginManager() { return null; }
        @Override public org.bukkit.scheduler.BukkitScheduler getScheduler() { return null; }
        @Override public org.bukkit.command.ConsoleCommandSender getConsoleSender() { return null; }
        @Override public List<org.bukkit.World> getWorlds() { return List.of(); }
        @Override public org.bukkit.World getWorld(String name) { return null; }
    }

    @Test
    void getPlayerExactDelegatesToGetPlayer() {
        CountingServer server = new CountingServer();
        Player p = server.getPlayerExact("Alice");
        assertSame(ALICE, p);
        assertSame("Alice", server.lastName);
        assertNotNull(p, "exact-match lookup must return the online player");
    }

    @Test
    void getPlayerExactReturnsNullForUnknown() {
        CountingServer server = new CountingServer();
        assertNull(server.getPlayerExact("Bob"));
    }

    @Test
    void getPlayerExactInterfaceShapePresent() {
        // Essentials calls this via virtual dispatch on the Server
        // interface; the method must be declared on the interface so
        // bytecode-level invocations resolve without NoSuchMethodError.
        boolean present = java.util.Arrays.stream(Server.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("getPlayerExact")
                        && m.getParameterCount() == 1
                        && m.getParameterTypes()[0] == String.class);
        assertNotNull(present ? Boolean.TRUE : null,
                "Server.getPlayerExact(String) must be declared on the interface");
    }
}

package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the ASM-injected legacy bridge on {@link Server}: the build
 * adds a {@code Player[] getOnlinePlayers()} default method alongside
 * the source-declared {@code Collection<Player> getOnlinePlayers()} so
 * old Bukkit plugins (VanishNoPacket 3.15's
 * {@code invokeinterface Server.getOnlinePlayers()[Lorg/bukkit/entity/Player;})
 * resolve without {@link NoSuchMethodError}, while modern plugins keep
 * working against the Collection variant.
 *
 * <p>Both reflective shape (two methods, different return types) and
 * runtime behaviour (legacy method delegates to the modern one and
 * returns an array containing the same players) are checked.
 */
class LegacyServerBridgeTest {

    private static final Player ALICE = (Player) java.lang.reflect.Proxy.newProxyInstance(
            LegacyServerBridgeTest.class.getClassLoader(),
            new Class<?>[]{Player.class},
            (p, m, a) -> "getName".equals(m.getName()) ? "Alice" : null);
    private static final Player BOB = (Player) java.lang.reflect.Proxy.newProxyInstance(
            LegacyServerBridgeTest.class.getClassLoader(),
            new Class<?>[]{Player.class},
            (p, m, a) -> "getName".equals(m.getName()) ? "Bob" : null);

    @Test
    void serverDeclaresBothLegacyAndModernGetOnlinePlayers() {
        long count = Arrays.stream(Server.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("getOnlinePlayers"))
                .count();
        assertEquals(2, count, "expected legacy Player[] + modern Collection variants");

        Method legacy = Arrays.stream(Server.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("getOnlinePlayers"))
                .filter(m -> m.getReturnType().isArray())
                .findFirst().orElseThrow();
        assertEquals(Player[].class, legacy.getReturnType());
        assertTrue(legacy.isDefault(),
                "legacy bridge must be a default method so subclasses inherit it");

        Method modern = Arrays.stream(Server.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("getOnlinePlayers"))
                .filter(m -> Collection.class.isAssignableFrom(m.getReturnType()))
                .findFirst().orElseThrow();
        assertEquals(Collection.class, modern.getReturnType());
    }

    /** Minimum-viable Server impl that only honours
     *  {@code getOnlinePlayers()} — the legacy bridge default method
     *  should call this and pack the result into a {@code Player[]}. */
    private static final class CollectionOnlyServer implements Server {
        private final List<Player> roster;
        CollectionOnlyServer(List<Player> roster) { this.roster = roster; }
        @Override public Collection<Player> getOnlinePlayers() { return roster; }
        @Override public String getName() { return "test"; }
        @Override public String getVersion() { return "test"; }
        @Override public String getBukkitVersion() { return "test"; }
        @Override public java.util.logging.Logger getLogger() { return java.util.logging.Logger.getLogger("test"); }
        @Override public int broadcastMessage(String message) { return 0; }
        @Override public org.bukkit.plugin.PluginManager getPluginManager() { return null; }
        @Override public org.bukkit.scheduler.BukkitScheduler getScheduler() { return null; }
        @Override public org.bukkit.command.ConsoleCommandSender getConsoleSender() { return null; }
        @Override public Player getPlayer(String name) { return null; }
        @Override public List<org.bukkit.World> getWorlds() { return List.of(); }
        @Override public org.bukkit.World getWorld(String name) { return null; }
    }

    @Test
    void legacyBridgeReturnsPlayerArrayContainingAllOnline() {
        CollectionOnlyServer server = new CollectionOnlyServer(List.of(ALICE, BOB));

        // Resolve via reflection so the test is descriptor-aware: the
        // Java compiler binds calls to the modern Collection variant.
        Method legacy = Arrays.stream(Server.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("getOnlinePlayers"))
                .filter(m -> m.getReturnType().isArray())
                .findFirst().orElseThrow();

        Object out;
        try {
            out = legacy.invoke(server);
        } catch (Exception e) {
            throw new AssertionError("invoking legacy bridge", e);
        }
        assertNotNull(out);
        assertTrue(out instanceof Player[]);
        Player[] arr = (Player[]) out;
        assertEquals(2, arr.length);
        assertSame(ALICE, arr[0]);
        assertSame(BOB, arr[1]);
    }

    @Test
    void legacyBridgeReturnsEmptyArrayForEmptyRoster() throws Exception {
        CollectionOnlyServer server = new CollectionOnlyServer(List.of());
        Method legacy = Arrays.stream(Server.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("getOnlinePlayers"))
                .filter(m -> m.getReturnType().isArray())
                .findFirst().orElseThrow();
        Player[] arr = (Player[]) legacy.invoke(server);
        assertEquals(0, arr.length);
    }
}

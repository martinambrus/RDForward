package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Essentials's {@code /sudo} dispatches forced commands through
 * {@code Server.getPluginCommand(String)}, which delegates to
 * {@link BukkitBridge#findPluginCommand(String)}. The bridge scans
 * every {@link BukkitBridge#allPlugins()} entry for a matching command
 * name or alias. Without it, /sudo NoSuchMethodErrors before dispatch.
 *
 * <p>The lookup helper is exercised directly here rather than through
 * the {@code Server} interface — at runtime the legacy-bridge-patched
 * {@code org/bukkit/Server.class} carries a covariant
 * {@code Player[] getOnlinePlayers()} bridge alongside the modern
 * {@code Collection<Player>}, which JDK Proxy refuses to generate over.
 * The default {@code getPluginCommand} forwards verbatim so behaviour
 * is identical end-to-end.
 */
class ServerGetPluginCommandTest {

    private static final class FixturePlugin extends JavaPlugin {}

    private FixturePlugin alpha;
    private FixturePlugin beta;

    @BeforeEach
    void setup() {
        alpha = new FixturePlugin();
        beta = new FixturePlugin();

        Map<String, PluginCommand> alphaCommands = new LinkedHashMap<>();
        PluginCommand fly = new PluginCommand("fly");
        fly.setAliases(List.of("flight"));
        alphaCommands.put("fly", fly);
        alpha.setRDPluginCommands(alphaCommands);

        Map<String, PluginCommand> betaCommands = new LinkedHashMap<>();
        PluginCommand msg = new PluginCommand("msg");
        msg.setAliases(List.of("tell", "w"));
        betaCommands.put("msg", msg);
        beta.setRDPluginCommands(betaCommands);

        BukkitBridge.registerPlugin("alpha", alpha);
        BukkitBridge.registerPlugin("beta", beta);
    }

    @AfterEach
    void teardown() {
        BukkitBridge.unregisterPlugin("alpha");
        BukkitBridge.unregisterPlugin("beta");
    }

    @Test
    void looksUpByDirectName() {
        PluginCommand pc = BukkitBridge.findPluginCommand("fly");
        assertNotNull(pc);
        assertSame("fly", pc.getName());
    }

    @Test
    void caseInsensitiveLookupByName() {
        // Bukkit's command dispatch is case-insensitive — Essentials's
        // sudo can pass /MSG and the lookup must still find /msg.
        assertNotNull(BukkitBridge.findPluginCommand("MSG"));
        assertNotNull(BukkitBridge.findPluginCommand("Fly"));
    }

    @Test
    void looksUpByAlias() {
        // 'tell' is an alias for 'msg'. Bukkit's dispatch falls back to
        // alias matching across every registered plugin.
        PluginCommand pc = BukkitBridge.findPluginCommand("tell");
        assertNotNull(pc);
        assertSame("msg", pc.getName());
    }

    @Test
    void aliasLookupIsCaseInsensitive() {
        assertNotNull(BukkitBridge.findPluginCommand("FLIGHT"));
    }

    @Test
    void unknownNameReturnsNull() {
        assertNull(BukkitBridge.findPluginCommand("nonexistent"));
    }

    @Test
    void nullNameReturnsNull() {
        assertNull(BukkitBridge.findPluginCommand(null));
    }

    @Test
    void scansAcrossPlugins() {
        // 'fly' lives in alpha, 'msg' lives in beta — both must resolve
        // via the same call without caller-side disambiguation.
        assertNotNull(BukkitBridge.findPluginCommand("fly"));
        assertNotNull(BukkitBridge.findPluginCommand("msg"));
    }
}

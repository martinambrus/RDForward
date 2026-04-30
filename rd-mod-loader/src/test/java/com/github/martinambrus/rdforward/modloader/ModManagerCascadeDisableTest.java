package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.command.Command;
import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.github.martinambrus.rdforward.api.command.TabCompleter;
import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import com.github.martinambrus.rdforward.api.mod.ModManager;
import com.github.martinambrus.rdforward.api.mod.ServerMod;
import com.github.martinambrus.rdforward.api.network.PluginChannel;
import com.github.martinambrus.rdforward.api.permission.PermissionManager;
import com.github.martinambrus.rdforward.api.player.Player;
import com.github.martinambrus.rdforward.api.registry.RegistryKey;
import com.github.martinambrus.rdforward.api.scheduler.ScheduledTask;
import com.github.martinambrus.rdforward.api.scheduler.Scheduler;
import com.github.martinambrus.rdforward.api.server.Server;
import com.github.martinambrus.rdforward.api.version.ProtocolVersion;
import com.github.martinambrus.rdforward.api.world.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cascade-disable: when a mod's hard-dep ends in any non-{@code ENABLED}
 * state (failed to load, threw on enable, self-disabled via Bukkit
 * bridge's {@code PluginSelfDisabledException}), every mod that hard-deps
 * on it must be skipped with a clean SEVERE log instead of being allowed
 * to crash on the missing peer's API. Mirrors the EssentialsDiscord ->
 * EssentialsDiscordLink pattern: when the upstream fails to register
 * its service, downstream lookups return null and the dependent NPEs
 * inside the bridge.
 */
class ModManagerCascadeDisableTest {

    @Test
    void dependentSkippedWhenHardDepThrowsOnEnable(@TempDir Path tmp) {
        // Three-mod fixture mimicking the real failure shape: a
        // dependency that throws on onEnable, a dependent that should
        // be skipped, and a sibling that does not depend on the
        // failing mod and must enable cleanly.
        AtomicBoolean depEnabled = new AtomicBoolean();
        AtomicBoolean dependentEnabled = new AtomicBoolean();
        AtomicBoolean siblingEnabled = new AtomicBoolean();

        ModContainer dep = container(tmp.resolve("dep.jar"), descriptor("dep", Map.of()),
                throwingMod(depEnabled, "boom"));
        ModContainer dependent = container(tmp.resolve("dependent.jar"),
                descriptor("dependent", Map.of("dep", "*")),
                trackingMod(dependentEnabled));
        ModContainer sibling = container(tmp.resolve("sibling.jar"),
                descriptor("sibling", Map.of()),
                trackingMod(siblingEnabled));

        ModManager mm = newModManager();
        ((com.github.martinambrus.rdforward.modloader.ModManager) mm)
                .setContainers(List.of(dep, dependent, sibling));
        ((com.github.martinambrus.rdforward.modloader.ModManager) mm).enableAll();

        assertEquals(ModState.ERROR, dep.state(),
                "throwing mod must end in ERROR state");
        assertTrue(depEnabled.get(),
                "throwing mod's onEnable was reached (set the flag) before the throw");
        assertEquals(ModState.ERROR, dependent.state(),
                "dependent must inherit failure: dep ERROR -> dependent skipped");
        assertFalse(dependentEnabled.get(),
                "skipped dependent must NOT have onEnable invoked");
        assertNotNull(dependent.lastError(),
                "skipped dependent should carry an explanatory error");
        assertTrue(dependent.lastError().getMessage().contains("dep"),
                "error message must name the failed dep so operators see the chain");

        assertEquals(ModState.ENABLED, sibling.state(),
                "siblings without a broken dep must still enable cleanly");
        assertTrue(siblingEnabled.get());
    }

    @Test
    void dependentSkippedWhenHardDepSelfDisables(@TempDir Path tmp) {
        // Mirrors Bukkit's {@code setEnabled(false)} self-disable shape:
        // the dependency's onEnable returns normally but its container
        // ends up in DISABLED via the bridge's
        // PluginSelfDisabledException path. The cascade must treat this
        // identically to a thrown enable.
        AtomicBoolean dependentEnabled = new AtomicBoolean();

        ModContainer dep = container(tmp.resolve("dep.jar"), descriptor("dep", Map.of()),
                selfDisablingMod());
        ModContainer dependent = container(tmp.resolve("dependent.jar"),
                descriptor("dependent", Map.of("dep", "*")),
                trackingMod(dependentEnabled));

        ModManager mm = newModManager();
        ((com.github.martinambrus.rdforward.modloader.ModManager) mm)
                .setContainers(List.of(dep, dependent));
        ((com.github.martinambrus.rdforward.modloader.ModManager) mm).enableAll();

        assertEquals(ModState.ERROR, dep.state(),
                "self-disable surfaces via PluginSelfDisabledException -> ERROR");
        assertEquals(ModState.ERROR, dependent.state(),
                "dependent must be skipped when dep self-disabled");
        assertFalse(dependentEnabled.get());
    }

    @Test
    void cascadeIsTransitive(@TempDir Path tmp) {
        // A -> B -> C: A throws, B depends on A, C depends on B.
        // Both B and C must be skipped — the cascade walks the chain
        // because by the time C is evaluated, B is already in ERROR.
        AtomicBoolean bEnabled = new AtomicBoolean();
        AtomicBoolean cEnabled = new AtomicBoolean();

        ModContainer a = container(tmp.resolve("a.jar"), descriptor("a", Map.of()),
                throwingMod(new AtomicBoolean(), "a-failed"));
        ModContainer b = container(tmp.resolve("b.jar"), descriptor("b", Map.of("a", "*")),
                trackingMod(bEnabled));
        ModContainer c = container(tmp.resolve("c.jar"), descriptor("c", Map.of("b", "*")),
                trackingMod(cEnabled));

        ModManager mm = newModManager();
        ((com.github.martinambrus.rdforward.modloader.ModManager) mm)
                .setContainers(List.of(a, b, c));
        ((com.github.martinambrus.rdforward.modloader.ModManager) mm).enableAll();

        assertEquals(ModState.ERROR, a.state());
        assertEquals(ModState.ERROR, b.state());
        assertEquals(ModState.ERROR, c.state(),
                "cascade must walk B -> C, not just stop at A's direct dependents");
        assertFalse(bEnabled.get());
        assertFalse(cEnabled.get());
    }

    @Test
    void softDepFailureDoesNotSkipDependent(@TempDir Path tmp) {
        // Soft deps are load-order hints only — when one fails, the
        // dependent must still load. Otherwise listing a plugin in
        // {@code softdepend:} would silently turn it into a hard-dep
        // gate, defeating the point of the two separate fields.
        AtomicBoolean dependentEnabled = new AtomicBoolean();

        ModContainer dep = container(tmp.resolve("dep.jar"), descriptor("dep", Map.of()),
                throwingMod(new AtomicBoolean(), "boom"));
        ModContainer dependent = container(tmp.resolve("dependent.jar"),
                descriptorWithSoftDep("dependent", Map.of("dep", "*")),
                trackingMod(dependentEnabled));

        ModManager mm = newModManager();
        ((com.github.martinambrus.rdforward.modloader.ModManager) mm)
                .setContainers(List.of(dep, dependent));
        ((com.github.martinambrus.rdforward.modloader.ModManager) mm).enableAll();

        assertEquals(ModState.ERROR, dep.state());
        assertEquals(ModState.ENABLED, dependent.state(),
                "soft-dep failure must NOT block dependent enable");
        assertTrue(dependentEnabled.get());
    }

    /* ---------- fixtures ---------- */

    private static com.github.martinambrus.rdforward.modloader.ModManager newModManager() {
        return new com.github.martinambrus.rdforward.modloader.ModManager(new StubServer());
    }

    private static ModDescriptor descriptor(String id, Map<String, String> hardDeps) {
        return new ModDescriptor(
                id, id, "1.0", "",
                List.of(), "*",
                Map.of(),
                hardDeps,
                Map.of(),
                List.of(),
                false, null, null);
    }

    private static ModDescriptor descriptorWithSoftDep(String id, Map<String, String> softDeps) {
        return new ModDescriptor(
                id, id, "1.0", "",
                List.of(), "*",
                Map.of(),
                Map.of(),
                softDeps,
                List.of(),
                false, null, null);
    }

    private static ModContainer container(Path jar, ModDescriptor d, ServerMod sm) {
        ModContainer c = new ModContainer(d, jar);
        c.setServerInstance(sm);
        return c;
    }

    private static ServerMod trackingMod(AtomicBoolean flag) {
        return new ServerMod() {
            @Override public void onEnable(Server server) { flag.set(true); }
            @Override public void onDisable() {}
        };
    }

    private static ServerMod throwingMod(AtomicBoolean flag, String message) {
        return new ServerMod() {
            @Override public void onEnable(Server server) {
                flag.set(true);
                throw new RuntimeException(message);
            }
            @Override public void onDisable() {}
        };
    }

    /** Returns a ServerMod that completes onEnable normally but causes
     *  {@code ModManager.enable} to surface an ERROR state via a thrown
     *  exception modelled on {@code PluginSelfDisabledException} — the
     *  exact path the Bukkit bridge takes when a plugin calls
     *  {@code setEnabled(false)} during its own enable. */
    private static ServerMod selfDisablingMod() {
        return new ServerMod() {
            @Override public void onEnable(Server server) {
                throw new IllegalStateException("self-disabled (test stand-in for PluginSelfDisabledException)");
            }
            @Override public void onDisable() {}
        };
    }

    /* ---------- minimal Server stub ---------- */

    private static final class StubServer implements Server {
        private final StubScheduler scheduler = new StubScheduler();
        private final StubCommandRegistry commands = new StubCommandRegistry();
        @Override public World getWorld() { return null; }
        @Override public Collection<? extends Player> getOnlinePlayers() { return List.of(); }
        @Override public Player getPlayer(String name) { return null; }
        @Override public Scheduler getScheduler() { return scheduler; }
        @Override public CommandRegistry getCommandRegistry() { return commands; }
        @Override public PermissionManager getPermissionManager() { return null; }
        @Override public ModManager getModManager() { return null; }
        @Override public ProtocolVersion[] getSupportedVersions() { return new ProtocolVersion[0]; }
        @Override public void broadcastMessage(String message) {}
        @Override public PluginChannel openPluginChannel(RegistryKey id) { return null; }
    }

    private static final class StubScheduler implements Scheduler {
        @Override public ScheduledTask runLater(String modId, int delayTicks, Runnable task) { return null; }
        @Override public ScheduledTask runRepeating(String modId, int initialDelay, int periodTicks, Runnable task) { return null; }
        @Override public int cancelByOwner(String modId) { return 0; }
    }

    private static final class StubCommandRegistry implements CommandRegistry {
        @Override public void register(String modId, String name, String description, Command handler) {}
        @Override public void registerOp(String modId, String name, String description, int opLevel, Command handler) {}
        @Override public void setTabCompleter(String modId, String name, TabCompleter completer) {}
        @Override public int unregisterByOwner(String modId) { return 0; }
        @Override public boolean exists(String name) { return false; }
        @Override public List<String> listForOpLevel(int opLevel) { return List.of(); }
    }
}

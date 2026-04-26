package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.github.martinambrus.rdforward.api.mod.ModManager;
import com.github.martinambrus.rdforward.api.network.PluginChannel;
import com.github.martinambrus.rdforward.api.permission.PermissionManager;
import com.github.martinambrus.rdforward.api.player.Player;
import com.github.martinambrus.rdforward.api.registry.RegistryKey;
import com.github.martinambrus.rdforward.api.scheduler.ScheduledTask;
import com.github.martinambrus.rdforward.api.scheduler.Scheduler;
import com.github.martinambrus.rdforward.api.server.Server;
import com.github.martinambrus.rdforward.api.version.ProtocolVersion;
import com.github.martinambrus.rdforward.api.world.Block;
import com.github.martinambrus.rdforward.api.world.BlockType;
import com.github.martinambrus.rdforward.api.world.World;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Verifies the production boot path installs the Bukkit bridge before
 * mod {@code onEnable} runs so plugin code that calls
 * {@code Bukkit.getServer().getPluginManager()} resolves to a live
 * facade. Prior to this hook, only test code installed the bridge —
 * SimpleLogin and similar plugins NPE'd when the production server
 * booted them.
 *
 * <p>The reflection-based dispatch in
 * {@link ModSystem#installBridges(Server)} is exercised end-to-end here:
 * the bridge module is on the test runtime classpath (declared via
 * {@code testImplementation project(':rd-bridge-bukkit')}), so the
 * lookup of {@code com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge}
 * succeeds and {@code BukkitBridge.install} runs against a stub server.
 */
class ModSystemBridgeInstallTest {

    @BeforeEach @AfterEach
    void clearBridge() {
        BukkitBridge.uninstall();
    }

    @Test
    void installBridgesPopulatesBukkitFacade() {
        Server stub = new StubServer();
        ModSystem.installBridges(stub);

        org.bukkit.Server installed = org.bukkit.Bukkit.getServer();
        assertNotNull(installed, "Bukkit.getServer() must be non-null after installBridges");
        assertSame(stub, BukkitBridge.currentRdServer(),
                "BukkitBridge should expose the same rd-api Server it was installed with");
    }

    @Test
    void uninstallBridgesClearsBukkitFacade() {
        ModSystem.installBridges(new StubServer());
        ModSystem.uninstallBridges();

        assertNull(org.bukkit.Bukkit.getServer(),
                "Bukkit.getServer() must revert to null after uninstallBridges");
    }

    @Test
    void installBridgesIsIdempotent() {
        Server first = new StubServer();
        Server second = new StubServer();
        ModSystem.installBridges(first);
        ModSystem.installBridges(second);

        assertSame(first, BukkitBridge.currentRdServer(),
                "second install should be a no-op while the first is still active");
    }

    /** Minimal {@link Server} that exposes only what
     *  {@code BukkitBridge.install} probes. The defensive guards added
     *  in {@code BukkitServerAdapter} (UnsupportedOperationException →
     *  null) keep the install path working when accessors are
     *  unimplemented, mirroring {@code PaperTestServer}'s convention. */
    private static final class StubServer implements Server {

        @Override public World getWorld() { return new StubWorld(); }
        @Override public Collection<? extends Player> getOnlinePlayers() { return List.of(); }
        @Override public Player getPlayer(String name) { return null; }
        @Override public Scheduler getScheduler() { return new StubScheduler(); }
        @Override public CommandRegistry getCommandRegistry() { throw new UnsupportedOperationException(); }
        @Override public PermissionManager getPermissionManager() { return null; }
        @Override public ModManager getModManager() { return null; }
        @Override public ProtocolVersion[] getSupportedVersions() { return new ProtocolVersion[0]; }
        @Override public void broadcastMessage(String message) { /* no-op */ }
        @Override public PluginChannel openPluginChannel(RegistryKey id) { throw new UnsupportedOperationException(); }
    }

    private static final class StubWorld implements World {
        @Override public String getName() { return "stub"; }
        @Override public int getWidth() { return 16; }
        @Override public int getHeight() { return 64; }
        @Override public int getDepth() { return 16; }
        @Override public Block getBlockAt(int x, int y, int z) { return null; }
        @Override public boolean setBlock(int x, int y, int z, BlockType type) { return false; }
        @Override public boolean isInBounds(int x, int y, int z) { return false; }
        @Override public long getTime() { return 0L; }
        @Override public void setTime(long time) { /* no-op */ }
    }

    private static final class StubScheduler implements Scheduler {
        @Override public ScheduledTask runLater(String modId, int delayTicks, Runnable task) {
            return new NoopTask();
        }
        @Override public ScheduledTask runRepeating(String modId, int initialDelay, int periodTicks, Runnable task) {
            return new NoopTask();
        }
        @Override public int cancelByOwner(String modId) { return 0; }

        private static final class NoopTask implements ScheduledTask {
            @Override public void cancel() {}
            @Override public boolean isCancelled() { return false; }
        }
    }
}

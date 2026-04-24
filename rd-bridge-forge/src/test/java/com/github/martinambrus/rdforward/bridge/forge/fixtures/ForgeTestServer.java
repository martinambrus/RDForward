package com.github.martinambrus.rdforward.bridge.forge.fixtures;

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
import com.github.martinambrus.rdforward.api.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Minimal in-memory {@code Server} for the Forge bridge tests. Only pieces
 * the ForgeBridge touches are implemented — everything else throws.
 */
public final class ForgeTestServer implements Server {

    public final List<String> broadcasts = new ArrayList<>();
    public final StubScheduler scheduler = new StubScheduler();

    @Override public World getWorld() { return null; }
    @Override public Scheduler getScheduler() { return scheduler; }
    @Override public CommandRegistry getCommandRegistry() { throw new UnsupportedOperationException(); }
    @Override public Collection<? extends Player> getOnlinePlayers() { return List.of(); }
    @Override public Player getPlayer(String name) { return null; }
    @Override public void broadcastMessage(String message) { broadcasts.add(message); }
    @Override public PermissionManager getPermissionManager() { throw new UnsupportedOperationException(); }
    @Override public ModManager getModManager() { throw new UnsupportedOperationException(); }
    @Override public ProtocolVersion[] getSupportedVersions() { return new ProtocolVersion[0]; }
    @Override public PluginChannel openPluginChannel(RegistryKey id) { throw new UnsupportedOperationException(); }

    public static final class StubScheduler implements Scheduler {
        @Override public ScheduledTask runLater(String modId, int delayTicks, Runnable task) { return NOOP; }
        @Override public ScheduledTask runRepeating(String modId, int initialDelay, int periodTicks, Runnable task) { return NOOP; }
        @Override public int cancelByOwner(String modId) { return 0; }

        private static final ScheduledTask NOOP = new ScheduledTask() {
            @Override public void cancel() {}
            @Override public boolean isCancelled() { return true; }
        };
    }
}

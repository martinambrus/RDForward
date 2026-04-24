package com.github.martinambrus.rdforward.bridge.paper.fixtures;

import com.github.martinambrus.rdforward.api.command.Command;
import com.github.martinambrus.rdforward.api.command.CommandContext;
import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.github.martinambrus.rdforward.api.command.TabCompleter;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal in-memory {@code Server} used by the Paper bridge tests. Only the
 * pieces the Paper wrappers actually touch are implemented — command
 * registry, scheduler noop, and broadcast. Anything else throws.
 */
public final class PaperTestServer implements Server {

    public final StubCommandRegistry commands = new StubCommandRegistry();
    public final StubScheduler scheduler = new StubScheduler();
    public final List<String> broadcasts = new ArrayList<>();

    @Override public World getWorld() { return null; }
    @Override public Scheduler getScheduler() { return scheduler; }
    @Override public CommandRegistry getCommandRegistry() { return commands; }

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

    public static final class StubCommandRegistry implements CommandRegistry {
        public final Map<String, Registered> registered = new LinkedHashMap<>();

        @Override
        public void register(String modId, String name, String description, Command handler) {
            registered.put(name, new Registered(modId, name, description, 0, handler));
        }

        @Override
        public void registerOp(String modId, String name, String description, int opLevel, Command handler) {
            registered.put(name, new Registered(modId, name, description, opLevel, handler));
        }

        @Override public void setTabCompleter(String modId, String name, TabCompleter completer) {}

        @Override
        public int unregisterByOwner(String modId) {
            int n = 0;
            Map<String, Registered> copy = new LinkedHashMap<>(registered);
            for (Map.Entry<String, Registered> e : copy.entrySet()) {
                if (e.getValue().modId.equals(modId)) {
                    registered.remove(e.getKey());
                    n++;
                }
            }
            return n;
        }

        @Override public boolean exists(String name) { return registered.containsKey(name); }
        @Override public List<String> listForOpLevel(int opLevel) { return new ArrayList<>(registered.keySet()); }

        public void dispatch(String name, String sender, boolean console, String[] args, List<String> replyBuffer) {
            Registered r = registered.get(name);
            if (r == null) throw new IllegalArgumentException("command not registered: " + name);
            r.handler.execute(new CommandContext() {
                @Override public String getSenderName() { return sender; }
                @Override public String[] getArgs() { return args; }
                @Override public boolean isConsole() { return console; }
                @Override public void reply(String message) { replyBuffer.add(message); }
            });
        }

        public record Registered(String modId, String name, String description, int opLevel, Command handler) {}
    }
}

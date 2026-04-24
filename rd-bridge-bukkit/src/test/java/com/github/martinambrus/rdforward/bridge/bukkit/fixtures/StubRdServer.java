package com.github.martinambrus.rdforward.bridge.bukkit.fixtures;

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
import com.github.martinambrus.rdforward.api.world.Block;
import com.github.martinambrus.rdforward.api.world.BlockType;
import com.github.martinambrus.rdforward.api.world.BlockTypes;
import com.github.martinambrus.rdforward.api.world.Location;
import com.github.martinambrus.rdforward.api.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal rd-api {@link Server} used by {@code BukkitBridge} unit tests.
 * Only exposes the pieces {@code BukkitServerAdapter} actually touches —
 * World, Scheduler, online players, broadcast. Every other method throws.
 */
public final class StubRdServer implements Server {

    public final StubWorld world = new StubWorld();
    public final StubScheduler scheduler = new StubScheduler();
    public final StubCommandRegistry commands = new StubCommandRegistry();
    public final Map<String, StubRdPlayer> players = new HashMap<>();
    public final List<String> broadcasts = new ArrayList<>();

    @Override public World getWorld() { return world; }
    @Override public Scheduler getScheduler() { return scheduler; }
    @Override public CommandRegistry getCommandRegistry() { return commands; }

    @Override
    public Collection<? extends Player> getOnlinePlayers() { return players.values(); }

    @Override public Player getPlayer(String name) { return players.get(name); }

    @Override public void broadcastMessage(String message) { broadcasts.add(message); }

    @Override public PermissionManager getPermissionManager() { throw new UnsupportedOperationException(); }
    @Override public ModManager getModManager() { throw new UnsupportedOperationException(); }
    @Override public ProtocolVersion[] getSupportedVersions() { return new ProtocolVersion[0]; }
    @Override public PluginChannel openPluginChannel(RegistryKey id) { throw new UnsupportedOperationException(); }

    /** Minimal world — stone floor at y=0, otherwise air. Mutable. */
    public static final class StubWorld implements World {
        public final Map<Long, BlockType> blocks = new HashMap<>();
        public long time = 123;

        @Override public String getName() { return "stub-world"; }
        @Override public int getWidth() { return 256; }
        @Override public int getHeight() { return 64; }
        @Override public int getDepth() { return 256; }

        @Override
        public Block getBlockAt(int x, int y, int z) {
            if (!isInBounds(x, y, z)) return null;
            BlockType type = blocks.getOrDefault(key(x, y, z),
                    y == 0 ? BlockTypes.STONE : BlockTypes.AIR);
            return new StubBlock(this, x, y, z, type);
        }

        @Override
        public boolean setBlock(int x, int y, int z, BlockType type) {
            if (!isInBounds(x, y, z)) return false;
            blocks.put(key(x, y, z), type);
            return true;
        }

        @Override
        public boolean isInBounds(int x, int y, int z) {
            return x >= 0 && x < getWidth()
                    && y >= 0 && y < getHeight()
                    && z >= 0 && z < getDepth();
        }

        @Override public long getTime() { return time; }
        @Override public void setTime(long t) { time = t; }

        private static long key(int x, int y, int z) {
            return ((long) x & 0xFFFF) << 32 | ((long) y & 0xFFFF) << 16 | ((long) z & 0xFFFF);
        }
    }

    private record StubBlock(World world, int x, int y, int z, BlockType type) implements Block {
        @Override public BlockType getType() { return type; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public World getWorld() { return world; }
        @Override public boolean setType(BlockType t) { return world.setBlock(x, y, z, t); }
    }

    /** Tracks scheduled tasks for assertion by owner id. */
    public static final class StubScheduler implements Scheduler {
        public final List<Scheduled> scheduled = new ArrayList<>();

        @Override
        public ScheduledTask runLater(String modId, int delayTicks, Runnable task) {
            Scheduled s = new Scheduled(modId, delayTicks, -1, false, task);
            scheduled.add(s);
            return s;
        }

        @Override
        public ScheduledTask runRepeating(String modId, int initialDelay, int periodTicks, Runnable task) {
            Scheduled s = new Scheduled(modId, initialDelay, periodTicks, false, task);
            scheduled.add(s);
            return s;
        }

        @Override
        public int cancelByOwner(String modId) {
            int n = 0;
            for (Scheduled s : scheduled) {
                if (!s.cancelled && modId.equals(s.owner)) {
                    s.cancelled = true;
                    n++;
                }
            }
            return n;
        }
    }

    /** Scheduled task record that survives cancellation for test assertions. */
    public static final class Scheduled implements ScheduledTask {
        public final String owner;
        public final int delay;
        public final int period;
        public boolean cancelled;
        public final Runnable task;

        public Scheduled(String owner, int delay, int period, boolean cancelled, Runnable task) {
            this.owner = owner;
            this.delay = delay;
            this.period = period;
            this.cancelled = cancelled;
            this.task = task;
        }

        @Override public void cancel() { cancelled = true; }
        @Override public boolean isCancelled() { return cancelled; }
    }

    /** Captures every registered command so tests can dispatch and assert. */
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

        /** Fire the registered handler for {@code name} with a synthetic context. */
        public void dispatch(String name, String sender, boolean console, String[] args, CapturedReply reply) {
            Registered r = registered.get(name);
            if (r == null) throw new IllegalArgumentException("command not registered: " + name);
            r.handler.execute(new CommandContext() {
                @Override public String getSenderName() { return sender; }
                @Override public String[] getArgs() { return args; }
                @Override public boolean isConsole() { return console; }
                @Override public void reply(String message) { reply.messages.add(message); }
            });
        }

        public record Registered(String modId, String name, String description, int opLevel, Command handler) {}
    }

    /** Holds replies captured from a dispatched command. */
    public static final class CapturedReply {
        public final List<String> messages = new ArrayList<>();
    }

    /** Minimal rd-api player used to exercise BukkitPlayerAdapter forwarding. */
    public static final class StubRdPlayer implements Player {
        private final String name;
        private final Location start;
        public final List<String> messages = new ArrayList<>();
        public final List<Location> teleports = new ArrayList<>();
        public String kickedReason;
        public boolean op;

        public StubRdPlayer(String name, Location start) {
            this.name = name;
            this.start = start;
        }

        @Override public String getName() { return name; }
        @Override public Location getLocation() { return teleports.isEmpty() ? start : teleports.get(teleports.size() - 1); }
        @Override public void teleport(Location location) { teleports.add(location); }
        @Override public void sendMessage(String message) { messages.add(message); }
        @Override public ProtocolVersion getProtocolVersion() { return null; }
        @Override public boolean isOp() { return op; }
        @Override public void kick(String reason) { kickedReason = reason; }
    }
}

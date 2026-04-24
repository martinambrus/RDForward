package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.server.Server;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Main bridge between RDForward and Bukkit-shaped plugins. {@link #install(Server)}
 * wires a Bukkit-shaped {@link org.bukkit.Server} into {@link Bukkit} so plugin
 * calls like {@link Bukkit#getPluginManager()} and {@link Bukkit#getScheduler()}
 * return live objects backed by the rd-api server.
 *
 * <p>{@link #uninstall()} clears the server reference so test cases and
 * shutdown see the facade revert to its pre-boot state.
 */
public final class BukkitBridge {

    private static final Logger LOG = Logger.getLogger("RDForward/BukkitBridge");

    private static volatile BukkitServerAdapter installed;

    private BukkitBridge() {}

    /** Install a Bukkit server facade backed by {@code rdServer}. */
    public static synchronized void install(Server rdServer) {
        if (installed != null) return;
        BukkitServerAdapter adapter = new BukkitServerAdapter(rdServer);
        installed = adapter;
        Bukkit.setServer(adapter);
    }

    /** Remove the installed facade. Safe to call when nothing is installed. */
    public static synchronized void uninstall() {
        installed = null;
        Bukkit.setServer(null);
    }

    public static boolean isInstalled() { return installed != null; }

    /** Bukkit-shaped server backed by an rd-api Server. */
    private static final class BukkitServerAdapter implements org.bukkit.Server {

        private final Server rd;
        private final PluginManager pluginManager = new StubPluginManager();
        private final BukkitScheduler scheduler;
        private final ConsoleCommandSender console = new ConsoleCommandSender();
        private final World defaultWorld;

        BukkitServerAdapter(Server rd) {
            this.rd = rd;
            this.scheduler = new BukkitSchedulerAdapter(rd.getScheduler());
            this.defaultWorld = new BukkitWorldAdapter(rd.getWorld());
        }

        @Override public String getName() { return "RDForward"; }
        @Override public String getVersion() { return "bridge-1.0"; }
        @Override public String getBukkitVersion() { return "1.21.11-R0.1-STUB"; }
        @Override public Logger getLogger() { return LOG; }

        @Override
        public int broadcastMessage(String message) {
            rd.broadcastMessage(message);
            return rd.getOnlinePlayers().size();
        }

        @Override public PluginManager getPluginManager() { return pluginManager; }
        @Override public BukkitScheduler getScheduler() { return scheduler; }
        @Override public ConsoleCommandSender getConsoleSender() { return console; }

        @Override
        public Player getPlayer(String name) {
            return BukkitPlayerAdapter.wrap(rd.getPlayer(name), defaultWorld);
        }

        @Override
        public Collection<Player> getOnlinePlayers() {
            List<Player> out = new ArrayList<>();
            for (com.github.martinambrus.rdforward.api.player.Player p : rd.getOnlinePlayers()) {
                out.add(BukkitPlayerAdapter.wrap(p, defaultWorld));
            }
            return out;
        }

        @Override public List<World> getWorlds() { return List.of(defaultWorld); }

        @Override
        public World getWorld(String name) {
            return name != null && name.equals(defaultWorld.getName()) ? defaultWorld : null;
        }
    }

    /** Plugin manager stub — forwards {@code registerEvents} to the bridge adapter. */
    private static final class StubPluginManager implements PluginManager {

        @Override
        public void registerEvents(Listener listener, Plugin plugin) {
            BukkitEventAdapter.register(listener, plugin == null ? null : plugin.getName());
        }

        @Override public void disablePlugin(Plugin plugin) { /* lifecycle owned by rd-mod-loader */ }

        @Override public Plugin getPlugin(String name) { return null; }

        @Override public Plugin[] getPlugins() { return new Plugin[0]; }
    }
}

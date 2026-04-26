// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.mod.ModManager;
import com.github.martinambrus.rdforward.api.permission.PermissionRegistry;
import com.github.martinambrus.rdforward.api.permission.RegisteredPermission;
import com.github.martinambrus.rdforward.api.server.Server;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        // Surface stub-call warnings in-game so operators see what
        // plugin features still need real implementations without
        // tailing the server log.
        com.github.martinambrus.rdforward.api.stub.StubCallLog.setBroadcastSink(msg -> {
            Server rd = currentRdServer();
            if (rd != null) {
                try { rd.broadcastMessage(msg); } catch (Throwable ignored) {}
            }
        });
    }

    /** Remove the installed facade. Safe to call when nothing is installed. */
    public static synchronized void uninstall() {
        installed = null;
        Bukkit.setServer(null);
        com.github.martinambrus.rdforward.api.stub.StubCallLog.setBroadcastSink(null);
        // Reset listener registry so successive tests don't see ghost
        // handlers from a prior boot.
        BukkitEventAdapter.clearAll();
    }

    public static boolean isInstalled() { return installed != null; }

    /** @return the rd-api {@link Server} backing the installed bridge, or {@code null} if none is installed. */
    public static Server currentRdServer() {
        BukkitServerAdapter adapter = installed;
        return adapter == null ? null : adapter.rd;
    }

    /** Bukkit-shaped server backed by an rd-api Server. */
    private static final class BukkitServerAdapter implements org.bukkit.Server {

        private final Server rd;
        private final PluginManager pluginManager;
        private final BukkitScheduler scheduler;
        private final ConsoleCommandSender console = new DefaultConsoleCommandSender();
        private final World defaultWorld;

        BukkitServerAdapter(Server rd) {
            this.rd = rd;
            this.scheduler = new BukkitSchedulerAdapter(rd.getScheduler());
            this.defaultWorld = new BukkitWorldAdapter(rd.getWorld());
            // rd-api intentionally allows getPermissionManager() /
            // getModManager() to be unimplemented (test fixtures throw
            // UnsupportedOperationException for them); fall back to null
            // so the bridge still installs and downstream code branches
            // on absence rather than crashing the whole boot.
            this.pluginManager = new StubPluginManager(
                    safeRegistry(rd), safeModManager(rd));
        }

        private static PermissionRegistry safeRegistry(Server rd) {
            try {
                com.github.martinambrus.rdforward.api.permission.PermissionManager pm = rd.getPermissionManager();
                return pm == null ? null : pm.getRegistry();
            } catch (UnsupportedOperationException e) {
                return null;
            }
        }

        private static ModManager safeModManager(Server rd) {
            try {
                return rd.getModManager();
            } catch (UnsupportedOperationException e) {
                return null;
            }
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

    /**
     * Plugin manager stub — forwards {@code registerEvents} to the bridge
     * adapter. Subclasses {@link SimplePluginManager} so that plugins which
     * gate functionality behind {@code instanceof SimplePluginManager}
     * (notably LuckPerms's permission/subscription map injectors) accept
     * us as a real Bukkit plugin manager.
     *
     * <p>{@code addPermission} / {@code removePermission} forward to the
     * rd-api {@link PermissionRegistry} so that Bukkit-declared permissions
     * become first-class server citizens (visible to admin commands and to
     * non-Bukkit code via {@code PermissionManager.hasPermission}).
     * {@code isPluginEnabled} routes through {@link ModManager#isLoaded} so
     * Vault-style detection probes get a real answer.
     */
    private static final class StubPluginManager extends SimplePluginManager {

        private final PermissionRegistry registry;
        private final ModManager modManager;

        StubPluginManager(PermissionRegistry registry, ModManager modManager) {
            this.registry = registry;
            this.modManager = modManager;
        }

        @Override
        public void registerEvents(Listener listener, Plugin plugin) {
            BukkitEventAdapter.register(listener, plugin == null ? null : plugin.getName());
        }

        // Direct single-event registration. RDForward dispatches Bukkit events
        // via @EventHandler scanning in BukkitEventAdapter, so individual
        // explicit registrations are accepted silently — overriding here
        // keeps SimplePluginManager's StubCallLog from spamming on every
        // plugin that uses this overload (LuckPerms registers 1 listener
        // this way at boot).
        @Override
        public void registerEvent(Class arg0, org.bukkit.event.Listener arg1,
                                  org.bukkit.event.EventPriority arg2,
                                  org.bukkit.plugin.EventExecutor arg3,
                                  Plugin arg4) {
            // silent
        }

        @Override
        public void registerEvent(Class arg0, org.bukkit.event.Listener arg1,
                                  org.bukkit.event.EventPriority arg2,
                                  org.bukkit.plugin.EventExecutor arg3,
                                  Plugin arg4, boolean arg5) {
            // silent
        }

        @Override public void disablePlugin(Plugin plugin) { /* lifecycle owned by rd-mod-loader */ }

        @Override public Plugin getPlugin(String name) { return null; }

        @Override public Plugin[] getPlugins() { return new Plugin[0]; }

        @Override
        public boolean isPluginEnabled(String name) {
            return modManager != null && name != null && modManager.isLoaded(name);
        }

        @Override
        public boolean isPluginEnabled(Plugin plugin) {
            return plugin != null && isPluginEnabled(plugin.getName());
        }

        @Override
        public void addPermission(Permission perm) {
            if (perm == null || perm.getName() == null) return;
            if (registry != null) {
                registry.register(new RegisteredPermission(
                        perm.getName(),
                        toApiDefault(perm.getDefault()),
                        perm.getChildren()));
            }
            // Write through to the public Bukkit map (LuckPerms wraps this
            // field and listens for new entries — bypassing it would hide
            // the registration from the wrapper).
            this.permissions.put(perm.getName(), perm);
            updateDefaultPermsMap(perm, true);
        }

        @Override
        public void addPermission(Permission perm, boolean recalculate) {
            addPermission(perm);
        }

        @Override
        public void removePermission(Permission perm) {
            if (perm == null || perm.getName() == null) return;
            removePermission(perm.getName());
        }

        @Override
        public void removePermission(String name) {
            if (name == null) return;
            if (registry != null) registry.unregister(name);
            Object existing = this.permissions.remove(name);
            if (existing instanceof Permission p) updateDefaultPermsMap(p, false);
        }

        @Override
        public Permission getPermission(String name) {
            Object p = this.permissions.get(name);
            return p instanceof Permission ? (Permission) p : null;
        }

        @Override
        public Set<Permission> getPermissions() {
            Set<Permission> out = new HashSet<>();
            for (Object v : this.permissions.values()) {
                if (v instanceof Permission p) out.add(p);
            }
            return out;
        }

        @Override
        public Set<Permission> getDefaultPermissions(boolean op) {
            Set<Permission> out = new HashSet<>();
            if (registry == null) return out;
            for (String name : registry.defaultsFor(op)) {
                Object p = this.permissions.get(name);
                if (p instanceof Permission perm) out.add(perm);
            }
            return out;
        }

        @Override
        public void recalculatePermissionDefaults(Permission perm) {
            // No per-player attachment cache to invalidate — silent no-op.
        }

        /** Keep the public {@code defaultPerms} map consistent with the registry. */
        @SuppressWarnings("unchecked")
        private void updateDefaultPermsMap(Permission perm, boolean adding) {
            PermissionDefault def = perm.getDefault();
            if (def == null) return;
            updateDefaultPermsBucket(true, perm, def.getValue(true), adding);
            updateDefaultPermsBucket(false, perm, def.getValue(false), adding);
        }

        @SuppressWarnings("unchecked")
        private void updateDefaultPermsBucket(boolean opKey, Permission perm, boolean appliesForKey, boolean adding) {
            if (!appliesForKey) return;
            Object bucket = this.defaultPerms.computeIfAbsent(opKey, k -> new HashSet<Permission>());
            if (bucket instanceof Set s) {
                if (adding) s.add(perm); else s.remove(perm);
            }
        }

        private static com.github.martinambrus.rdforward.api.permission.PermissionDefault
                toApiDefault(PermissionDefault bukkit) {
            if (bukkit == null) return com.github.martinambrus.rdforward.api.permission.PermissionDefault.OP;
            return switch (bukkit) {
                case TRUE -> com.github.martinambrus.rdforward.api.permission.PermissionDefault.TRUE;
                case FALSE -> com.github.martinambrus.rdforward.api.permission.PermissionDefault.FALSE;
                case OP -> com.github.martinambrus.rdforward.api.permission.PermissionDefault.OP;
                case NOT_OP -> com.github.martinambrus.rdforward.api.permission.PermissionDefault.NOT_OP;
            };
        }
    }
}

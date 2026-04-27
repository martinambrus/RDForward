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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

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

    /** Registry of currently-loaded Bukkit plugins keyed by plugin.yml
     *  {@code name}. Populated by {@link BukkitPluginLoader} after the
     *  plugin instance is constructed and cleared by
     *  {@link BukkitPluginWrapper}'s disable / failure paths. The
     *  {@link StubPluginManager#getPlugin} accessor reads this map so
     *  plugins that look themselves up via
     *  {@code Bukkit.getPluginManager().getPlugin(getName())} (mcbans
     *  v3.8 in {@code BukkitInterface.pluginInterface(String)}) get a
     *  real instance instead of {@code null}. */
    private static final Map<String, JavaPlugin> loadedPlugins = new ConcurrentHashMap<>();

    private BukkitBridge() {}

    /** Track {@code plugin} under its declared name. Called from
     *  {@link BukkitPluginLoader#load} immediately after the plugin
     *  instance is constructed. */
    public static void registerPlugin(String name, JavaPlugin plugin) {
        if (name == null || plugin == null) return;
        loadedPlugins.put(name, plugin);
    }

    /** Drop the registry entry for {@code name}. Called by
     *  {@link BukkitPluginWrapper} when a plugin disables (cleanly,
     *  self-disables, or throws during enable). Idempotent. */
    public static void unregisterPlugin(String name) {
        if (name == null) return;
        loadedPlugins.remove(name);
    }

    /** @return the live {@link JavaPlugin} previously registered under
     *  {@code name}, or {@code null} if none. */
    public static JavaPlugin lookupPlugin(String name) {
        return name == null ? null : loadedPlugins.get(name);
    }

    /** @return a snapshot of every currently-registered plugin. */
    public static java.util.Collection<JavaPlugin> allPlugins() {
        return new java.util.ArrayList<>(loadedPlugins.values());
    }

    /** Install a Bukkit server facade backed by {@code rdServer}. */
    public static synchronized void install(Server rdServer) {
        if (installed != null) return;
        BukkitServerAdapter adapter = new BukkitServerAdapter(rdServer);
        installed = adapter;
        Bukkit.setServer(adapter);
        // StubCallLog broadcast sink intentionally NOT installed: stub
        // warnings stay in the server console (JUL WARNING) so operators
        // can audit missing API coverage without spamming every player's
        // chat each time a plugin hits an unimplemented method.
        // Mirror dynamic Bukkit command registrations (plugins that bypass
        // plugin.yml and reflect SimplePluginManager.commandMap directly —
        // notably WorldEdit's CommandRegistration) into the rd-api registry
        // so the typed labels actually dispatch.
        org.bukkit.command.SimpleCommandMap.setBridgeSink(BukkitBridge::mirrorDynamicCommand);
        // Wire Player.hidePlayer/showPlayer through to rd-server's
        // per-pair visibility filter so VanishNoPacket-style plugins
        // running without ProtocolLib actually drop the vanished player
        // from each recipient's view (spawn/despawn/position/tab list).
        rdServer.setVisibilityFilter((sender, recipient) ->
                !PlayerVisibilityRegistry.isHidden(recipient, sender));
        installNmsWarnOnceFilter();
    }

    /** WE 5.6.1's {@code BukkitWorld} resolves two NMS Methods at clinit
     *  ({@code nmsGetMethod}, {@code nmsSetSafeMethod}) by reflectively
     *  walking the CraftBukkit server class hierarchy. RDForward has no
     *  NMS layer, so both fields stay null and every {@code //set} write
     *  cycle prints a NPE-stack WARNING that WE catches and logs before
     *  falling back to the public Block API. Stack-spam noise — they
     *  cannot be fixed without a real NMS surface. This filter keeps the
     *  first occurrence per message (so operators see WE took the
     *  fallback path once) and drops subsequent identical records. */
    private static final java.util.Set<String> WE_NMS_LOGGED =
            java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static volatile boolean WE_NMS_FILTER_INSTALLED = false;

    private static void installNmsWarnOnceFilter() {
        if (WE_NMS_FILTER_INSTALLED) return;
        WE_NMS_FILTER_INSTALLED = true;
        java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
        for (java.util.logging.Handler h : root.getHandlers()) {
            java.util.logging.Filter existing = h.getFilter();
            h.setFilter(record -> {
                String m = record.getMessage();
                if (m != null
                        && (m.contains("Failed to do NMS access for direct NBT data copy")
                            || m.contains("Failed to do NMS safe block set"))) {
                    return WE_NMS_LOGGED.add(m);
                }
                return existing == null || existing.isLoggable(record);
            });
        }
    }

    /** Remove the installed facade. Safe to call when nothing is installed. */
    public static synchronized void uninstall() {
        installed = null;
        Bukkit.setServer(null);
        com.github.martinambrus.rdforward.api.stub.StubCallLog.setBroadcastSink(null);
        org.bukkit.command.SimpleCommandMap.setBridgeSink(null);
        // Reset listener registry so successive tests don't see ghost
        // handlers from a prior boot.
        BukkitEventAdapter.clearAll();
    }

    /** Sink invoked from {@link org.bukkit.command.SimpleCommandMap#register}
     *  whenever a plugin registers a command dynamically (not via
     *  {@code plugin.yml}). Each label (the command's primary name plus every
     *  alias) is forwarded to the rd-api {@link com.github.martinambrus.rdforward.api.command.CommandRegistry}
     *  under the plugin's mod id (= {@code fallbackPrefix}). The dispatch
     *  lambda calls {@link org.bukkit.command.Command#execute} on the original
     *  command, which in WorldEdit's case is a {@code DynamicPluginCommand}
     *  whose {@code execute} forwards to {@code CommandExecutor.onCommand}. */
    private static void mirrorDynamicCommand(String fallbackPrefix, org.bukkit.command.Command cmd) {
        Server rd = currentRdServer();
        if (rd == null || cmd == null) return;
        com.github.martinambrus.rdforward.api.command.CommandRegistry registry;
        try { registry = rd.getCommandRegistry(); } catch (Throwable t) { return; }
        if (registry == null) return;
        String modId = fallbackPrefix == null || fallbackPrefix.isEmpty() ? "bukkit" : fallbackPrefix;
        String description = cmd.getDescription() == null ? "" : cmd.getDescription();
        java.util.LinkedHashSet<String> labels = new java.util.LinkedHashSet<>();
        if (cmd.getName() != null) labels.add(cmd.getName());
        if (cmd.getAliases() != null) {
            for (Object a : cmd.getAliases()) {
                String s = String.valueOf(a);
                if (!s.isEmpty()) labels.add(s);
            }
        }
        for (String label : labels) {
            final String dispatchLabel = label;
            registry.register(modId, label, description, ctx -> {
                org.bukkit.command.CommandSender sender =
                        BukkitPluginWrapper.resolveSender(ctx.getSenderName(), ctx.isConsole());
                try {
                    cmd.execute(sender, dispatchLabel, ctx.getArgs());
                } catch (Throwable t) {
                    LOG.warning("[BukkitBridge] Dynamic command '" + dispatchLabel + "' threw: " + t);
                    ctx.reply("An internal error occurred while executing this command.");
                }
            });
        }
    }

    public static boolean isInstalled() { return installed != null; }

    /** @return the rd-api {@link Server} backing the installed bridge, or {@code null} if none is installed. */
    public static Server currentRdServer() {
        BukkitServerAdapter adapter = installed;
        return adapter == null ? null : adapter.rd;
    }

    /** @return the bridge's default {@link World}, or {@code null} if none is installed. */
    public static World defaultWorld() {
        BukkitServerAdapter adapter = installed;
        return adapter == null ? null : adapter.defaultWorld;
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

        // Direct single-event registration. Forwards to BukkitEventAdapter
        // so executor-based listeners (notably adventure-platform-bukkit's
        // PlayerJoin/PlayerQuit viewer-tracking handlers, used by every
        // LuckPerms message dispatch) actually fire when the corresponding
        // ServerEvents callback runs. Without this wiring Adventure's
        // viewer map stays empty and audiences.player(uuid) returns the
        // empty audience for every online player.
        @Override
        public void registerEvent(Class arg0, org.bukkit.event.Listener arg1,
                                  org.bukkit.event.EventPriority arg2,
                                  org.bukkit.plugin.EventExecutor arg3,
                                  Plugin arg4) {
            BukkitEventAdapter.registerExecutor(arg0, arg1, arg2, arg3,
                    arg4 == null ? null : arg4.getName(), false);
        }

        @Override
        public void registerEvent(Class arg0, org.bukkit.event.Listener arg1,
                                  org.bukkit.event.EventPriority arg2,
                                  org.bukkit.plugin.EventExecutor arg3,
                                  Plugin arg4, boolean arg5) {
            BukkitEventAdapter.registerExecutor(arg0, arg1, arg2, arg3,
                    arg4 == null ? null : arg4.getName(), arg5);
        }

        /** Real Bukkit's {@code disablePlugin} runs the plugin's {@code
         *  onDisable}, unregisters its events, and flips its enabled flag.
         *  RDForward owns the disable + unregister side via rd-mod-loader,
         *  but plugins (VanishNoPacket 3.14+, mcbans v3.8) call this on
         *  themselves DURING {@code onEnable} to signal a fatal init
         *  failure. Flipping the flag here lets {@link
         *  BukkitPluginWrapper#onEnable} detect the self-disable on
         *  return and short-circuit listener/command wiring. */
        @Override
        public void disablePlugin(Plugin plugin) {
            if (plugin instanceof org.bukkit.plugin.java.JavaPlugin jp) {
                jp.setEnabled(false);
            }
        }

        @Override public Plugin getPlugin(String name) { return BukkitBridge.lookupPlugin(name); }

        @Override
        public Plugin[] getPlugins() {
            java.util.Collection<JavaPlugin> snapshot = BukkitBridge.allPlugins();
            return snapshot.toArray(new Plugin[0]);
        }

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

// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView$Scale;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Bukkit-shaped static facade. Plugins call {@code Bukkit.getXxx()} to
 * reach global server services. The RDForward host installs the real
 * server via {@link #setServer(Server)} — before that point, lookups
 * return {@code null} or empty collections, matching pre-boot Bukkit.
 */
public final class Bukkit {

    private static volatile Server server;

    private Bukkit() {}

    /** Host-side hook — called once by the Bukkit bridge after boot. */
    public static void setServer(Server s) {
        server = s;
    }

    public static Server getServer() { return server; }

    public static String getName() {
        return server == null ? "RDForward" : server.getName();
    }

    public static String getVersion() {
        return server == null ? "unknown" : server.getVersion();
    }

    public static String getBukkitVersion() {
        return server == null ? "stub" : server.getBukkitVersion();
    }

    public static Logger getLogger() {
        return server == null ? Logger.getLogger("Bukkit") : server.getLogger();
    }

    public static int broadcastMessage(String message) {
        return server == null ? 0 : server.broadcastMessage(message);
    }

    public static PluginManager getPluginManager() {
        return server == null ? null : server.getPluginManager();
    }

    public static BukkitScheduler getScheduler() {
        return server == null ? null : server.getScheduler();
    }

    /** Static accessor real Bukkit added in 1.0; some plugins (Vault's
     *  bStats Metrics, MapMetrics) call it directly rather than via
     *  {@code Bukkit.getServer().getServicesManager()} — without this
     *  shortcut they {@link NoSuchMethodError} during their {@code
     *  onEnable}. */
    public static org.bukkit.plugin.ServicesManager getServicesManager() {
        return server == null ? null : server.getServicesManager();
    }

    public static ConsoleCommandSender getConsoleSender() {
        return server == null ? null : server.getConsoleSender();
    }

    public static Player getPlayer(String name) {
        return server == null ? null : server.getPlayer(name);
    }

    /** UUID-keyed online player lookup. mChat 4.x's
     *  {@code API.checkPermissions} dispatches on
     *  {@code Bukkit.getPlayer(uuid)} during join/quit listeners — without
     *  the static delegate the listener {@code NoSuchMethodError}s on
     *  every join. Delegates to {@link Server#getPlayer(java.util.UUID)}. */
    public static Player getPlayer(java.util.UUID id) {
        return server == null ? null : server.getPlayer(id);
    }

    public static Collection<Player> getOnlinePlayers() {
        return server == null ? List.of() : server.getOnlinePlayers();
    }

    public static List<World> getWorlds() {
        return server == null ? List.of() : server.getWorlds();
    }

    public static World getWorld(String name) {
        return server == null ? null : server.getWorld(name);
    }

    /** @return the server's world-container directory. Delegates to
     *  {@link Server#getWorldContainer()}. HomeSpawnPlus's new-player
     *  detection probes for {@code <world>/players/<name>.dat}. */
    public static java.io.File getWorldContainer() {
        return server == null ? new java.io.File(".") : server.getWorldContainer();
    }

    /** UUID-keyed world lookup. EssentialsX's {@code LazyLocation
     *  .location} resolves a saved logout/teleport target by re-hydrating
     *  the world via the UUID it persisted earlier; without this overload
     *  every {@code /tpr}, {@code /back}, and {@code /home} that round-trips
     *  through {@code LazyLocation} aborts on {@link NoSuchMethodError}.
     *  Walks the world list and matches against {@link World#getUID()}. */
    public static World getWorld(java.util.UUID uid) {
        if (uid == null || server == null) return null;
        for (World w : server.getWorlds()) {
            if (w != null && uid.equals(w.getUID())) return w;
        }
        return null;
    }

    /** @return {@code false} when no server is installed; otherwise
     *  delegates to {@link Server#getOnlineMode()}. LoginSecurity's
     *  bundled bStats {@code Metrics.appendPlatformData} calls this
     *  statically every 30 minutes and crashes the metrics thread on
     *  {@link NoSuchMethodError} otherwise. RDForward never implements
     *  Mojang online-mode auth, so the value is always {@code false}. */
    public static boolean getOnlineMode() {
        return server != null && server.getOnlineMode();
    }

    /** @return a non-null {@link Server$Spigot} stub. Real Bukkit returns
     *  {@code Server.Spigot} which carries Spigot/Paper-specific config
     *  hooks. LoginSecurity's {@code ThreadingModule.onPlayerLogin}
     *  reads {@code Bukkit.spigot().getConfig().getBoolean(...)}; the
     *  whole block is wrapped in {@code catch (Exception)} but
     *  {@link NoSuchMethodError} is an {@link Error}, not an
     *  {@link Exception}, so it bypasses the swallow and aborts the
     *  login listener. We return a singleton stub whose
     *  {@code getConfig()} returns an empty {@link YamlConfiguration},
     *  matching the no-bungeecord defaults LS expects. */
    public static Server$Spigot spigot() {
        return SPIGOT;
    }

    private static final Server$Spigot SPIGOT = new Server$Spigot() {
        private final org.bukkit.configuration.file.YamlConfiguration empty =
                new org.bukkit.configuration.file.YamlConfiguration();
        @Override public org.bukkit.configuration.file.YamlConfiguration getConfig() { return empty; }
        @Override public org.bukkit.configuration.file.YamlConfiguration getBukkitConfig() { return empty; }
        @Override public org.bukkit.configuration.file.YamlConfiguration getSpigotConfig() { return empty; }
        @Override public org.bukkit.configuration.file.YamlConfiguration getPaperConfig() { return empty; }
    };

    /** @return {@code true} when the current thread is the server's
     *  tick loop. LoginSecurity's {@code PlayerSession.performAction}
     *  calls this on every async auth task to decide whether to bounce
     *  back to the main thread; {@link NoSuchMethodError} kills the
     *  auth pool worker otherwise. RDForward names its tick thread
     *  {@code RDForward-TickLoop} — see
     *  {@code com.github.martinambrus.rdforward.server.ServerTickLoop}. */
    public static boolean isPrimaryThread() {
        if ("RDForward-TickLoop".equals(Thread.currentThread().getName())) return true;
        // Bukkit treats plugin lifecycle callbacks (onLoad/onEnable/onDisable)
        // as running on the primary thread. RDForward boots plugins on the
        // JVM "main" thread before the tick loop starts, so plugins like
        // CoreProtect that gate work behind {@code if (!isPrimaryThread()) {
        // schedule + future.join() }} would deadlock — the scheduled task
        // queues for a tick loop that has not started, and join() blocks
        // forever. The bridge plugin wrapper sets this flag for the
        // duration of every plugin lifecycle call.
        Boolean inLifecycle = INSIDE_PLUGIN_LIFECYCLE.get();
        return inLifecycle != null && inLifecycle;
    }

    /** Set true by BukkitPluginWrapper around onLoad/onEnable/onDisable so
     *  {@link #isPrimaryThread()} returns the Bukkit-correct answer for
     *  plugins that gate scheduled work behind it. */
    public static final ThreadLocal<Boolean> INSIDE_PLUGIN_LIFECYCLE = new ThreadLocal<>();

    /** Real Paper exposes the internal NMS bridge via {@code getUnsafe()}.
     *  EssentialsX's shaded {@code BukkitComponentSerializer.<clinit>}
     *  resolves {@code Bukkit.getUnsafe()} at first message dispatch and
     *  hard-fails the static initialiser on {@link NoSuchMethodError},
     *  poisoning the class for the rest of the session (every subsequent
     *  send throws {@code NoClassDefFoundError: Could not initialize
     *  class ...BukkitComponentSerializer}). RDForward has no NMS, so we
     *  return a JDK-Proxy stub that logs once per unique method via
     *  {@link com.github.martinambrus.rdforward.api.stub.StubCallLog} and
     *  hands back type-appropriate zeros / nulls. Adventure's fallback
     *  path null-checks each serializer return and substitutes its own
     *  defaults, so the clinit completes. */
    public static UnsafeValues getUnsafe() {
        return STUB_UNSAFE;
    }

    private static final UnsafeValues STUB_UNSAFE = (UnsafeValues)
            java.lang.reflect.Proxy.newProxyInstance(
                    UnsafeValues.class.getClassLoader(),
                    new Class<?>[] { UnsafeValues.class },
                    (proxy, method, args) -> {
                        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                                null, "org.bukkit.UnsafeValues." + method.getName());
                        // EssentialsX's PaperBiomeKeyProvider reads
                        // {@code Bukkit.getUnsafe().getBiomeKey(...)} during
                        // the /tpr biome-exclusion check and immediately
                        // calls {@code .toString()} on the result. A null
                        // here NPEs synchronously inside the CompletableFuture
                        // lambda that {@code RandomTeleport.attemptRandomLocation}
                        // chains through, so the whole future hangs and
                        // /tpr never reaches teleport. Return a stable
                        // namespaced key.
                        if ("getBiomeKey".equals(method.getName())) {
                            return org.bukkit.NamespacedKey.minecraft("plains");
                        }
                        Class<?> rt = method.getReturnType();
                        if (rt == boolean.class) return false;
                        if (rt == int.class) return 0;
                        if (rt == long.class) return 0L;
                        if (rt == short.class) return (short) 0;
                        if (rt == byte.class) return (byte) 0;
                        if (rt == double.class) return 0.0;
                        if (rt == float.class) return 0.0f;
                        if (rt == char.class) return '\0';
                        return null;
                    });

    /**
     * Mint a new {@link MapView} for the given world. Real Bukkit
     * persists the view as a numbered map; the stub returns an
     * in-memory MapView whose renderer list is mutable so plugins
     * (LoginSecurity's CaptchaManager) can iterate and replace
     * renderers, but the rendered surface is never sent to clients.
     */
    public static MapView createMap(World world) {
        return new StubMapView(world, nextMapId.getAndIncrement());
    }

    private static final java.util.concurrent.atomic.AtomicInteger nextMapId =
            new java.util.concurrent.atomic.AtomicInteger(0);

    /** In-memory {@link MapView} with mutable renderer list. The methods
     *  that aren't actively driven by plugins return safe defaults. */
    private static final class StubMapView implements MapView {
        private World world;
        private final int id;
        private final List<MapRenderer> renderers = new ArrayList<>();
        private int centerX, centerZ;
        private MapView$Scale scale = MapView$Scale.NORMAL;
        private boolean trackingPosition, unlimitedTracking, locked;

        StubMapView(World world, int id) { this.world = world; this.id = id; }

        @Override public int getId() { return id; }
        @Override public boolean isVirtual() { return false; }
        @Override public MapView$Scale getScale() { return scale; }
        @Override public void setScale(MapView$Scale s) { this.scale = s; }
        @Override public int getCenterX() { return centerX; }
        @Override public int getCenterZ() { return centerZ; }
        @Override public void setCenterX(int x) { this.centerX = x; }
        @Override public void setCenterZ(int z) { this.centerZ = z; }
        @Override public World getWorld() { return world; }
        @Override public void setWorld(World w) { this.world = w; }
        @Override public List getRenderers() { return new ArrayList<>(renderers); }
        @Override public void addRenderer(MapRenderer r) { if (r != null) renderers.add(r); }
        @Override public boolean removeRenderer(MapRenderer r) { return renderers.remove(r); }
        @Override public boolean isTrackingPosition() { return trackingPosition; }
        @Override public void setTrackingPosition(boolean t) { this.trackingPosition = t; }
        @Override public boolean isUnlimitedTracking() { return unlimitedTracking; }
        @Override public void setUnlimitedTracking(boolean u) { this.unlimitedTracking = u; }
        @Override public boolean isLocked() { return locked; }
        @Override public void setLocked(boolean l) { this.locked = l; }
    }
}

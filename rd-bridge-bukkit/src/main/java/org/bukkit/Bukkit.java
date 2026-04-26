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

    public static ConsoleCommandSender getConsoleSender() {
        return server == null ? null : server.getConsoleSender();
    }

    public static Player getPlayer(String name) {
        return server == null ? null : server.getPlayer(name);
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

    /** @return {@code false} when no server is installed; otherwise
     *  delegates to {@link Server#getOnlineMode()}. LoginSecurity's
     *  bundled bStats {@code Metrics.appendPlatformData} calls this
     *  statically every 30 minutes and crashes the metrics thread on
     *  {@link NoSuchMethodError} otherwise. RDForward never implements
     *  Mojang online-mode auth, so the value is always {@code false}. */
    public static boolean getOnlineMode() {
        return server != null && server.getOnlineMode();
    }

    /** @return {@code true} when the current thread is the server's
     *  tick loop. LoginSecurity's {@code PlayerSession.performAction}
     *  calls this on every async auth task to decide whether to bounce
     *  back to the main thread; {@link NoSuchMethodError} kills the
     *  auth pool worker otherwise. RDForward names its tick thread
     *  {@code RDForward-TickLoop} — see
     *  {@code com.github.martinambrus.rdforward.server.ServerTickLoop}. */
    public static boolean isPrimaryThread() {
        return "RDForward-TickLoop".equals(Thread.currentThread().getName());
    }

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

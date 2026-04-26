// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.plugin;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;

/**
 * Bukkit-shaped plugin manager. Only {@link #registerEvents(Listener, Plugin)}
 * is wired through to RDForward — events registered here flow through
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter}
 * and land on rd-api {@code ServerEvents}. Other methods (plugin enable /
 * disable, plugin lookup) return sensible defaults because the RDForward
 * mod loader owns lifecycle, not the plugin manager.
 */
public interface PluginManager {

    /** Register every {@code @EventHandler} method on {@code listener}. */
    void registerEvents(Listener listener, Plugin plugin);

    /**
     * Single-handler event registration. Used by libraries that build their
     * own reflection-free dispatch pipeline (e.g. adventure-platform-bukkit).
     * The default implementation is a no-op: RDForward's Bukkit bridge does
     * not yet wire per-class event executors into {@code ServerEvents}, so
     * only {@link #registerEvents(Listener, Plugin)} is actually hooked up.
     */
    default void registerEvent(Class<? extends Event> event, Listener listener,
                               EventPriority priority, EventExecutor executor,
                               Plugin plugin) {
    }

    /** Six-arg overload — upstream signature (accepts {@code ignoreCancelled}). */
    default void registerEvent(Class<? extends Event> event, Listener listener,
                               EventPriority priority, EventExecutor executor,
                               Plugin plugin, boolean ignoreCancelled) {
    }

    /**
     * Dispatch {@code event} to every registered listener whose
     * {@code @EventHandler} parameter type is assignable from the
     * event's runtime class. Plugins call this to fire their own
     * synthetic events — e.g. LoginSecurity's
     * {@code PlayerSession.performAction} dispatches an
     * {@code AuthActionEvent} after a successful login attempt;
     * without this method declaration the bytecode call site (an
     * {@code INVOKEINTERFACE PluginManager.callEvent}) fails at link
     * time with {@link NoSuchMethodError}.
     *
     * <p>Default implementation forwards to
     * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter#dispatchPluginEvent}
     * which walks the listener registry built up by
     * {@link #registerEvents}.
     */
    default void callEvent(Event event) {
        com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter.dispatchPluginEvent(event);
    }

    /** Noop — RDForward does not support plugin disable from the plugin manager. */
    void disablePlugin(Plugin plugin);

    /** @return the plugin with the given name if loaded, or null. */
    Plugin getPlugin(String name);

    /** @return an array of currently-loaded plugins (empty by default). */
    Plugin[] getPlugins();

    /**
     * @return whether a plugin with the given name is loaded + enabled.
     *         RDForward's bridge doesn't expose cross-plugin lookup yet,
     *         so this returns {@code false}. Plugins that gate optional
     *         integrations on this (e.g. Vault, LuckPerms's hook check)
     *         therefore skip the integration.
     */
    default boolean isPluginEnabled(String name) { return false; }

    /** @return whether {@code plugin} is enabled — defaults to {@link Plugin#isEnabled()}. */
    default boolean isPluginEnabled(Plugin plugin) { return plugin != null && plugin.isEnabled(); }

    /**
     * @return the {@link Permission} previously added under this name,
     *         or {@code null} if none. RDForward's bridge doesn't track
     *         registered permissions, so this always returns {@code null}.
     */
    default Permission getPermission(String name) { return null; }

    /** No-op — RDForward does not track registered permissions. */
    default void addPermission(Permission perm) {}

    /** No-op — RDForward does not track registered permissions. */
    default void removePermission(Permission perm) {}

    /** No-op — RDForward does not track registered permissions. */
    default void removePermission(String name) {}
}

// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.BlockBreakCallback;
import com.github.martinambrus.rdforward.api.event.server.BlockPlaceCallback;
import com.github.martinambrus.rdforward.api.event.server.ChatCallback;
import com.github.martinambrus.rdforward.api.event.server.PlayerJoinCallback;
import com.github.martinambrus.rdforward.api.event.server.PlayerLeaveCallback;
import com.github.martinambrus.rdforward.api.event.server.PlayerMoveCallback;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Reflectively scans a Bukkit {@link Listener} for {@code @EventHandler}
 * methods and registers matching adapters on RDForward's
 * {@link ServerEvents}. Event registration happens inside the mod's
 * {@code EventOwnership} scope so the listeners are swept on mod disable.
 *
 * <p>Supported mappings:
 * <ul>
 *   <li>{@link BlockBreakEvent}      -&gt; {@link ServerEvents#BLOCK_BREAK}</li>
 *   <li>{@link BlockPlaceEvent}      -&gt; {@link ServerEvents#BLOCK_PLACE}</li>
 *   <li>{@link AsyncPlayerChatEvent} -&gt; {@link ServerEvents#CHAT}</li>
 *   <li>{@link PlayerJoinEvent}      -&gt; {@link ServerEvents#PLAYER_JOIN}</li>
 *   <li>{@link PlayerQuitEvent}      -&gt; {@link ServerEvents#PLAYER_LEAVE}</li>
 *   <li>{@link PlayerMoveEvent}      -&gt; {@link ServerEvents#PLAYER_MOVE}</li>
 * </ul>
 *
 * <p>Bukkit's {@code ignoreCancelled=false} (Spigot default) is intentionally
 * NOT honored: RDForward stops event dispatch on the first cancelling
 * listener, so later listeners never see cancelled events. When a plugin
 * registers a cancellable-event handler without {@code ignoreCancelled=true},
 * a one-time warning is logged per plugin name (see plan §4.4).
 */
public final class BukkitEventAdapter {

    private static final Logger LOG = Logger.getLogger("RDForward/BukkitBridge");

    /** Plugin names already warned about {@code ignoreCancelled=false}. */
    private static final Set<String> warnedPlugins = ConcurrentHashMap.newKeySet();

    /** Per-event-class binding entry — captures everything needed to
     *  dispatch a plugin-fired event back to its {@code @EventHandler}
     *  method via reflection. */
    private record Bound(Listener listener, Method method,
                         org.bukkit.event.EventPriority priority,
                         boolean ignoreCancelled) {}

    /** Listeners by exact event class declared on the {@code @EventHandler}
     *  parameter. Populated for every annotated method, regardless of
     *  whether the event type also has a matching ServerEvents callback —
     *  so plugin-fired custom events (LoginSecurity's
     *  {@code AuthActionEvent}, EssentialsX's per-command events, etc.)
     *  can be dispatched via {@link #dispatchPluginEvent}. */
    private static final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Bound>> DIRECT =
            new ConcurrentHashMap<>();

    private BukkitEventAdapter() {}

    /** Walk a listener's declared methods and wire every {@code @EventHandler} to ServerEvents. */
    public static void register(Listener listener) {
        register(listener, (String) null);
    }

    /**
     * Walk a listener's declared methods and wire every {@code @EventHandler}
     * to ServerEvents. {@code pluginName} is used to tag the one-time
     * {@code ignoreCancelled=false} warning; may be null for test harnesses.
     */
    public static void register(Listener listener, String pluginName) {
        for (Method m : listener.getClass().getDeclaredMethods()) {
            EventHandler eh = m.getAnnotation(EventHandler.class);
            if (eh == null) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length != 1) continue;
            m.setAccessible(true);

            Class<?> evtType = params[0];
            EventPriority prio = mapPriority(eh.priority());

            // Always remember the (event-class -> listener+method) binding
            // so plugin-fired events reach their handlers via
            // dispatchPluginEvent — independent of the ServerEvents-driven
            // path that translates real server actions into Bukkit events.
            DIRECT.computeIfAbsent(evtType, k -> new CopyOnWriteArrayList<>())
                    .add(new Bound(listener, m, eh.priority(), eh.ignoreCancelled()));

            if (isCancellable(evtType) && !eh.ignoreCancelled() && prio != EventPriority.MONITOR) {
                maybeWarnIgnoreCancelled(pluginName, listener);
            }

            if (evtType == BlockBreakEvent.class) {
                bindBlockBreak(listener, m, prio);
            } else if (evtType == BlockPlaceEvent.class) {
                bindBlockPlace(listener, m, prio);
            } else if (evtType == AsyncPlayerChatEvent.class) {
                bindChat(listener, m, prio);
            } else if (evtType == PlayerJoinEvent.class) {
                bindPlayerJoin(listener, m);
            } else if (evtType == PlayerQuitEvent.class) {
                bindPlayerQuit(listener, m);
            } else if (evtType == PlayerMoveEvent.class) {
                bindPlayerMove(listener, m);
            }
        }
    }

    /** Reset the warned-plugins set. Test-only. */
    public static void resetWarnedPlugins() {
        warnedPlugins.clear();
    }

    /**
     * Dispatch a plugin-fired Bukkit event to every {@link Listener}
     * whose {@code @EventHandler} parameter type is assignable from
     * {@code event}'s runtime class. Listeners run in Bukkit priority
     * order ({@code LOWEST} → {@code MONITOR}); listeners marked
     * {@code ignoreCancelled = true} are skipped after another
     * listener cancels the event.
     *
     * <p>Real Bukkit dispatches via the event's static
     * {@code HandlerList}; RDForward registers handlers directly here,
     * so we do the type lookup ourselves. Used by plugins that fire
     * their own events through {@code PluginManager.callEvent} —
     * notably LoginSecurity's {@code AuthActionEvent} on registration
     * and login.
     */
    public static void dispatchPluginEvent(Event event) {
        if (event == null) return;
        Class<?> evtClass = event.getClass();
        List<Bound> matched = new ArrayList<>();
        for (Map.Entry<Class<?>, CopyOnWriteArrayList<Bound>> e : DIRECT.entrySet()) {
            if (e.getKey().isAssignableFrom(evtClass)) {
                matched.addAll(e.getValue());
            }
        }
        matched.sort(Comparator.comparingInt(b -> b.priority.ordinal()));
        for (Bound b : matched) {
            if (b.ignoreCancelled && event.isCancelled()) continue;
            invokeListener(b.listener, b.method, event);
        }
    }

    /** Test-only — clear every registered listener and dedup state so
     *  successive tests boot cleanly. */
    public static void clearAll() {
        DIRECT.clear();
        SEEN_LISTENER_ERRORS.clear();
        warnedPlugins.clear();
    }

    private static boolean isCancellable(Class<?> evtType) {
        return evtType == BlockBreakEvent.class
                || evtType == BlockPlaceEvent.class
                || evtType == AsyncPlayerChatEvent.class
                || evtType == PlayerMoveEvent.class;
    }

    private static void maybeWarnIgnoreCancelled(String pluginName, Listener listener) {
        String name = pluginName != null ? pluginName : listener.getClass().getName();
        if (warnedPlugins.add(name)) {
            LOG.warning(
                    "[BukkitBridge] Note: Plugin '" + name + "' registers event handlers without ignoreCancelled=true.\n"
                    + "In RDForward, cancelled events are NOT delivered to non-MONITOR listeners (unlike Spigot).\n"
                    + "If this plugin needs to see cancelled events, it should use MONITOR priority.");
        }
    }

    private static EventPriority mapPriority(org.bukkit.event.EventPriority p) {
        return switch (p) {
            case LOWEST -> EventPriority.LOWEST;
            case LOW -> EventPriority.LOW;
            case NORMAL -> EventPriority.NORMAL;
            case HIGH -> EventPriority.HIGH;
            case HIGHEST -> EventPriority.HIGHEST;
            case MONITOR -> EventPriority.MONITOR;
        };
    }

    private static void bindBlockBreak(Listener l, Method m, EventPriority prio) {
        BlockBreakCallback cb = (name, x, y, z, blockType) -> {
            BlockBreakEvent ev = new BlockBreakEvent(BukkitPlayer.create(name), x, y, z, blockType);
            invokeListener(l, m, ev);
            return ev.isCancelled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.BLOCK_BREAK.register(prio, cb);
    }

    private static void bindBlockPlace(Listener l, Method m, EventPriority prio) {
        BlockPlaceCallback cb = (name, x, y, z, newBlockType) -> {
            BlockPlaceEvent ev = new BlockPlaceEvent(BukkitPlayer.create(name), x, y, z, newBlockType);
            invokeListener(l, m, ev);
            return ev.isCancelled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.BLOCK_PLACE.register(prio, cb);
    }

    private static void bindChat(Listener l, Method m, EventPriority prio) {
        ChatCallback cb = (name, message) -> {
            AsyncPlayerChatEvent ev = new AsyncPlayerChatEvent(BukkitPlayer.create(name), message);
            invokeListener(l, m, ev);
            return ev.isCancelled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.CHAT.register(prio, cb);
    }

    private static void bindPlayerJoin(Listener l, Method m) {
        PlayerJoinCallback cb = (name, version) -> {
            PlayerJoinEvent ev = new PlayerJoinEvent(BukkitPlayer.create(name));
            invokeListener(l, m, ev);
        };
        ServerEvents.PLAYER_JOIN.register(cb);
    }

    private static void bindPlayerQuit(Listener l, Method m) {
        PlayerLeaveCallback cb = name -> {
            PlayerQuitEvent ev = new PlayerQuitEvent(BukkitPlayer.create(name));
            invokeListener(l, m, ev);
        };
        ServerEvents.PLAYER_LEAVE.register(cb);
    }

    private static void bindPlayerMove(Listener l, Method m) {
        PlayerMoveCallback cb = (name, x, y, z, yaw, pitch) -> {
            double dx = x / 32.0;
            double dy = y / 32.0;
            double dz = z / 32.0;
            float fyaw = (yaw & 0xFF) * 360f / 256f;
            float fpitch = pitch * 360f / 256f;
            Location loc = new Location(null, dx, dy, dz, fyaw, fpitch);
            PlayerMoveEvent ev = new PlayerMoveEvent(BukkitPlayer.create(name), loc, loc);
            invokeListener(l, m, ev);
        };
        ServerEvents.PLAYER_MOVE.register(cb);
    }

    /** Dedup map for listener-throw warnings. Keys are
     *  {@code listenerClass#method:exceptionClass:message} so the same
     *  class+message combination only logs the first time it surfaces.
     *  Real Bukkit prints the full stack on every event-listener failure;
     *  RDForward's auto-generated stubs surface predictable per-player
     *  exceptions (e.g. LuckPerms's reflective {@code Field.get} on the
     *  Player Proxy) that would otherwise flood the log on every join. */
    private static final java.util.concurrent.ConcurrentHashMap<String, Boolean> SEEN_LISTENER_ERRORS =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static void invokeListener(Listener l, Method m, Object event) {
        try {
            m.invoke(l, event);
        } catch (InvocationTargetException ite) {
            // Real Bukkit logs listener failures and continues — one bad
            // plugin must not abort the calling event chain or the
            // connection that triggered it. Surface the full stack so
            // missing stub APIs are visible, but dedup per
            // (listener-method, cause-class, cause-message) so a
            // repeating per-player failure floods the log only once.
            Throwable cause = ite.getTargetException();
            String key = l.getClass().getName() + "#" + m.getName() + ":"
                    + cause.getClass().getName() + ":"
                    + (cause.getMessage() == null ? "" : cause.getMessage());
            if (SEEN_LISTENER_ERRORS.putIfAbsent(key, Boolean.TRUE) == null) {
                System.err.println("[Bukkit] Listener " + l.getClass().getName() + "."
                        + m.getName() + " threw " + cause.getClass().getName()
                        + (cause.getMessage() == null ? "" : ": " + cause.getMessage())
                        + " (further occurrences silenced)");
                cause.printStackTrace(System.err);
            }
        } catch (IllegalAccessException iae) {
            String key = l.getClass().getName() + "#" + m.getName() + ":illegal-access";
            if (SEEN_LISTENER_ERRORS.putIfAbsent(key, Boolean.TRUE) == null) {
                System.err.println("[Bukkit] Cannot invoke " + l.getClass().getName() + "."
                        + m.getName() + ": " + iae.getMessage()
                        + " (further occurrences silenced)");
                iae.printStackTrace(System.err);
            }
        }
    }
}

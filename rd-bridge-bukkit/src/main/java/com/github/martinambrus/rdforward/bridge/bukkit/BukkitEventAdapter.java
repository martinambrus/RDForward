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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
            BlockBreakEvent ev = new BlockBreakEvent(new Player(name), x, y, z, blockType);
            invokeListener(l, m, ev);
            return ev.isCancelled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.BLOCK_BREAK.register(prio, cb);
    }

    private static void bindBlockPlace(Listener l, Method m, EventPriority prio) {
        BlockPlaceCallback cb = (name, x, y, z, newBlockType) -> {
            BlockPlaceEvent ev = new BlockPlaceEvent(new Player(name), x, y, z, newBlockType);
            invokeListener(l, m, ev);
            return ev.isCancelled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.BLOCK_PLACE.register(prio, cb);
    }

    private static void bindChat(Listener l, Method m, EventPriority prio) {
        ChatCallback cb = (name, message) -> {
            AsyncPlayerChatEvent ev = new AsyncPlayerChatEvent(new Player(name), message);
            invokeListener(l, m, ev);
            return ev.isCancelled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.CHAT.register(prio, cb);
    }

    private static void bindPlayerJoin(Listener l, Method m) {
        PlayerJoinCallback cb = (name, version) -> {
            PlayerJoinEvent ev = new PlayerJoinEvent(new Player(name));
            invokeListener(l, m, ev);
        };
        ServerEvents.PLAYER_JOIN.register(cb);
    }

    private static void bindPlayerQuit(Listener l, Method m) {
        PlayerLeaveCallback cb = name -> {
            PlayerQuitEvent ev = new PlayerQuitEvent(new Player(name));
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
            PlayerMoveEvent ev = new PlayerMoveEvent(new Player(name), loc, loc);
            invokeListener(l, m, ev);
        };
        ServerEvents.PLAYER_MOVE.register(cb);
    }

    private static void invokeListener(Listener l, Method m, Object event) {
        try {
            m.invoke(l, event);
        } catch (InvocationTargetException ite) {
            throw new RuntimeException(
                    "Bukkit listener " + l.getClass().getName() + "." + m.getName() + " threw",
                    ite.getTargetException());
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(
                    "Cannot invoke " + l.getClass().getName() + "." + m.getName(), iae);
        }
    }
}

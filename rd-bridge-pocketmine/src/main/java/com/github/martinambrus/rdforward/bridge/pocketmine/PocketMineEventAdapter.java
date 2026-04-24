package com.github.martinambrus.rdforward.bridge.pocketmine;

import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.BlockBreakCallback;
import com.github.martinambrus.rdforward.api.event.server.BlockPlaceCallback;
import com.github.martinambrus.rdforward.api.event.server.ChatCallback;
import com.github.martinambrus.rdforward.api.event.server.PlayerJoinCallback;
import com.github.martinambrus.rdforward.api.event.server.PlayerLeaveCallback;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import pocketmine.event.Cancellable;
import pocketmine.event.Event;
import pocketmine.event.HandleEvent;
import pocketmine.event.Listener;
import pocketmine.event.block.BlockBreakEvent;
import pocketmine.event.block.BlockPlaceEvent;
import pocketmine.event.player.PlayerChatEvent;
import pocketmine.event.player.PlayerJoinEvent;
import pocketmine.event.player.PlayerQuitEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Reflectively scans a {@link Listener} for {@code @HandleEvent} methods
 * and wires each one to the matching rd-api {@link ServerEvents} entry.
 * The bridge-local {@code @HandleEvent} annotation replaces PocketMine's
 * PHPDoc {@code @priority} / {@code @ignoreCancelled} tags — port authors
 * move those attributes directly onto the Java method.
 *
 * <p>Supported mappings:
 * <ul>
 *   <li>{@link BlockBreakEvent}  -&gt; {@link ServerEvents#BLOCK_BREAK}</li>
 *   <li>{@link BlockPlaceEvent}  -&gt; {@link ServerEvents#BLOCK_PLACE}</li>
 *   <li>{@link PlayerChatEvent}  -&gt; {@link ServerEvents#CHAT}</li>
 *   <li>{@link PlayerJoinEvent}  -&gt; {@link ServerEvents#PLAYER_JOIN}</li>
 *   <li>{@link PlayerQuitEvent}  -&gt; {@link ServerEvents#PLAYER_LEAVE}</li>
 * </ul>
 *
 * <p>PocketMine's native {@code @ignoreCancelled true} means "only fire when
 * NOT cancelled". RDForward stops dispatch on cancellation so non-MONITOR
 * listeners never see a cancelled event. For MONITOR listeners,
 * {@code ignoreCancelled=true} skips invocation explicitly.
 */
public final class PocketMineEventAdapter {

    private static final Logger LOG = Logger.getLogger("RDForward/PocketMineBridge");

    private PocketMineEventAdapter() {}

    public static void register(Listener listener, String pluginName) {
        for (Method m : listener.getClass().getDeclaredMethods()) {
            HandleEvent he = m.getAnnotation(HandleEvent.class);
            if (he == null) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) {
                LOG.warning("[PocketMineBridge] skipping @HandleEvent method with bad signature: "
                        + listener.getClass().getName() + "." + m.getName());
                continue;
            }
            m.setAccessible(true);

            Class<?> evtType = params[0];
            EventPriority prio = mapPriority(he.priority());
            boolean ignoreCancelled = he.ignoreCancelled();

            if (evtType == BlockBreakEvent.class) {
                bindBlockBreak(listener, m, prio, ignoreCancelled);
            } else if (evtType == BlockPlaceEvent.class) {
                bindBlockPlace(listener, m, prio, ignoreCancelled);
            } else if (evtType == PlayerChatEvent.class) {
                bindChat(listener, m, prio, ignoreCancelled);
            } else if (evtType == PlayerJoinEvent.class) {
                bindPlayerJoin(listener, m);
            } else if (evtType == PlayerQuitEvent.class) {
                bindPlayerQuit(listener, m);
            } else {
                LOG.fine("[PocketMineBridge] @HandleEvent on unsupported event type: " + evtType.getName()
                        + " — listener " + listener.getClass().getName() + "." + m.getName() + " ignored");
            }
        }
    }

    private static EventPriority mapPriority(HandleEvent.EventPriority p) {
        return switch (p) {
            case LOWEST -> EventPriority.LOWEST;
            case LOW -> EventPriority.LOW;
            case NORMAL -> EventPriority.NORMAL;
            case HIGH -> EventPriority.HIGH;
            case HIGHEST -> EventPriority.HIGHEST;
            case MONITOR -> EventPriority.MONITOR;
        };
    }

    private static void bindBlockBreak(Listener l, Method m, EventPriority prio, boolean ignoreCancelled) {
        BlockBreakCallback cb = (name, x, y, z, blockType) -> {
            BlockBreakEvent ev = new BlockBreakEvent(name, x, y, z, blockType);
            if (ignoreCancelled && ev.isCancelled()) return EventResult.PASS;
            invokeListener(l, m, ev);
            return ev.isCancelled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.BLOCK_BREAK.register(prio, cb);
    }

    private static void bindBlockPlace(Listener l, Method m, EventPriority prio, boolean ignoreCancelled) {
        BlockPlaceCallback cb = (name, x, y, z, blockType) -> {
            BlockPlaceEvent ev = new BlockPlaceEvent(name, x, y, z, blockType);
            if (ignoreCancelled && ev.isCancelled()) return EventResult.PASS;
            invokeListener(l, m, ev);
            return ev.isCancelled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.BLOCK_PLACE.register(prio, cb);
    }

    private static void bindChat(Listener l, Method m, EventPriority prio, boolean ignoreCancelled) {
        ChatCallback cb = (name, message) -> {
            PlayerChatEvent ev = new PlayerChatEvent(name, message);
            if (ignoreCancelled && ev.isCancelled()) return EventResult.PASS;
            invokeListener(l, m, ev);
            return ev.isCancelled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.CHAT.register(prio, cb);
    }

    private static void bindPlayerJoin(Listener l, Method m) {
        PlayerJoinCallback cb = (name, version) -> {
            PlayerJoinEvent ev = new PlayerJoinEvent(name);
            invokeListener(l, m, ev);
        };
        ServerEvents.PLAYER_JOIN.register(cb);
    }

    private static void bindPlayerQuit(Listener l, Method m) {
        PlayerLeaveCallback cb = name -> {
            PlayerQuitEvent ev = new PlayerQuitEvent(name);
            invokeListener(l, m, ev);
        };
        ServerEvents.PLAYER_LEAVE.register(cb);
    }

    private static void invokeListener(Listener l, Method m, Object event) {
        try {
            m.invoke(l, event);
        } catch (InvocationTargetException ite) {
            throw new RuntimeException(
                    "PocketMine listener " + l.getClass().getName() + "." + m.getName() + " threw",
                    ite.getTargetException());
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(
                    "Cannot invoke " + l.getClass().getName() + "." + m.getName(), iae);
        }
    }
}

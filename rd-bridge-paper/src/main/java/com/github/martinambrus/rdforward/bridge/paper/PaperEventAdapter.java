package com.github.martinambrus.rdforward.bridge.paper;

import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.ChatCallback;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Reflectively scans a listener for {@code @EventHandler} methods that
 * target Paper-specific event types (currently {@link AsyncChatEvent}) and
 * wires them to the corresponding rd-api events. Bukkit-shaped events are
 * delegated to {@link BukkitEventAdapter} — callers register Paper
 * listeners via both adapters.
 */
public final class PaperEventAdapter {

    private static final Logger LOG = Logger.getLogger("RDForward/PaperBridge");

    private PaperEventAdapter() {}

    public static void register(Listener listener, String pluginName) {
        BukkitEventAdapter.register(listener, pluginName);
        registerPaperOnly(listener, pluginName);
    }

    public static void registerPaperOnly(Listener listener, String pluginName) {
        for (Method m : listener.getClass().getDeclaredMethods()) {
            EventHandler eh = m.getAnnotation(EventHandler.class);
            if (eh == null) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length != 1) continue;
            m.setAccessible(true);
            Class<?> evtType = params[0];
            EventPriority prio = mapPriority(eh.priority());
            if (evtType == AsyncChatEvent.class) {
                bindAsyncChat(listener, m, prio);
            }
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

    private static void bindAsyncChat(Listener listener, Method method, EventPriority prio) {
        ChatCallback cb = (name, message) -> {
            AsyncChatEvent event = new AsyncChatEvent();
            try {
                method.invoke(listener, event);
            } catch (InvocationTargetException ite) {
                throw new RuntimeException(
                        "Paper listener " + listener.getClass().getName() + "." + method.getName() + " threw",
                        ite.getTargetException());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(
                        "Cannot invoke " + listener.getClass().getName() + "." + method.getName(), iae);
            }
            return event.isCancelled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.CHAT.register(prio, cb);
        LOG.fine("[PaperBridge] Registered AsyncChatEvent listener " + listener.getClass().getName());
    }
}

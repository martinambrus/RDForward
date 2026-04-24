package com.github.martinambrus.rdforward.api.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which mod registered which listener on a plain {@link Event} so
 * the mod loader can unregister them en masse on unload / hot-reload.
 *
 * <p>Installed by the mod loader once at startup via {@link #install()}.
 * During a mod's {@code onEnable(Server)} callback the loader wraps the
 * invocation in {@link #withOwner(String, Runnable)} — every listener the
 * mod registers on any {@code Event<T>} is tagged with its id. When the
 * mod unloads the loader calls {@link #unregisterAllForMod(String)} and
 * every tagged listener is removed without the mod having to clean up
 * manually.
 *
 * <p>{@link PrioritizedEvent} registers owners through its own
 * {@code register(priority, listener, modId)} path — those listeners do
 * not flow through this tracker. Both paths coexist.
 */
public final class EventOwnership {

    private EventOwnership() {}

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    /** modId -> list of (event, listener) pairs registered under that id. */
    private static final Map<String, List<Registration>> BY_MOD = new ConcurrentHashMap<>();

    /**
     * Install the ownership tracker on {@link Event#registrationHook}. Call
     * once at server startup, before any mod's onEnable. Safe to call
     * multiple times — subsequent calls are no-ops.
     */
    public static void install() {
        if (Event.registrationHook != null) return;
        Event.registrationHook = EventOwnership::track;
    }

    /**
     * Run {@code action} with {@code modId} as the current owner. Any
     * listener registered on a plain {@code Event<T>} during the action
     * is tagged with this id. Thread-local scope — reentrant calls stack
     * with last-one-wins.
     */
    public static void withOwner(String modId, Runnable action) {
        String previous = CURRENT.get();
        CURRENT.set(modId);
        try {
            action.run();
        } finally {
            if (previous == null) CURRENT.remove();
            else CURRENT.set(previous);
        }
    }

    /** @return the mod id currently on the thread, or {@code null} if no wrapper is active. */
    public static String currentOwner() {
        return CURRENT.get();
    }

    /** Unregister every plain-Event listener tagged with the given mod id. @return number removed. */
    public static int unregisterAllForMod(String modId) {
        List<Registration> regs = BY_MOD.remove(modId);
        if (regs == null) return 0;
        int removed = 0;
        for (Registration r : regs) {
            if (r.removeFromEvent()) removed++;
        }
        return removed;
    }

    /** Snapshot of registrations per mod — for admin tooling. */
    public static Map<String, List<ListenerInfo>> snapshot() {
        Map<String, List<ListenerInfo>> out = new LinkedHashMap<>();
        for (Map.Entry<String, List<Registration>> e : BY_MOD.entrySet()) {
            List<ListenerInfo> list = new ArrayList<>(e.getValue().size());
            for (Registration r : e.getValue()) {
                list.add(new ListenerInfo(e.getKey(), EventPriority.NORMAL, r.listenerClassName()));
            }
            out.put(e.getKey(), Collections.unmodifiableList(list));
        }
        return Collections.unmodifiableMap(out);
    }

    private static void track(Event<?> event, Object listener) {
        String owner = CURRENT.get();
        if (owner == null) return;
        BY_MOD.computeIfAbsent(owner, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new Registration(event, listener));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private record Registration(Event<?> event, Object listener) {
        boolean removeFromEvent() {
            return ((Event) event).unregister(listener);
        }
        String listenerClassName() {
            return listener.getClass().getName();
        }
    }
}

package com.github.martinambrus.rdforward.api.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Event variant with dispatch priority, stop-on-cancel semantics, and
 * MONITOR pass-through. Extends {@link Event} so Fabric bridges can still
 * upcast and use the plain {@code register(T)} + {@code invoker()} API
 * with zero translation overhead.
 *
 * <p>Dispatch order: LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR.
 * <p>Within LOWEST..HIGHEST, any listener returning {@link EventResult#FAIL}
 * stops further non-MONITOR listeners. MONITOR listeners always run regardless
 * and their return values are ignored.
 *
 * <p>The {@link PrioritizedInvokerFactory} receives two lists: the dispatch
 * list (LOWEST..HIGHEST, in priority order) and the monitor list. Implementations
 * are responsible for iterating both and honoring the stop-on-cancel contract
 * against the dispatch list.
 *
 * @param <T> the callback interface type
 */
public class PrioritizedEvent<T> extends Event<T> {

    /** Owner id used when registration happens outside a mod context. */
    public static final String SERVER_OWNER = "__server__";

    /**
     * Weakly-held registry of every {@code PrioritizedEvent} instance ever
     * constructed. The mod loader iterates this on unload to sweep away
     * every listener a mod installed across all events with a single call.
     */
    private static final Set<PrioritizedEvent<?>> ALL =
            Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Optional gate the admin event manager installs so ops can disable
     * individual listeners at runtime. When {@code null} every listener
     * fires. Returning {@code false} makes {@link #rebuildInvoker()} skip
     * the listener on the next rebuild.
     */
    public static volatile BiPredicate<PrioritizedEvent<?>, ListenerInfo> dispatchGate;

    private final PrioritizedInvokerFactory<T> prioritizedFactory;

    private final Map<EventPriority, List<OwnedListener<T>>> byPriority =
            new EnumMap<>(EventPriority.class);

    protected PrioritizedEvent(T emptyInvoker, PrioritizedInvokerFactory<T> factory) {
        super(emptyInvoker, adaptFactory(factory));
        this.prioritizedFactory = factory;
        for (EventPriority p : EventPriority.values()) {
            byPriority.put(p, new CopyOnWriteArrayList<>());
        }
        synchronized (ALL) { ALL.add(this); }
    }

    /** Snapshot of every live {@code PrioritizedEvent} — for admin tooling. */
    public static List<PrioritizedEvent<?>> allInstances() {
        synchronized (ALL) { return new ArrayList<>(ALL); }
    }

    /**
     * Remove every listener owned by {@code modId} across every known
     * {@link PrioritizedEvent}. @return number of events that had at least
     * one listener removed.
     */
    public static int unregisterAllByOwner(String modId) {
        int count = 0;
        List<PrioritizedEvent<?>> snapshot;
        synchronized (ALL) { snapshot = new ArrayList<>(ALL); }
        for (PrioritizedEvent<?> e : snapshot) {
            if (e.unregisterByOwner(modId)) count++;
        }
        return count;
    }

    /** Adapter so the base Event<T> rebuildInvoker() path still works if called. */
    private static <T> Function<List<T>, T> adaptFactory(PrioritizedInvokerFactory<T> f) {
        return listeners -> f.create(listeners, List.of());
    }

    /** Create a new prioritized event. */
    public static <T> PrioritizedEvent<T> create(T emptyInvoker, PrioritizedInvokerFactory<T> factory) {
        return new PrioritizedEvent<>(emptyInvoker, factory);
    }

    /** Register at NORMAL priority, unknown mod owner (SERVER_OWNER). */
    @Override
    public void register(T listener) {
        register(EventPriority.NORMAL, listener, SERVER_OWNER);
    }

    /** Register at explicit priority, owner defaults to SERVER_OWNER. */
    public void register(EventPriority priority, T listener) {
        register(priority, listener, SERVER_OWNER);
    }

    /** Register at explicit priority + explicit owner. */
    public void register(EventPriority priority, T listener, String modId) {
        byPriority.get(priority).add(new OwnedListener<>(listener, modId));
        handlers.add(listener);
        rebuildInvoker();
    }

    @Override
    public boolean unregister(T listener) {
        boolean removed = false;
        for (List<OwnedListener<T>> bucket : byPriority.values()) {
            removed |= bucket.removeIf(o -> o.listener == listener);
        }
        handlers.remove(listener);
        if (removed) rebuildInvoker();
        return removed;
    }

    /** Remove every listener owned by the given mod. */
    public boolean unregisterByOwner(String modId) {
        boolean changed = false;
        for (List<OwnedListener<T>> bucket : byPriority.values()) {
            List<T> removedListeners = new ArrayList<>();
            bucket.removeIf(o -> {
                if (o.modId.equals(modId)) {
                    removedListeners.add(o.listener);
                    return true;
                }
                return false;
            });
            for (T l : removedListeners) handlers.remove(l);
            changed |= !removedListeners.isEmpty();
        }
        if (changed) rebuildInvoker();
        return changed;
    }

    /**
     * Find the priority bucket currently holding the listener matching
     * {@code modId} and {@code listenerClassName}. Returns {@code null} if
     * no match exists. Used by {@code EventManager} before applying a
     * priority override so the original priority can be recorded.
     */
    public EventPriority findPriority(String modId, String listenerClassName) {
        for (Map.Entry<EventPriority, List<OwnedListener<T>>> e : byPriority.entrySet()) {
            for (OwnedListener<T> o : e.getValue()) {
                if (matches(o, modId, listenerClassName)) return e.getKey();
            }
        }
        return null;
    }

    /**
     * Move the listener matching {@code modId} and {@code listenerClassName}
     * to {@code newPriority}. If {@code newPosition} is non-null, insert at
     * that index within the target bucket (clamped to bucket size); otherwise
     * append to the end. @return {@code true} if a listener was moved.
     */
    public boolean moveListener(String modId, String listenerClassName,
                                EventPriority newPriority, Integer newPosition) {
        OwnedListener<T> found = null;
        EventPriority fromPriority = null;
        for (Map.Entry<EventPriority, List<OwnedListener<T>>> e : byPriority.entrySet()) {
            for (OwnedListener<T> o : e.getValue()) {
                if (matches(o, modId, listenerClassName)) {
                    found = o;
                    fromPriority = e.getKey();
                    break;
                }
            }
            if (found != null) break;
        }
        if (found == null) return false;
        byPriority.get(fromPriority).remove(found);
        List<OwnedListener<T>> target = byPriority.get(newPriority);
        if (newPosition == null || newPosition >= target.size()) {
            target.add(found);
        } else {
            target.add(Math.max(0, newPosition), found);
        }
        rebuildInvoker();
        return true;
    }

    /**
     * Move the listener within its current priority bucket to {@code newPosition}.
     * No-op if the listener is not registered. @return {@code true} on success.
     */
    public boolean setPosition(String modId, String listenerClassName, int newPosition) {
        for (List<OwnedListener<T>> bucket : byPriority.values()) {
            for (OwnedListener<T> o : bucket) {
                if (matches(o, modId, listenerClassName)) {
                    bucket.remove(o);
                    int idx = Math.max(0, Math.min(newPosition, bucket.size()));
                    bucket.add(idx, o);
                    rebuildInvoker();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matches(OwnedListener<T> o, String modId, String listenerClassName) {
        if (!o.modId.equals(modId)) return false;
        return listenerClassName == null || listenerClassName.equals(o.listener.getClass().getName());
    }

    /** Snapshot of all listeners for admin tooling. */
    public List<ListenerInfo> getListenerInfo() {
        List<ListenerInfo> out = new ArrayList<>();
        for (Map.Entry<EventPriority, List<OwnedListener<T>>> e : byPriority.entrySet()) {
            for (OwnedListener<T> o : e.getValue()) {
                out.add(new ListenerInfo(o.modId, e.getKey(), o.listener.getClass().getName()));
            }
        }
        return out;
    }

    @Override
    public void clearListeners() {
        for (List<OwnedListener<T>> bucket : byPriority.values()) bucket.clear();
        super.clearListeners();
    }

    @Override
    protected void rebuildInvoker() {
        BiPredicate<PrioritizedEvent<?>, ListenerInfo> gate = dispatchGate;
        List<T> dispatch = new ArrayList<>();
        for (EventPriority p : EventPriority.values()) {
            if (p == EventPriority.MONITOR) continue;
            for (OwnedListener<T> o : byPriority.get(p)) {
                if (allowedByGate(gate, o, p)) dispatch.add(o.listener);
            }
        }
        List<T> monitor = new ArrayList<>();
        for (OwnedListener<T> o : byPriority.get(EventPriority.MONITOR)) {
            if (allowedByGate(gate, o, EventPriority.MONITOR)) monitor.add(o.listener);
        }

        if (dispatch.isEmpty() && monitor.isEmpty()) {
            invoker = emptyInvoker;
        } else {
            invoker = prioritizedFactory.create(dispatch, monitor);
        }
    }

    private boolean allowedByGate(BiPredicate<PrioritizedEvent<?>, ListenerInfo> gate,
                                  OwnedListener<T> o, EventPriority p) {
        if (gate == null) return true;
        return gate.test(this, new ListenerInfo(o.modId, p, o.listener.getClass().getName()));
    }

    /**
     * Force the invoker to rebuild. Called by the admin event manager
     * after toggling {@link #dispatchGate} so changes take effect on the
     * next {@link #invoker()} call.
     */
    public void refresh() {
        rebuildInvoker();
    }

    /**
     * Factory producing a combined invoker from dispatch + monitor lists.
     * The factory implementation is responsible for iterating both and for
     * honoring stop-on-cancel against the dispatch list.
     */
    @FunctionalInterface
    public interface PrioritizedInvokerFactory<T> {
        T create(List<T> dispatchListeners, List<T> monitorListeners);
    }

    /** Internal record of a listener with its owning mod id. */
    public static final class OwnedListener<T> {
        public final T listener;
        public final String modId;
        public OwnedListener(T listener, String modId) {
            this.listener = listener;
            this.modId = modId;
        }
    }
}

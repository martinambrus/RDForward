package com.github.martinambrus.rdforward.api.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Fabric-compatible event container. Each event holds a list of listeners
 * and an invoker that calls all listeners in registration order.
 *
 * <p>Signatures intentionally match Fabric's {@code net.fabricmc.fabric.api.event.Event}
 * so a Fabric-bridge Event<T> can delegate with zero translation overhead.
 *
 * <p>Usage (defining):
 * <pre>
 *   public static final Event&lt;ServerTickCallback&gt; SERVER_TICK = Event.create(
 *       () -&gt; {},
 *       listeners -&gt; () -&gt; { for (ServerTickCallback l : listeners) l.onServerTick(); }
 *   );
 * </pre>
 *
 * <p>Usage (listening): {@code SERVER_TICK.register(() -> {...});}
 * <p>Usage (firing):    {@code SERVER_TICK.invoker().onServerTick();}
 *
 * @param <T> the callback interface type
 */
public class Event<T> {

    /**
     * Optional hook the mod loader installs so it can tag listeners with
     * the registering mod id for automatic cleanup on unload. The hook is
     * called on every {@link #register(Object)} with {@code (this, listener)}.
     * When {@code null} (e.g. in unit tests or Fabric-only environments)
     * registration proceeds untracked.
     */
    public static volatile BiConsumer<Event<?>, Object> registrationHook;

    protected final List<T> handlers = new CopyOnWriteArrayList<>();
    protected final T emptyInvoker;
    protected final Function<List<T>, T> invokerFactory;
    protected volatile T invoker;

    protected Event(T emptyInvoker, Function<List<T>, T> invokerFactory) {
        this.emptyInvoker = emptyInvoker;
        this.invokerFactory = invokerFactory;
        this.invoker = emptyInvoker;
    }

    /** Create a new event. */
    public static <T> Event<T> create(T emptyInvoker, Function<List<T>, T> invokerFactory) {
        return new Event<>(emptyInvoker, invokerFactory);
    }

    /** Invoker that calls all registered listeners. Main firing entry point. */
    public T invoker() {
        return invoker;
    }

    /** Register a listener. Listeners are called in registration order. */
    public void register(T listener) {
        handlers.add(listener);
        rebuildInvoker();
        BiConsumer<Event<?>, Object> hook = registrationHook;
        if (hook != null) hook.accept(this, listener);
    }

    /**
     * Remove a previously-registered listener. Used for per-mod cleanup on
     * hot-reload. Returns true if the listener was found and removed.
     */
    public boolean unregister(T listener) {
        boolean removed = handlers.remove(listener);
        if (removed) rebuildInvoker();
        return removed;
    }

    /** Remove all listeners and reset invoker to the empty no-op. */
    public void clearListeners() {
        handlers.clear();
        invoker = emptyInvoker;
    }

    /** Current number of registered listeners. */
    public int listenerCount() {
        return handlers.size();
    }

    protected void rebuildInvoker() {
        invoker = handlers.isEmpty() ? emptyInvoker : invokerFactory.apply(handlers);
    }
}

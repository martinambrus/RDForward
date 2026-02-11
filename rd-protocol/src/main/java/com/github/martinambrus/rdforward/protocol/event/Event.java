package com.github.martinambrus.rdforward.protocol.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Fabric-compatible event container. Each event holds a list of listeners
 * and an invoker that calls all listeners in registration order.
 *
 * Usage (defining an event):
 * <pre>
 *   public static final Event&lt;ServerTickCallback&gt; SERVER_TICK = Event.create(
 *       () -> {},                              // empty invoker (no listeners)
 *       listeners -> () -> {                   // invoker factory
 *           for (ServerTickCallback l : listeners) l.onServerTick();
 *       }
 *   );
 * </pre>
 *
 * Usage (listening):
 * <pre>
 *   ServerEvents.SERVER_TICK.register(() -> System.out.println("tick!"));
 * </pre>
 *
 * Usage (firing):
 * <pre>
 *   ServerEvents.SERVER_TICK.invoker().onServerTick();
 * </pre>
 *
 * @param <T> the callback interface type
 */
public final class Event<T> {

    private final List<T> handlers = new CopyOnWriteArrayList<>();
    private final T emptyInvoker;
    private final Function<List<T>, T> invokerFactory;
    private volatile T invoker;

    private Event(T emptyInvoker, Function<List<T>, T> invokerFactory) {
        this.emptyInvoker = emptyInvoker;
        this.invokerFactory = invokerFactory;
        this.invoker = emptyInvoker;
    }

    /**
     * Create a new event.
     *
     * @param emptyInvoker   invoker to use when no listeners are registered (no-op)
     * @param invokerFactory creates a combined invoker from the current listener list
     * @return a new Event instance
     */
    public static <T> Event<T> create(T emptyInvoker, Function<List<T>, T> invokerFactory) {
        return new Event<>(emptyInvoker, invokerFactory);
    }

    /**
     * Get the invoker that calls all registered listeners.
     * This is the main entry point for firing the event.
     */
    public T invoker() {
        return invoker;
    }

    /**
     * Register a listener for this event. Listeners are called in registration order.
     */
    public void register(T listener) {
        handlers.add(listener);
        invoker = invokerFactory.apply(handlers);
    }

    /**
     * Returns the number of registered listeners.
     */
    public int listenerCount() {
        return handlers.size();
    }
}

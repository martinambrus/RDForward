package net.fabricmc.fabric.api.event;

import java.util.List;
import java.util.function.Function;

/**
 * Fabric-namespaced thin alias for rd-api's
 * {@link com.github.martinambrus.rdforward.api.event.Event}. Signatures match
 * exactly: {@code create(emptyInvoker, invokerFactory)}, {@code register}, and
 * {@code invoker()} all behave identically. This lets Fabric mods compile
 * against {@code net.fabricmc.fabric.api.event.Event} while dispatch is
 * actually carried by the rd-api event object at runtime.
 *
 * @param <T> callback interface type
 */
public class Event<T> extends com.github.martinambrus.rdforward.api.event.Event<T> {

    protected Event(T emptyInvoker, Function<List<T>, T> invokerFactory) {
        super(emptyInvoker, invokerFactory);
    }

    /** Create a new event. Matches upstream Fabric's factory signature. */
    public static <T> Event<T> create(T emptyInvoker, Function<List<T>, T> invokerFactory) {
        return new Event<>(emptyInvoker, invokerFactory);
    }
}

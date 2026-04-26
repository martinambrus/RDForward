// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event;

/**
 * Base class every Bukkit event extends. Stub: the bridge only carries
 * cancellation state + an asynchronous-firing flag.
 *
 * <p>The {@code Event(boolean)} constructor is the upstream Bukkit
 * contract; LoginSecurity's {@code AuthActionEvent} (and many other
 * plugins) call {@code super(true)} from a worker pool to mark the
 * event as async. Without this overload the JVM throws {@link
 * NoSuchMethodError} on every async event construction.
 */
public abstract class Event {
    private boolean cancelled;
    private final boolean async;

    public Event() {
        this(false);
    }

    public Event(boolean isAsync) {
        this.async = isAsync;
    }

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean value) { this.cancelled = value; }

    /** @return {@code true} if this event was constructed off the
     *  server tick thread (the constructor argument is the only
     *  source — RDForward does not auto-detect). */
    public boolean isAsynchronous() { return async; }
}

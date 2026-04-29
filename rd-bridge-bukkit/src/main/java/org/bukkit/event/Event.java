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

    /** Single shared HandlerList for every Bukkit event whose subclass
     *  doesn't carry its own. Real Bukkit allocates one per event class
     *  and dispatches listeners through it; RDForward fires events via
     *  {@code BukkitEventAdapter} instead, so the list is never read for
     *  dispatch — its only consumer is plugins that call
     *  {@code event.getHandlers().unregister(this)} for self-removal
     *  (Essentials's {@code SignPlayerListener} / {@code SignBlockListener}
     *  do this when sign support is disabled in config). The unregister
     *  call is a no-op on our stub; we just need a non-null list so the
     *  {@link NullPointerException} from the prior {@code return null}
     *  stubs goes away, and a real method on every event subclass so the
     *  {@link NoSuchMethodError} on subclasses without an override
     *  (BlockBreakEvent) goes away. */
    private static final HandlerList HANDLERS = new HandlerList();

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

    /** Default instance HandlerList. Event subclasses that auto-generated
     *  a {@code return null} override mask this; those need to be patched
     *  individually as plugins surface NPEs. Subclasses without an
     *  override (BlockBreakEvent and many others) inherit it cleanly. */
    public HandlerList getHandlers() { return HANDLERS; }

    /** Mirror of the per-event {@code static getHandlerList()} convention
     *  Bukkit plugins use for registration. Returns the shared list for
     *  the same reason as {@link #getHandlers()}. */
    public static HandlerList getHandlerList() { return HANDLERS; }
}

// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event;

/**
 * Base class every Bukkit event extends. Stub: the bridge only carries
 * cancellation state + provides a hook for subclasses to add their own
 * payload accessors.
 */
public abstract class Event {
    private boolean cancelled;

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean value) { this.cancelled = value; }
}

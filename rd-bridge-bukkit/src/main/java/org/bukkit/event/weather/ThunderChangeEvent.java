// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.weather;

/**
 * Bukkit's {@code ThunderChangeEvent}. Fired by
 * {@code BukkitWorldAdapter.setThundering} before any actual state
 * change so plugin listeners can veto via {@link #setCancelled(boolean)}.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ThunderChangeEvent extends WeatherEvent implements org.bukkit.event.Cancellable {

    private final boolean to;
    private final ThunderChangeEvent$Cause cause;
    private boolean cancelled;

    public ThunderChangeEvent(org.bukkit.World world, boolean to, ThunderChangeEvent$Cause cause) {
        super(world);
        this.to = to;
        this.cause = cause;
    }

    public ThunderChangeEvent(org.bukkit.World world, boolean to) {
        this(world, to, null);
    }

    public ThunderChangeEvent() {
        super(null);
        this.to = false;
        this.cause = null;
    }

    /** @return target thunder state — {@code true} for thundering, {@code false} for not. */
    public boolean toThunderState() { return to; }

    public ThunderChangeEvent$Cause getCause() { return cause; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    public org.bukkit.event.HandlerList getHandlers() { return null; }
    public static org.bukkit.event.HandlerList getHandlerList() { return null; }
}

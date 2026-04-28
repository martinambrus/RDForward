// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.weather;

/**
 * Bukkit's {@code WeatherChangeEvent}. Fired by
 * {@code BukkitWorldAdapter.setStorm} before any actual state change so
 * plugin listeners can veto via {@link #setCancelled(boolean)}.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class WeatherChangeEvent extends WeatherEvent implements org.bukkit.event.Cancellable {

    private final boolean to;
    private final WeatherChangeEvent$Cause cause;
    private boolean cancelled;

    public WeatherChangeEvent(org.bukkit.World world, boolean to, WeatherChangeEvent$Cause cause) {
        super(world);
        this.to = to;
        this.cause = cause;
    }

    public WeatherChangeEvent(org.bukkit.World world, boolean to) {
        this(world, to, null);
    }

    public WeatherChangeEvent() {
        super(null);
        this.to = false;
        this.cause = null;
    }

    /** @return target weather state — {@code true} for stormy, {@code false} for clear. */
    public boolean toWeatherState() { return to; }

    public WeatherChangeEvent$Cause getCause() { return cause; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    public org.bukkit.event.HandlerList getHandlers() { return null; }
    public static org.bukkit.event.HandlerList getHandlerList() { return null; }
}

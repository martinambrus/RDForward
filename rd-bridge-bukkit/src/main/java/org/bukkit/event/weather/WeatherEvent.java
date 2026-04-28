// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.weather;

/**
 * Base class for {@link WeatherChangeEvent} and {@link ThunderChangeEvent}.
 * Stores the world reference so listeners can scope their reaction
 * (e.g. only act on the overworld).
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class WeatherEvent extends org.bukkit.event.Event {

    private final org.bukkit.World world;

    protected WeatherEvent(org.bukkit.World world) { this.world = world; }
    protected WeatherEvent() { this.world = null; }

    public final org.bukkit.World getWorld() { return world; }
}

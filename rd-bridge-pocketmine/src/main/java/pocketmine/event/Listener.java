package pocketmine.event;

/**
 * Marker interface for PocketMine event listeners. An instance
 * implementing {@code Listener} is registered with the event manager;
 * the bridge scans it for {@code @HandleEvent} methods and wires each
 * to the matching rd-api {@code ServerEvents} entry.
 */
public interface Listener {
}

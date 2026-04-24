package pocketmine.event;

/**
 * Opt-in cancellation contract for PocketMine events. Mirrors
 * PocketMine-MP's {@code Cancellable} trait — events that mix this in
 * grant {@link Event#setCancelled} meaning; others remain no-ops.
 */
public interface Cancellable {
}

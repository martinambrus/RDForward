package pocketmine.event;

/**
 * Base class for every PocketMine event. PocketMine has no event priority
 * constants on the event itself — priority belongs to the listener and is
 * declared via {@link HandleEvent#priority()}. Subclasses that want to be
 * cancellable implement {@link Cancellable} — the default {@code Event}
 * is not cancellable and the bridge ignores calls to {@link #setCancelled}
 * on non-cancellable events.
 */
public class Event {

    private boolean cancelled;

    public boolean isCancelled() {
        return this instanceof Cancellable && cancelled;
    }

    public void setCancelled(boolean cancelled) {
        if (this instanceof Cancellable) this.cancelled = cancelled;
    }

    public void setCancelled() {
        setCancelled(true);
    }
}

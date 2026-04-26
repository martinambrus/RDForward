// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.minecraftforge.eventbus.api;

/**
 * Stub base class for Forge events. Every Forge event class extends this.
 * Provides the cancellation flag in a form simple enough for the bridge's
 * reflective dispatcher to observe — we don't honour the {@code @Cancelable}
 * annotation contract, we just expose the flag to all subclasses.
 *
 * <p>The {@link Event$Result} value type is generated as a sibling
 * top-level stub ({@code Event$Result.java}) rather than an inner enum so
 * the codegen drift gate stays clean. The literal {@code $} in the source
 * name is the binary name JVM uses for inner classes — mods compiled
 * against real Forge see the same binary signature on
 * {@link #getResult()} regardless.
 */
public class Event {

    private boolean canceled;

    public boolean isCanceled() { return canceled; }

    public void setCanceled(boolean canceled) { this.canceled = canceled; }

    public boolean isCancelable() { return true; }

    public Event$Result getResult() { return Event$Result.DEFAULT; }
}

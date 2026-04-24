package net.minecraftforge.eventbus.api;

/**
 * Stub base class for Forge events. Every Forge event class extends this.
 * Provides the cancellation flag in a form simple enough for the bridge's
 * reflective dispatcher to observe — we don't honour the {@code @Cancelable}
 * annotation contract, we just expose the flag to all subclasses.
 */
public class Event {

    private boolean canceled;

    public boolean isCanceled() { return canceled; }

    public void setCanceled(boolean canceled) { this.canceled = canceled; }

    public boolean isCancelable() { return true; }

    public Result getResult() { return Result.DEFAULT; }

    public enum Result { DENY, DEFAULT, ALLOW }
}

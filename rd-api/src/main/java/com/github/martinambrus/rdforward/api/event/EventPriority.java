package com.github.martinambrus.rdforward.api.event;

/**
 * Dispatch priority for prioritized event listeners.
 *
 * <p>Listeners run in enum order: LOWEST first, MONITOR last.
 * <p>If any listener at LOWEST..HIGHEST returns {@link EventResult#FAIL},
 * dispatch stops for remaining non-MONITOR listeners. MONITOR listeners
 * always run (even when cancelled) and their return values are ignored.
 *
 * <p>Mapped 1:1 to Bukkit's {@code EventPriority} for bridge simplicity.
 */
public enum EventPriority {
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHEST,
    MONITOR
}

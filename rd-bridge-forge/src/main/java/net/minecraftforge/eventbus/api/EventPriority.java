package net.minecraftforge.eventbus.api;

/**
 * Stub of Forge's priority enum. No {@code MONITOR} slot — Forge listeners
 * at {@link #HIGHEST} map to rd-api {@code EventPriority.HIGHEST}, not
 * {@code MONITOR}.
 */
public enum EventPriority {
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST
}

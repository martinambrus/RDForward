// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event;

/**
 * Matches Bukkit's dispatch order. Maps onto
 * {@link com.github.martinambrus.rdforward.api.event.EventPriority} inside
 * the bridge.
 */
public enum EventPriority {
    LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR
}

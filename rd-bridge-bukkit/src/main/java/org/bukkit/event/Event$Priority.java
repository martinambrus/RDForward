// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event;

/**
 * Pre-Bukkit-1.x {@code Event.Priority} enum. Replaced by
 * {@link EventPriority} in modern Bukkit; legacy plugins
 * (LogBlockQuestioner 0.02) still reference the inner-class form.
 *
 * <p>Constants use the original mixed-case spellings ({@code Normal})
 * because that's what plugin bytecode embeds in its constant pool.
 */
@SuppressWarnings({"unused"})
public enum Event$Priority {
    Lowest, Low, Normal, High, Highest, Monitor
}

// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

import org.bukkit.entity.Player;

/**
 * Common base for every Bukkit player-scoped event. Real plugins compile
 * against the upstream contract {@code AsyncPlayerChatEvent extends
 * PlayerEvent} and ship bytecode that hands the event to helper methods
 * typed against {@code PlayerEvent}; the JVM verifier rejects the call if
 * the runtime hierarchy disagrees.
 *
 * <p>The previous auto-generated stub lost the {@link Player} reference
 * and returned {@code null} from {@link #getPlayer()}; both are wrong.
 * This rewrite preserves the upstream {@code final} contract on
 * {@code getPlayer()} but actually returns the player passed in by the
 * subclass constructor.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class PlayerEvent extends org.bukkit.event.Event {

    private final Player player;

    protected PlayerEvent(Player player) {
        this.player = player;
    }

    protected PlayerEvent(Player player, boolean async) {
        this.player = player;
    }

    /** Backwards-compatible no-arg ctor preserved from the auto-generated
     *  stub. Subclasses that use it observe a {@code null} player —
     *  matches the previous behaviour and avoids breaking any code that
     *  was relying on the empty constructor. */
    protected PlayerEvent() {
        this.player = null;
    }

    public final Player getPlayer() {
        return player;
    }
}

// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

import org.bukkit.entity.Player;

/**
 * Stub of Bukkit's legacy {@code PlayerChatEvent}. Pre-1.3 plugins
 * (Essentials 2.8.x's {@code EssentialsPlayerListener.onPlayerChat})
 * listen to this event instead of {@link AsyncPlayerChatEvent} —
 * Essentials's mute / nick / format pipeline only fires here. The bridge
 * dispatches both events so legacy and modern listeners observe chat.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerChatEvent extends PlayerEvent implements org.bukkit.event.Cancellable {

    private String message;
    private String format;
    private boolean cancelled;
    // Mutable — Essentials's {@code /ignore} pipeline removes ignored
    // recipients from this set, the bridge diffs the resulting set
    // against the original online players to compute the exclusion list
    // it forwards to the rd-server chat broadcaster.
    private final java.util.Set<Player> recipients = new java.util.HashSet<>();

    public PlayerChatEvent(Player player, String message) {
        super(player);
        this.message = message;
        this.format = "<%1$s> %2$s";
    }

    public PlayerChatEvent(Player player, String message, String format, java.util.Set arg3) {
        super(player);
        this.message = message;
        this.format = format;
        if (arg3 != null) {
            for (Object o : arg3) {
                if (o instanceof Player p) recipients.add(p);
            }
        }
    }

    public PlayerChatEvent() { super(null); }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public void setPlayer(Player p) { /* noop — legacy mutator */ }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public java.util.Set<Player> getRecipients() { return recipients; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    public org.bukkit.event.HandlerList getHandlers() { return null; }
    public static org.bukkit.event.HandlerList getHandlerList() { return null; }
}

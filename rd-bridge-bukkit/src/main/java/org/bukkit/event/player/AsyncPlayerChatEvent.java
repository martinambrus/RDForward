// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Stub of Bukkit's {@code AsyncPlayerChatEvent}. Extends
 * {@link PlayerEvent} so plugin bytecode that hands the event to helpers
 * typed against {@code PlayerEvent} (LoginSecurity's
 * {@code PlayerListener.onPlayerChat}) verifies cleanly.
 */
public class AsyncPlayerChatEvent extends PlayerEvent implements org.bukkit.event.Cancellable {

    /** Bukkit's documented default format. EssentialsChat reads it via
     *  {@code event.getFormat()} as the seed for its own
     *  {@code {DISPLAYNAME}} / {@code {MESSAGE}} substitutions and writes
     *  the result back via {@code setFormat}. The bridge does not yet
     *  apply the format to the broadcast envelope (rd-server prepends a
     *  fixed {@code "username: "} prefix), but the field MUST exist or
     *  EssentialsChatPlayerListenerLowest crashes the chat dispatch
     *  with NoSuchMethodError on every message. */
    private static final String DEFAULT_FORMAT = "<%1$s> %2$s";

    private String message;
    private String format = DEFAULT_FORMAT;
    private boolean cancelled;
    // Mutable — modern plugins remove recipients here; bridge diffs
    // against the original online set to feed broadcastChat exclusions.
    private final Set<Player> recipients = new java.util.HashSet<>();

    public AsyncPlayerChatEvent(Player player, String message) {
        super(player);
        this.message = message;
    }

    public AsyncPlayerChatEvent(boolean async, Player player, String message,
                                Set<Player> recipients) {
        super(player, async);
        this.message = message;
        if (recipients != null) this.recipients.addAll(recipients);
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public Set<Player> getRecipients() { return recipients; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}

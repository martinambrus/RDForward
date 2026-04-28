package com.github.martinambrus.rdforward.api.event.server;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Per-call context for {@link ServerEvents#CHAT}. Holds the (possibly
 * rewritten) message and the set of recipient usernames that listeners
 * have excluded from delivery.
 *
 * <p>Bridges (Bukkit, PocketMine) populate {@link #excluded()} from
 * plugin-side recipient sets — e.g. Essentials's {@code /ignore} removes
 * the muted sender from {@code event.getRecipients()}, the bridge diffs
 * the original online set against what remains and records the removed
 * names here. Server connection handlers read the context after firing
 * {@link ServerEvents#CHAT} and pass the exclusion set to
 * {@link com.github.martinambrus.rdforward.api.server.PlayerManagerView#broadcastChat}.
 *
 * <p>Use via {@code try (ChatContext ctx = ChatContext.begin(...)) { ... }}.
 * The ThreadLocal is cleared on close so leaked references can't pollute
 * subsequent chat dispatches on the same thread.
 */
public final class ChatContext implements AutoCloseable {

    private static final ThreadLocal<ChatContext> CURRENT = new ThreadLocal<>();

    private String message;
    private final Set<String> excluded = new LinkedHashSet<>();

    private ChatContext(String message) { this.message = message; }

    public static ChatContext begin(String message) {
        ChatContext ctx = new ChatContext(message);
        CURRENT.set(ctx);
        return ctx;
    }

    /** @return current context, or {@code null} when no chat dispatch is in flight. */
    public static ChatContext current() { return CURRENT.get(); }

    public String message() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Set<String> excluded() { return excluded; }

    @Override public void close() { CURRENT.remove(); }
}

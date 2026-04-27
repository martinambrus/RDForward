// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-recipient set of player names that have been hidden via
 * {@code Player.hidePlayer(target)}. Names are stored lower-cased so
 * lookups match Bukkit's case-insensitive identity convention even when
 * the host hands us mixed-case usernames at the wire.
 *
 * <p>The Bukkit bridge installs an rd-api {@code PlayerVisibilityFilter}
 * that consults {@link #isHidden(String, String)}; rd-server's
 * {@code PlayerManager} consults that filter on every per-sender
 * broadcast (spawn, despawn, position, tab list add). Plugins like
 * VanishNoPacket running without ProtocolLib rely on this round-trip
 * to actually drop the vanished player from each recipient's view.
 *
 * <p>Lifetime is in-memory only — entries are cleared when the recipient
 * disconnects (the bridge clears them via {@link #clearRecipient(String)}
 * from the quit hook) and when the hidden sender disconnects
 * ({@link #clearSender(String)}). Reconnects start with an empty set,
 * matching real Bukkit's per-session semantics.
 */
public final class PlayerVisibilityRegistry {

    private PlayerVisibilityRegistry() {}

    private static final ConcurrentHashMap<String, Set<String>> HIDDEN_BY_RECIPIENT =
            new ConcurrentHashMap<>();

    private static String key(String name) {
        return name == null ? null : name.toLowerCase(Locale.ROOT);
    }

    /** Mark {@code senderName} as hidden from {@code recipientName}'s
     *  view. Subsequent visibility filter checks for this pair return
     *  {@code false}. */
    public static void hide(String recipientName, String senderName) {
        String r = key(recipientName);
        String s = key(senderName);
        if (r == null || s == null || r.equals(s)) return;
        HIDDEN_BY_RECIPIENT
                .computeIfAbsent(r, k -> ConcurrentHashMap.newKeySet())
                .add(s);
    }

    /** Reverse of {@link #hide}. Returns true if the pair was previously
     *  hidden. */
    public static boolean show(String recipientName, String senderName) {
        String r = key(recipientName);
        String s = key(senderName);
        if (r == null || s == null) return false;
        Set<String> set = HIDDEN_BY_RECIPIENT.get(r);
        if (set == null) return false;
        boolean removed = set.remove(s);
        if (set.isEmpty()) HIDDEN_BY_RECIPIENT.remove(r);
        return removed;
    }

    /** @return {@code true} if {@code senderName} is currently hidden
     *  from {@code recipientName} (i.e. {@code hide} was called and no
     *  matching {@code show} has run yet). Returns {@code false} for
     *  null inputs and for self-self pairs. */
    public static boolean isHidden(String recipientName, String senderName) {
        String r = key(recipientName);
        String s = key(senderName);
        if (r == null || s == null) return false;
        Set<String> set = HIDDEN_BY_RECIPIENT.get(r);
        return set != null && set.contains(s);
    }

    /** Drop every hidden-sender entry for {@code recipientName}. Called
     *  by the bridge when the recipient disconnects so a reconnect
     *  starts from a clean state. */
    public static void clearRecipient(String recipientName) {
        String r = key(recipientName);
        if (r != null) HIDDEN_BY_RECIPIENT.remove(r);
    }

    /** Remove {@code senderName} from every recipient's hidden set.
     *  Called when the hidden player disconnects so they're no longer
     *  tracked as "hidden from" anyone. */
    public static void clearSender(String senderName) {
        String s = key(senderName);
        if (s == null) return;
        HIDDEN_BY_RECIPIENT.values().forEach(set -> set.remove(s));
        HIDDEN_BY_RECIPIENT.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    /** Test-only — wipe all entries between tests. */
    public static void resetForTests() {
        HIDDEN_BY_RECIPIENT.clear();
    }

    /** Test-only — snapshot of the hidden-sender set for one recipient. */
    public static Set<String> hiddenFor(String recipientName) {
        String r = key(recipientName);
        if (r == null) return Collections.emptySet();
        Set<String> set = HIDDEN_BY_RECIPIENT.get(r);
        return set == null ? Collections.emptySet() : Set.copyOf(set);
    }
}

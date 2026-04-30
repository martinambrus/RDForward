package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * EssentialsChat reads {@code event.getFormat()} and writes back via
 * {@code setFormat} on every chat message. Pre-fix, the methods did
 * not exist and {@code EssentialsChatPlayerListenerLowest.onPlayerChat}
 * crashed every chat dispatch with NoSuchMethodError. The contract
 * verified here is the same shape Bukkit's API documents: a
 * non-null default format, getter / setter symmetry, and no
 * unexpected mutation of unrelated event fields.
 */
class AsyncPlayerChatEventFormatTest {

    @Test
    void newEventCarriesBukkitDefaultFormat() {
        AsyncPlayerChatEvent ev = new AsyncPlayerChatEvent(null, "hi");
        assertNotNull(ev.getFormat(),
                "default format must be non-null so plugins can read it without a guard");
        assertEquals("<%1$s> %2$s", ev.getFormat(),
                "default must match Bukkit's documented format string");
    }

    @Test
    void setFormatRoundTripsThroughGetFormat() {
        AsyncPlayerChatEvent ev = new AsyncPlayerChatEvent(null, "hi");
        ev.setFormat("[Chat] %1$s -> %2$s");
        assertEquals("[Chat] %1$s -> %2$s", ev.getFormat());
    }

    @Test
    void setFormatDoesNotMutateMessageOrCancelledFlag() {
        // Sibling fields must not regress when format mutates — the
        // listener pipeline relies on independent get/set semantics
        // for each property.
        AsyncPlayerChatEvent ev = new AsyncPlayerChatEvent(null, "original");
        ev.setFormat("[Whatever]");
        assertEquals("original", ev.getMessage());
        assertEquals(false, ev.isCancelled());
    }

    @Test
    void asyncCtorVariantAlsoExposesFormat() {
        // The (boolean async, Player, String, Set) constructor is the
        // shape modern Bukkit's chat dispatch uses; verify it surfaces
        // the same default format as the simpler ctor.
        AsyncPlayerChatEvent ev = new AsyncPlayerChatEvent(true, (Player) null, "hi",
                new java.util.HashSet<>());
        assertEquals("<%1$s> %2$s", ev.getFormat());
    }
}

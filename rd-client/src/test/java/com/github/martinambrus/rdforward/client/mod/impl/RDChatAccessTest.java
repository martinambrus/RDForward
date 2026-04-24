package com.github.martinambrus.rdforward.client.mod.impl;

import com.github.martinambrus.rdforward.client.ChatRenderer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RDChatAccess delegates to static ChatRenderer (for local messages) and to
 * RDClient.getInstance() (for outbound chat). The null-client branch keeps
 * sendMessage a safe no-op in test/singleplayer preview contexts.
 */
class RDChatAccessTest {

    private final RDChatAccess access = new RDChatAccess();

    @Test
    void addLocalMessageRoutesToChatRenderer() {
        // Seed a sentinel message; then verify the renderer now has content.
        // ChatRenderer's own trimming keeps messages bounded so this is safe.
        access.addLocalMessage("hello-from-test");
        assertFalse(ChatRenderer.isEmpty(), "local message must appear in ChatRenderer");
    }

    @Test
    void addLocalMessageNullIsIgnored() {
        assertDoesNotThrow(() -> access.addLocalMessage(null));
    }

    @Test
    void sendMessageNullIsIgnored() {
        // Without a connected RDClient, sendMessage should silently noop.
        // The null-message short-circuit runs before the client lookup.
        assertDoesNotThrow(() -> access.sendMessage(null));
    }

    @Test
    void sendMessageWithoutRDClientIsNoOp() {
        // RDClient.getInstance() is null in the unit-test JVM — the guard in
        // sendMessage is exactly what makes this test valid.
        assertDoesNotThrow(() -> access.sendMessage("hi"));
        assertTrue(true, "no exception thrown — null-client guard worked");
    }
}

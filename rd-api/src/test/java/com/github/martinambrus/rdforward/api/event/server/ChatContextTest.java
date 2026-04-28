package com.github.martinambrus.rdforward.api.event.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ChatContext}. Covers the ThreadLocal lifecycle
 * (no leakage after {@link ChatContext#close()}), message rewriting
 * round-trip, exclusion-set mutation, and thread isolation. The bridge
 * adapters write to this context after a chat listener fires; the
 * server-side connection handlers read it back to drive
 * {@code broadcastChat} exclusion + final-message routing.
 */
class ChatContextTest {

    @AfterEach
    void clearAfter() {
        // Defensive: a failing test mid-flow could leave the
        // ThreadLocal populated and pollute the next test.
        ChatContext stale = ChatContext.current();
        if (stale != null) stale.close();
    }

    @Test
    void beginInstallsThreadLocal() {
        try (ChatContext ctx = ChatContext.begin("hello")) {
            assertEquals(ctx, ChatContext.current());
            assertEquals("hello", ctx.message());
            assertTrue(ctx.excluded().isEmpty());
        }
        assertNull(ChatContext.current());
    }

    @Test
    void closeClearsThreadLocal() {
        ChatContext ctx = ChatContext.begin("x");
        ctx.close();
        assertNull(ChatContext.current());
    }

    @Test
    void messageRewritePropagates() {
        try (ChatContext ctx = ChatContext.begin("orig")) {
            ctx.setMessage("rewritten");
            assertEquals("rewritten", ChatContext.current().message());
        }
    }

    @Test
    void excludedAccumulatesNames() {
        try (ChatContext ctx = ChatContext.begin("m")) {
            ctx.excluded().add("alice");
            ctx.excluded().add("bob");
            assertEquals(2, ChatContext.current().excluded().size());
            assertTrue(ChatContext.current().excluded().contains("alice"));
            assertTrue(ChatContext.current().excluded().contains("bob"));
        }
    }

    @Test
    void currentIsNullOutsideBegin() {
        assertNull(ChatContext.current());
    }

    @Test
    void otherThreadSeesIndependentContext() throws Exception {
        try (ChatContext outer = ChatContext.begin("outer")) {
            outer.excluded().add("only-on-this-thread");

            Thread t = new Thread(() -> {
                // Sibling thread sees no inherited context.
                assertNull(ChatContext.current());
                try (ChatContext inner = ChatContext.begin("inner")) {
                    assertEquals("inner", ChatContext.current().message());
                    assertTrue(ChatContext.current().excluded().isEmpty());
                }
            });
            t.start();
            t.join();

            // Original context survived the sibling's lifecycle.
            assertEquals("outer", ChatContext.current().message());
            assertTrue(ChatContext.current().excluded().contains("only-on-this-thread"));
        }
    }
}

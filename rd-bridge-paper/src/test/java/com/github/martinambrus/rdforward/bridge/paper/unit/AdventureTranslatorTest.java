package com.github.martinambrus.rdforward.bridge.paper.unit;

import com.github.martinambrus.rdforward.bridge.paper.AdventureTranslator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * AdventureTranslator tests against the paper-api 26.1.2 stubs, whose
 * {@code LegacyComponentSerializer.legacySection()} returns {@code null}
 * and {@code Component.text(String)} also returns {@code null}. The
 * translator therefore degrades gracefully — these tests verify the
 * null-safe contract, not real serialization.
 */
class AdventureTranslatorTest {

    @Test
    void nullComponentReturnsEmptyString() {
        assertEquals("", AdventureTranslator.toPlainText(null));
    }

    @Test
    void nullPlainDoesNotThrow() {
        assertDoesNotThrow(() -> AdventureTranslator.toComponent(null));
    }

    @Test
    void plainStringDoesNotThrow() {
        assertDoesNotThrow(() -> AdventureTranslator.toComponent("hello"));
    }
}

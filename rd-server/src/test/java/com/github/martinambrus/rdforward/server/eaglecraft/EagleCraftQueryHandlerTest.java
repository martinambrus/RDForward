package com.github.martinambrus.rdforward.server.eaglecraft;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EagleCraftQueryHandler's JSON escaping logic.
 * Tests the private escapeJson method via reflection since it is
 * a pure function with no external dependencies.
 */
class EagleCraftQueryHandlerTest {

    private static String escapeJson(String input) {
        try {
            Method method = EagleCraftQueryHandler.class.getDeclaredMethod("escapeJson", String.class);
            method.setAccessible(true);
            return (String) method.invoke(null, input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke escapeJson", e);
        }
    }

    @Test
    void plainTextUnchanged() {
        assertEquals("Hello World", escapeJson("Hello World"));
    }

    @Test
    void escapesDoubleQuote() {
        assertEquals("say \\\"hello\\\"", escapeJson("say \"hello\""));
    }

    @Test
    void escapesBackslash() {
        assertEquals("path\\\\to\\\\file", escapeJson("path\\to\\file"));
    }

    @Test
    void escapesBackspace() {
        assertEquals("a\\bb", escapeJson("a\bb"));
    }

    @Test
    void escapesFormFeed() {
        assertEquals("a\\fb", escapeJson("a\fb"));
    }

    @Test
    void escapesNewline() {
        assertEquals("line1\\nline2", escapeJson("line1\nline2"));
    }

    @Test
    void escapesCarriageReturn() {
        assertEquals("a\\rb", escapeJson("a\rb"));
    }

    @Test
    void escapesTab() {
        assertEquals("a\\tb", escapeJson("a\tb"));
    }

    @Test
    void escapesControlCharsBelowSpace() {
        // NUL (0x00) should be escaped as \u0000
        assertEquals("a\\u0000b", escapeJson("a\u0000b"));
        // BEL (0x07)
        assertEquals("a\\u0007b", escapeJson("a\u0007b"));
        // ESC (0x1B)
        assertEquals("a\\u001bb", escapeJson("a\u001Bb"));
    }

    @Test
    void emptyStringUnchanged() {
        assertEquals("", escapeJson(""));
    }

    @Test
    void unicodePrintableCharsUnchanged() {
        assertEquals("Hello \u00e9\u00e8\u00ea", escapeJson("Hello \u00e9\u00e8\u00ea"));
    }

    @Test
    void multipleEscapesInOneString() {
        assertEquals("\\\"\\\\\\n\\t", escapeJson("\"\\\n\t"));
    }

    @Test
    void allAsciiPrintableUnchanged() {
        // Space (0x20) through tilde (0x7E), excluding " and \
        StringBuilder sb = new StringBuilder();
        for (char c = 0x20; c <= 0x7E; c++) {
            if (c != '"' && c != '\\') {
                sb.append(c);
            }
        }
        String input = sb.toString();
        assertEquals(input, escapeJson(input));
    }
}

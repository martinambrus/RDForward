package com.github.martinambrus.rdforward.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pins console-side colour-code stripping for chat log lines. The wire
 * broadcast keeps colours; only the {@code System.out} line goes through
 * {@link ChatConsoleFormat#strip}, so the Windows console renders
 * {@code [Chat] alpha: LIME} instead of {@code [Chat] alpha: ?6LIME?f}
 * when a plugin like Bananas wraps replacements in
 * {@code ChatColor.GOLD + word + ChatColor.WHITE}.
 */
class ChatConsoleFormatTest {

    @Test
    void stripsBananasReplacementShape() {
        assertEquals("LIME", ChatConsoleFormat.strip("§6LIME§f"));
    }

    @Test
    void stripsAllStandardColourAndFormatCodes() {
        // 0..9, a..f are colours; k..o + r are formatting (obfuscated,
        // bold, strikethrough, underline, italic, reset).
        for (char c : "0123456789abcdefABCDEFklmnoKLMNOrR".toCharArray()) {
            assertEquals("hello", ChatConsoleFormat.strip("§" + c + "hello"),
                    "code §" + c + " should be stripped");
        }
    }

    @Test
    void stripsHexCodeIntroducer() {
        // Modern hex codes are §x followed by six §0..§f pairs. The §x
        // itself plus the trailing colour pairs all get peeled off.
        assertEquals("hello", ChatConsoleFormat.strip("§x§a§a§b§b§c§chello"));
    }

    @Test
    void leavesPlainTextUntouched() {
        assertSame("plain", ChatConsoleFormat.strip("plain"),
                "no allocation when nothing to strip");
    }

    @Test
    void leavesLoneSectionSignUntouched() {
        // A trailing section sign with no follow-up char is not a code,
        // and a section sign followed by a non-format char is also kept.
        assertEquals("§", ChatConsoleFormat.strip("§"));
        assertEquals("a§Z", ChatConsoleFormat.strip("a§Z"));
    }

    @Test
    void nullPassThrough() {
        assertNull(ChatConsoleFormat.strip(null));
    }

    @Test
    void emptyPassThrough() {
        assertEquals("", ChatConsoleFormat.strip(""));
    }

    @Test
    void multipleEmbeddedCodes() {
        assertEquals("foo bar", ChatConsoleFormat.strip("§afoo §bbar"));
        assertEquals("foobar", ChatConsoleFormat.strip("§a§r§b§lfoo§rbar"));
    }
}

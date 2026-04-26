package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.DefaultConsoleCommandSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pins {@link DefaultConsoleCommandSender#stripColor(String)} so the
 * colour-coded LuckPerms banner ({@code §b ?3 __}) renders as readable
 * text on Windows consoles, where the section sign would otherwise
 * surface as a {@code ?} placeholder under cp1252.
 */
class ConsoleColorStripperTest {

    @Test
    void stripsSimpleSectionCodes() {
        String coloured = "§bLuckPerms§r v5.5.0";
        assertEquals("LuckPerms v5.5.0", DefaultConsoleCommandSender.stripColor(coloured));
    }

    @Test
    void stripsRgbForm() {
        // Bukkit RGB form is §x followed by six individually-prefixed hex
        // digits, i.e. seven §<char> pairs (14 chars) before user text.
        String coloured = "§x§F§F§A§B§0§0hello";
        // Each §<char> pair is removed, regardless of whether it is a
        // legacy code or part of the §x RGB sequence.
        assertEquals("hello", DefaultConsoleCommandSender.stripColor(coloured));
    }

    @Test
    void leavesPlainTextUntouched() {
        assertEquals("plain message",
                DefaultConsoleCommandSender.stripColor("plain message"));
    }

    @Test
    void emptyAndNullRoundTrip() {
        assertEquals("", DefaultConsoleCommandSender.stripColor(""));
        assertNull(DefaultConsoleCommandSender.stripColor(null));
    }

    @Test
    void trailingLoneSectionSignKept() {
        // No following char to strip — keep the lone § so the input
        // round-trips byte-perfect rather than truncating user content.
        assertEquals("end§", DefaultConsoleCommandSender.stripColor("end§"));
    }
}

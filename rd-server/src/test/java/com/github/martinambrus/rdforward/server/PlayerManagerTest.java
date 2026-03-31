package com.github.martinambrus.rdforward.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlayerManager.sanitizeUsername.
 */
class PlayerManagerTest {

    @Test
    void validUsernameUnchanged() {
        assertEquals("Steve", PlayerManager.sanitizeUsername("Steve"));
    }

    @Test
    void allowsUnderscoreHyphenDot() {
        assertEquals("a_b-c.d", PlayerManager.sanitizeUsername("a_b-c.d"));
    }

    @Test
    void stripsColonsAndCommas() {
        assertEquals("alice", PlayerManager.sanitizeUsername("al:i,ce"));
    }

    @Test
    void stripsNewlinesAndTabs() {
        assertEquals("alice", PlayerManager.sanitizeUsername("al\nice\t"));
    }

    @Test
    void truncatesTo32Chars() {
        String longName = "a".repeat(50);
        String result = PlayerManager.sanitizeUsername(longName);
        assertEquals(32, result.length());
    }

    @Test
    void emptyInputReturnsPlayer() {
        assertEquals("Player", PlayerManager.sanitizeUsername(""));
    }

    @Test
    void allInvalidCharsReturnsPlayer() {
        assertEquals("Player", PlayerManager.sanitizeUsername(":::,,,\n\n"));
    }

    @ParameterizedTest
    @CsvSource({
            "'test:inject', 'testinject'",
            "'<script>', 'script'",
            "'normal_player', 'normal_player'",
            "'123', '123'",
            "'a b c', 'abc'",
    })
    void variousInputs(String input, String expected) {
        assertEquals(expected, PlayerManager.sanitizeUsername(input));
    }

    @Test
    void mixedCasePreserved() {
        assertEquals("TestPlayer", PlayerManager.sanitizeUsername("TestPlayer"));
    }

    @Test
    void unicodeStripped() {
        assertEquals("Player", PlayerManager.sanitizeUsername("日本語"));
    }
}

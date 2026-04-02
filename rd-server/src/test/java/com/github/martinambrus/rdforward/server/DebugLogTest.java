package com.github.martinambrus.rdforward.server;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DebugLog — the zero-cost debug logging system.
 * Tests master toggle, category toggles, player filtering,
 * auto-off behavior, and name sanitization.
 */
class DebugLogTest {

    @BeforeEach
    void reset() {
        DebugLog.resetForTesting();
    }

    @AfterAll
    static void cleanup() {
        DebugLog.resetForTesting();
    }

    // === Master toggle ===

    @Test
    void disabledByDefault() {
        assertFalse(DebugLog.isEnabled());
        assertFalse(DebugLog.blocks());
        assertFalse(DebugLog.pos());
        assertFalse(DebugLog.chunks());
        assertFalse(DebugLog.packets());
    }

    @Test
    void enableTurnsOnGuards() {
        DebugLog.setEnabled(true);
        assertTrue(DebugLog.isEnabled());
        assertTrue(DebugLog.blocks());
        assertTrue(DebugLog.pos());
        assertTrue(DebugLog.chunks());
        // packets default to false
        assertFalse(DebugLog.packets());
    }

    @Test
    void disableAfterEnableTurnsOffGuards() {
        DebugLog.setEnabled(true);
        DebugLog.setEnabled(false);
        assertFalse(DebugLog.isEnabled());
        assertFalse(DebugLog.blocks());
    }

    // === Category toggles ===

    @Test
    void categoryTogglesWork() {
        DebugLog.setEnabled(true);

        DebugLog.setBlocks(false);
        assertFalse(DebugLog.blocks());
        assertTrue(DebugLog.pos());

        DebugLog.setPos(false);
        assertFalse(DebugLog.pos());
        assertTrue(DebugLog.chunks());

        DebugLog.setChunks(false);
        assertFalse(DebugLog.chunks());

        DebugLog.setPackets(true);
        assertTrue(DebugLog.packets());
    }

    @Test
    void categoryGuardsReturnFalseWhenMasterDisabled() {
        // Enable categories but keep master off
        DebugLog.setBlocks(true);
        DebugLog.setPos(true);
        DebugLog.setChunks(true);
        DebugLog.setPackets(true);

        assertFalse(DebugLog.blocks(), "blocks() should be false when master disabled");
        assertFalse(DebugLog.pos(), "pos() should be false when master disabled");
        assertFalse(DebugLog.chunks(), "chunks() should be false when master disabled");
        assertFalse(DebugLog.packets(), "packets() should be false when master disabled");
    }

    // === Verbose mode ===

    @Test
    void verboseDefaultOff() {
        assertFalse(DebugLog.isVerbose());
    }

    @Test
    void verboseToggle() {
        DebugLog.setVerbose(true);
        assertTrue(DebugLog.isVerbose());
        DebugLog.setVerbose(false);
        assertFalse(DebugLog.isVerbose());
    }

    // === Player filter ===

    @Test
    void noFilterLogsAllPlayers() {
        assertTrue(DebugLog.forPlayer("Alice"));
        assertTrue(DebugLog.forPlayer("Bob"));
        assertTrue(DebugLog.forPlayer(null));
    }

    @Test
    void addFilterRestrictsToNamedPlayers() {
        DebugLog.addPlayerFilter("Alice");
        assertTrue(DebugLog.forPlayer("Alice"));
        assertTrue(DebugLog.forPlayer("alice")); // case-insensitive
        assertFalse(DebugLog.forPlayer("Bob"));
        assertTrue(DebugLog.forPlayer(null)); // null always passes
    }

    @Test
    void addMultiplePlayersToFilter() {
        DebugLog.addPlayerFilter("Alice");
        DebugLog.addPlayerFilter("Bob");
        assertTrue(DebugLog.forPlayer("Alice"));
        assertTrue(DebugLog.forPlayer("Bob"));
        assertFalse(DebugLog.forPlayer("Charlie"));
    }

    @Test
    void removePlayerFromFilter() {
        DebugLog.addPlayerFilter("Alice");
        DebugLog.addPlayerFilter("Bob");
        DebugLog.removePlayerFilter("Alice");
        assertFalse(DebugLog.forPlayer("Alice"));
        assertTrue(DebugLog.forPlayer("Bob"));
    }

    @Test
    void removeLastPlayerClearsFilter() {
        DebugLog.addPlayerFilter("Alice");
        DebugLog.removePlayerFilter("Alice");
        // Filter should be null (log all) when empty
        assertTrue(DebugLog.forPlayer("Bob"));
    }

    @Test
    void clearFilterLogsAll() {
        DebugLog.addPlayerFilter("Alice");
        DebugLog.clearPlayerFilter();
        assertTrue(DebugLog.forPlayer("Bob"));
    }

    @Test
    void filterDescAllWhenNoFilter() {
        assertEquals("all", DebugLog.getPlayerFilterDesc());
    }

    @Test
    void filterDescShowsNames() {
        DebugLog.addPlayerFilter("Alice");
        String desc = DebugLog.getPlayerFilterDesc();
        assertTrue(desc.contains("alice"), "Should contain player name: " + desc);
    }

    @Test
    void addNullOrEmptyFilterIgnored() {
        DebugLog.addPlayerFilter(null);
        DebugLog.addPlayerFilter("");
        assertEquals("all", DebugLog.getPlayerFilterDesc());
    }

    // === Auto-off ===

    @Test
    void autoOffDescEmptyWhenDisabled() {
        assertEquals("", DebugLog.getAutoOffDesc());
    }

    @Test
    void autoOffDescNonEmptyWhenEnabled() {
        DebugLog.setEnabled(true);
        String desc = DebugLog.getAutoOffDesc();
        assertTrue(desc.contains("auto-off in"),
                "Should show auto-off timer: " + desc);
    }

    // === Name sanitization ===

    @Test
    void sanitizeNamePreservesNormal() {
        assertEquals("Steve", DebugLog.sanitizeName("Steve"));
    }

    @Test
    void sanitizeNameReplacesControlChars() {
        assertEquals("Al?ce", DebugLog.sanitizeName("Al\u0000ce"));
        assertEquals("Bo?b", DebugLog.sanitizeName("Bo\nb"));
    }

    @Test
    void sanitizeNameHandlesNull() {
        assertEquals("null", DebugLog.sanitizeName(null));
    }

    @Test
    void sanitizeNamePreservesSpacesAndPrintable() {
        assertEquals("Hello World!", DebugLog.sanitizeName("Hello World!"));
    }

    @Test
    void sanitizeNameReplacesDEL() {
        assertEquals("a?b", DebugLog.sanitizeName("a\u007Fb"));
    }

    // === resetForTesting ===

    @Test
    void resetClearsAllState() {
        DebugLog.setEnabled(true);
        DebugLog.setBlocks(false);
        DebugLog.setVerbose(true);
        DebugLog.addPlayerFilter("Alice");

        DebugLog.resetForTesting();

        assertFalse(DebugLog.isEnabled());
        assertTrue(DebugLog.isBlocks()); // default true
        assertFalse(DebugLog.isVerbose());
        assertEquals("all", DebugLog.getPlayerFilterDesc());
    }
}

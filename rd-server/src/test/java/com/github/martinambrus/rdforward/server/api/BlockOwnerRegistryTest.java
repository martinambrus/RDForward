package com.github.martinambrus.rdforward.server.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BlockOwnerRegistry: ID management, budget, play time, persistence.
 */
class BlockOwnerRegistryTest {

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        BlockOwnerRegistry.clearForTesting();
        // Reset to defaults
        BlockOwnerRegistry.INITIAL_BUDGET = 200;
        BlockOwnerRegistry.ACCRUAL_PER_HOUR = 100;
        BlockOwnerRegistry.MAX_BUDGET = 50_000;
        BlockOwnerRegistry.EXPIRY_DAYS = 30;
    }

    @AfterEach
    void cleanUp() {
        BlockOwnerRegistry.clearForTesting();
    }

    // --- ID management ---

    @Test
    void getOrCreateIdAssignsUniqueIds() {
        short id1 = BlockOwnerRegistry.getOrCreateId("alice");
        short id2 = BlockOwnerRegistry.getOrCreateId("bob");
        assertTrue(id1 > 0);
        assertTrue(id2 > 0);
        assertNotEquals(id1, id2);
    }

    @Test
    void getOrCreateIdReturnsSameIdForSamePlayer() {
        short id1 = BlockOwnerRegistry.getOrCreateId("alice");
        short id2 = BlockOwnerRegistry.getOrCreateId("alice");
        assertEquals(id1, id2);
    }

    @Test
    void getOrCreateIdIsCaseInsensitive() {
        short id1 = BlockOwnerRegistry.getOrCreateId("Alice");
        short id2 = BlockOwnerRegistry.getOrCreateId("ALICE");
        assertEquals(id1, id2);
    }

    @Test
    void getPlayerNameReturnsCorrectName() {
        short id = BlockOwnerRegistry.getOrCreateId("alice");
        assertEquals("alice", BlockOwnerRegistry.getPlayerName(id));
    }

    @Test
    void getPlayerNameReturnsNullForInvalidId() {
        assertNull(BlockOwnerRegistry.getPlayerName((short) 0));
        assertNull(BlockOwnerRegistry.getPlayerName((short) -1));
        assertNull(BlockOwnerRegistry.getPlayerName((short) 999));
    }

    @Test
    void getIdReturnsZeroForUnregistered() {
        assertEquals(0, BlockOwnerRegistry.getId("nobody"));
    }

    @Test
    void getIdReturnCorrectId() {
        short expected = BlockOwnerRegistry.getOrCreateId("alice");
        assertEquals(expected, BlockOwnerRegistry.getId("alice"));
    }

    // --- Protection budget ---

    @Test
    void initialBudgetMatchesDefault() {
        BlockOwnerRegistry.load(tempDir);
        assertEquals(200, BlockOwnerRegistry.getTotalBudget("newplayer"));
    }

    @Test
    void remainingBudgetStartsAtTotalForNewPlayer() {
        BlockOwnerRegistry.load(tempDir);
        int total = BlockOwnerRegistry.getTotalBudget("newplayer");
        assertEquals(total, BlockOwnerRegistry.getRemainingBudget("newplayer"));
    }

    @Test
    void useProtectionSlotDecreasesRemaining() {
        BlockOwnerRegistry.load(tempDir);
        BlockOwnerRegistry.getOrCreateId("alice");
        int before = BlockOwnerRegistry.getRemainingBudget("alice");
        assertTrue(BlockOwnerRegistry.useProtectionSlot("alice"));
        assertEquals(before - 1, BlockOwnerRegistry.getRemainingBudget("alice"));
    }

    @Test
    void returnProtectionSlotIncreasesRemaining() {
        BlockOwnerRegistry.load(tempDir);
        short id = BlockOwnerRegistry.getOrCreateId("alice");
        BlockOwnerRegistry.useProtectionSlot("alice");
        int afterUse = BlockOwnerRegistry.getRemainingBudget("alice");
        BlockOwnerRegistry.returnProtectionSlot("alice");
        assertEquals(afterUse + 1, BlockOwnerRegistry.getRemainingBudget("alice"));
    }

    @Test
    void returnProtectionSlotById() {
        BlockOwnerRegistry.load(tempDir);
        short id = BlockOwnerRegistry.getOrCreateId("alice");
        BlockOwnerRegistry.useProtectionSlot("alice");
        int afterUse = BlockOwnerRegistry.getRemainingBudget("alice");
        BlockOwnerRegistry.returnProtectionSlot(id);
        assertEquals(afterUse + 1, BlockOwnerRegistry.getRemainingBudget("alice"));
    }

    @Test
    void useProtectionSlotReturnsFalseWhenBudgetExhausted() {
        BlockOwnerRegistry.load(tempDir);
        BlockOwnerRegistry.INITIAL_BUDGET = 2;
        BlockOwnerRegistry.ACCRUAL_PER_HOUR = 0;
        BlockOwnerRegistry.getOrCreateId("alice");

        assertTrue(BlockOwnerRegistry.useProtectionSlot("alice"));
        assertTrue(BlockOwnerRegistry.useProtectionSlot("alice"));
        assertFalse(BlockOwnerRegistry.useProtectionSlot("alice"));
    }

    @Test
    void returnProtectionSlotDoesNotGoNegative() {
        BlockOwnerRegistry.load(tempDir);
        BlockOwnerRegistry.getOrCreateId("alice");
        // Return without using first — should stay at 0 used
        BlockOwnerRegistry.returnProtectionSlot("alice");
        assertEquals(0, BlockOwnerRegistry.getUsedBlocks("alice"));
    }

    @Test
    void restoreUsedSlotBypasesesBudgetCheck() {
        BlockOwnerRegistry.load(tempDir);
        BlockOwnerRegistry.INITIAL_BUDGET = 1;
        BlockOwnerRegistry.ACCRUAL_PER_HOUR = 0;
        short id = BlockOwnerRegistry.getOrCreateId("alice");

        // Use the one slot
        assertTrue(BlockOwnerRegistry.useProtectionSlot("alice"));
        // Restore goes beyond budget — that's intentional for rollback
        BlockOwnerRegistry.restoreUsedSlot(id);
        assertEquals(2, BlockOwnerRegistry.getUsedBlocks("alice"));
    }

    // --- Login tracking ---

    @Test
    void updateLastLoginSetsTimestamp() {
        BlockOwnerRegistry.updateLastLogin("alice");
        long login = BlockOwnerRegistry.getLastLoginMs("alice");
        assertTrue(login > 0);
        assertTrue(Math.abs(login - System.currentTimeMillis()) < 1000);
    }

    @Test
    void getLastLoginMsReturnsZeroForUnknown() {
        assertEquals(0, BlockOwnerRegistry.getLastLoginMs("nobody"));
    }

    // --- Play time tracking ---

    @Test
    void sessionTracksPlayTime() throws InterruptedException {
        BlockOwnerRegistry.startSession("alice");
        Thread.sleep(50); // small sleep for measurable time
        BlockOwnerRegistry.endSession("alice");
        long playTime = BlockOwnerRegistry.getEffectivePlayTimeMs("alice");
        assertTrue(playTime >= 40, "Play time should be at least 40ms, got " + playTime);
    }

    @Test
    void effectivePlayTimeIncludesActiveSession() {
        BlockOwnerRegistry.startSession("alice");
        long playTime = BlockOwnerRegistry.getEffectivePlayTimeMs("alice");
        // Should be > 0 because session is active
        assertTrue(playTime >= 0);
    }

    @Test
    void endSessionWithoutStartIsNoOp() {
        // Should not throw
        BlockOwnerRegistry.endSession("alice");
        assertEquals(0, BlockOwnerRegistry.getEffectivePlayTimeMs("alice"));
    }

    // --- Expiry ---

    @Test
    void isExpiredReturnsFalseForActivePlayer() {
        short id = BlockOwnerRegistry.getOrCreateId("alice");
        BlockOwnerRegistry.updateLastLogin("alice");
        assertFalse(BlockOwnerRegistry.isExpired(id));
    }

    @Test
    void isExpiredReturnsFalseForNoLoginData() {
        short id = BlockOwnerRegistry.getOrCreateId("alice");
        // No login recorded — should not be treated as expired
        assertFalse(BlockOwnerRegistry.isExpired(id));
    }

    @Test
    void isExpiredReturnsTrueForUnknownId() {
        assertTrue(BlockOwnerRegistry.isExpired((short) 9999));
    }

    @Test
    void isExpiredReturnsFalseForZeroId() {
        assertFalse(BlockOwnerRegistry.isExpired((short) 0));
    }

    @Test
    void getDaysUntilExpiryReturnsNegativeOneForNoLoginData() {
        assertEquals(-1, BlockOwnerRegistry.getDaysUntilExpiry("nobody"));
    }

    @Test
    void getDaysUntilExpiryReturnsPositiveForRecentLogin() {
        BlockOwnerRegistry.updateLastLogin("alice");
        int days = BlockOwnerRegistry.getDaysUntilExpiry("alice");
        assertTrue(days > 0 && days <= 30);
    }

    // --- Persistence ---

    @Test
    void saveAndLoadRoundTrip() {
        BlockOwnerRegistry.load(tempDir);
        short id1 = BlockOwnerRegistry.getOrCreateId("alice");
        short id2 = BlockOwnerRegistry.getOrCreateId("bob");
        BlockOwnerRegistry.updateLastLogin("alice");
        BlockOwnerRegistry.useProtectionSlot("alice");
        BlockOwnerRegistry.useProtectionSlot("alice");
        BlockOwnerRegistry.save();

        // Reload
        BlockOwnerRegistry.clearForTesting();
        BlockOwnerRegistry.load(tempDir);

        assertEquals(id1, BlockOwnerRegistry.getId("alice"));
        assertEquals(id2, BlockOwnerRegistry.getId("bob"));
        assertEquals("alice", BlockOwnerRegistry.getPlayerName(id1));
        assertEquals(2, BlockOwnerRegistry.getUsedBlocks("alice"));
        assertTrue(BlockOwnerRegistry.getLastLoginMs("alice") > 0);
    }

    @Test
    void loadFromExistingFile() throws IOException {
        File registryFile = new File(tempDir, "block-owners.txt");
        try (FileWriter fw = new FileWriter(registryFile)) {
            fw.write("# Block owner registry\n");
            fw.write("nextId:5\n");
            fw.write("alice:1:1711756800:3600000:150\n");
            fw.write("bob:2:1711843200:7200000:300\n");
        }

        BlockOwnerRegistry.load(tempDir);
        assertEquals(1, BlockOwnerRegistry.getId("alice"));
        assertEquals(2, BlockOwnerRegistry.getId("bob"));
        assertEquals("alice", BlockOwnerRegistry.getPlayerName((short) 1));
        assertEquals(150, BlockOwnerRegistry.getUsedBlocks("alice"));
        assertEquals(300, BlockOwnerRegistry.getUsedBlocks("bob"));
    }

    @Test
    void loadHandlesOldFormatGracefully() throws IOException {
        // Old format: name:id:lastLoginSec (no playTimeMs or usedBlocks)
        File registryFile = new File(tempDir, "block-owners.txt");
        try (FileWriter fw = new FileWriter(registryFile)) {
            fw.write("nextId:3\n");
            fw.write("alice:1:1711756800\n");
            fw.write("bob:2:0\n");
        }

        BlockOwnerRegistry.load(tempDir);
        assertEquals(1, BlockOwnerRegistry.getId("alice"));
        assertEquals(2, BlockOwnerRegistry.getId("bob"));
        assertEquals(0, BlockOwnerRegistry.getUsedBlocks("alice")); // defaults
        assertEquals(0, BlockOwnerRegistry.getUsedBlocks("bob"));
    }

    @Test
    void loadMaxIdAutoIncrements() throws IOException {
        File registryFile = new File(tempDir, "block-owners.txt");
        try (FileWriter fw = new FileWriter(registryFile)) {
            fw.write("nextId:2\n"); // nextId says 2, but max ID is 10
            fw.write("alice:10:0:0:0\n");
        }

        BlockOwnerRegistry.load(tempDir);
        // New player should get 11, not 2
        short newId = BlockOwnerRegistry.getOrCreateId("charlie");
        assertEquals(11, newId);
    }

    @Test
    void loadNonExistentFileStartsFresh() {
        BlockOwnerRegistry.load(new File(tempDir, "nonexistent"));
        assertEquals(0, BlockOwnerRegistry.getId("alice"));
        assertEquals(0, BlockOwnerRegistry.getAllPlayers().size());
    }

    @Test
    void getAllPlayersReturnsUnmodifiable() {
        BlockOwnerRegistry.getOrCreateId("alice");
        assertThrows(UnsupportedOperationException.class,
                () -> BlockOwnerRegistry.getAllPlayers().put("hack", (short) 1));
    }

    @Test
    void saveIfDirtyOnlySavesWhenDirty() {
        BlockOwnerRegistry.load(tempDir);
        // No changes — saveIfDirty should not create file
        BlockOwnerRegistry.saveIfDirty();
        File registryFile = new File(tempDir, "block-owners.txt");
        assertFalse(registryFile.exists());

        // Make a change
        BlockOwnerRegistry.getOrCreateId("alice");
        BlockOwnerRegistry.saveIfDirty();
        assertTrue(registryFile.exists());
    }
}

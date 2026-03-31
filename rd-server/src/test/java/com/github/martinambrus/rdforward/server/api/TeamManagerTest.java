package com.github.martinambrus.rdforward.server.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TeamManager: trust relationships, persistence, case handling.
 */
class TeamManagerTest {

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        TeamManager.clearForTesting();
    }

    @AfterEach
    void cleanUp() {
        TeamManager.clearForTesting();
    }

    // --- Add/Remove ---

    @Test
    void addTeammateReturnsTrueOnFirstAdd() {
        assertTrue(TeamManager.addTeammate("alice", "bob"));
    }

    @Test
    void addTeammateReturnsFalseOnDuplicate() {
        TeamManager.addTeammate("alice", "bob");
        assertFalse(TeamManager.addTeammate("alice", "bob"));
    }

    @Test
    void removeTeammateReturnsTrueWhenPresent() {
        TeamManager.addTeammate("alice", "bob");
        assertTrue(TeamManager.removeTeammate("alice", "bob"));
    }

    @Test
    void removeTeammateReturnsFalseWhenAbsent() {
        assertFalse(TeamManager.removeTeammate("alice", "bob"));
    }

    @Test
    void removeLastTeammateCleansUpOwnerEntry() {
        TeamManager.addTeammate("alice", "bob");
        TeamManager.removeTeammate("alice", "bob");
        assertTrue(TeamManager.getTeammates("alice").isEmpty());
    }

    // --- isTeammate / asymmetry ---

    @Test
    void isTeammateReturnsTrueForTrusted() {
        TeamManager.addTeammate("alice", "bob");
        assertTrue(TeamManager.isTeammate("alice", "bob"));
    }

    @Test
    void trustIsAsymmetric() {
        TeamManager.addTeammate("alice", "bob");
        // Alice trusts Bob, but Bob does NOT trust Alice
        assertTrue(TeamManager.isTeammate("alice", "bob"));
        assertFalse(TeamManager.isTeammate("bob", "alice"));
    }

    @Test
    void isTeammateReturnsFalseForUnknown() {
        assertFalse(TeamManager.isTeammate("alice", "charlie"));
    }

    // --- Case insensitivity ---

    @Test
    void namesAreCaseInsensitive() {
        TeamManager.addTeammate("Alice", "BOB");
        assertTrue(TeamManager.isTeammate("ALICE", "bob"));
        assertTrue(TeamManager.isTeammate("alice", "Bob"));
    }

    @Test
    void addDuplicateDifferentCaseReturnsFalse() {
        TeamManager.addTeammate("Alice", "Bob");
        assertFalse(TeamManager.addTeammate("alice", "bob"));
    }

    // --- getTeammates / getTeammatesList ---

    @Test
    void getTeammatesReturnsUnmodifiableSet() {
        TeamManager.addTeammate("alice", "bob");
        Set<String> members = TeamManager.getTeammates("alice");
        assertThrows(UnsupportedOperationException.class, () -> members.add("charlie"));
    }

    @Test
    void getTeammatesListReturnsSorted() {
        TeamManager.addTeammate("alice", "charlie");
        TeamManager.addTeammate("alice", "bob");
        TeamManager.addTeammate("alice", "dave");
        List<String> list = TeamManager.getTeammatesList("alice");
        assertEquals(List.of("bob", "charlie", "dave"), list);
    }

    @Test
    void getTeammatesListEmptyForUnknownOwner() {
        assertTrue(TeamManager.getTeammatesList("nobody").isEmpty());
    }

    // --- lastTruster ---

    @Test
    void setAndGetLastTruster() {
        TeamManager.setLastTruster("bob", "alice");
        assertEquals("alice", TeamManager.getLastTruster("bob"));
    }

    @Test
    void getLastTrusterReturnsNullWhenNeverSet() {
        assertNull(TeamManager.getLastTruster("nobody"));
    }

    @Test
    void lastTrusterIsCaseInsensitiveOnLookup() {
        TeamManager.setLastTruster("Bob", "Alice");
        assertEquals("Alice", TeamManager.getLastTruster("bob"));
    }

    // --- Persistence (save/load round-trip) ---

    @Test
    void saveAndLoadRoundTrip() {
        TeamManager.load(tempDir);
        TeamManager.addTeammate("alice", "bob");
        TeamManager.addTeammate("alice", "charlie");
        TeamManager.addTeammate("dave", "eve");
        TeamManager.saveIfDirty();

        // Reload
        TeamManager.clearForTesting();
        TeamManager.load(tempDir);

        assertTrue(TeamManager.isTeammate("alice", "bob"));
        assertTrue(TeamManager.isTeammate("alice", "charlie"));
        assertTrue(TeamManager.isTeammate("dave", "eve"));
        assertFalse(TeamManager.isTeammate("bob", "alice"));
    }

    @Test
    void loadFromExistingFile() throws IOException {
        File teamsFile = new File(tempDir, "teams.txt");
        try (FileWriter fw = new FileWriter(teamsFile)) {
            fw.write("# comment\n");
            fw.write("alice:bob,charlie\n");
            fw.write("dave:eve\n");
        }

        TeamManager.load(tempDir);
        assertTrue(TeamManager.isTeammate("alice", "bob"));
        assertTrue(TeamManager.isTeammate("alice", "charlie"));
        assertTrue(TeamManager.isTeammate("dave", "eve"));
        assertEquals(2, TeamManager.getTeammates("alice").size());
    }

    @Test
    void loadSkipsMalformedLines() throws IOException {
        File teamsFile = new File(tempDir, "teams.txt");
        try (FileWriter fw = new FileWriter(teamsFile)) {
            fw.write("nocolon\n");
            fw.write("empty:\n");
            fw.write("valid:member\n");
        }

        TeamManager.load(tempDir);
        assertTrue(TeamManager.isTeammate("valid", "member"));
        assertFalse(TeamManager.isTeammate("nocolon", ""));
        assertTrue(TeamManager.getTeammates("empty").isEmpty());
    }

    @Test
    void loadNonExistentDirectoryDoesNotThrow() {
        TeamManager.load(new File(tempDir, "nonexistent"));
        // Should just start with empty state
        assertTrue(TeamManager.getTeammates("anyone").isEmpty());
    }

    @Test
    void saveIfDirtyDoesNothingWhenClean() {
        TeamManager.load(tempDir);
        // No changes made
        TeamManager.saveIfDirty();
        File teamsFile = new File(tempDir, "teams.txt");
        assertFalse(teamsFile.exists());
    }

    @Test
    void removeAndSavePersistsRemoval() {
        TeamManager.load(tempDir);
        TeamManager.addTeammate("alice", "bob");
        TeamManager.addTeammate("alice", "charlie");
        TeamManager.saveIfDirty();

        // Remove bob
        TeamManager.removeTeammate("alice", "bob");
        TeamManager.saveIfDirty();

        // Reload and verify
        TeamManager.clearForTesting();
        TeamManager.load(tempDir);
        assertFalse(TeamManager.isTeammate("alice", "bob"));
        assertTrue(TeamManager.isTeammate("alice", "charlie"));
    }
}

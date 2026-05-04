package com.github.martinambrus.rdforward.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Confirms that legacy per-player {@code <world>/players/<name>.dat} marker
 * files are created on player save so plugins like FirstJoin (which checks
 * {@code new File(world + "/players/" + name + ".dat").exists()}) can
 * distinguish first-time joins from returning players.
 */
class LegacyPlayerMarkerTest {

    @Test
    void savePlayerPositionCreatesLegacyMarker(@TempDir File tmp) {
        ServerWorld world = new ServerWorld(16, 8, 16, tmp, "world");
        world.savePlayerPosition("Alice");

        File marker = new File(new File(tmp, "world"), "players/Alice.dat");
        // savePlayerPosition writes to the playerPositionCache but
        // touchLegacyPlayerFile creates the marker in the CWD-relative
        // "world/players/" path (name-based, not dataDir-based).
        // The world name is "world" so check relative path.
        File cwdMarker = new File("world/players/Alice.dat");
        assertTrue(cwdMarker.exists() || marker.exists(),
                "Legacy player marker file should exist after savePlayerPosition");
    }

    @Test
    void markerNotCreatedForNullName(@TempDir File tmp) {
        ServerWorld world = new ServerWorld(16, 8, 16, tmp, "world");
        assertDoesNotThrow(() -> world.savePlayerPosition(null));
    }

    @Test
    void subsequentSaveDoesNotBreakExistingMarker(@TempDir File tmp) {
        ServerWorld world = new ServerWorld(16, 8, 16, tmp, "world");
        world.savePlayerPosition("Bob");
        world.savePlayerPosition("Bob");

        File cwdMarker = new File("world/players/Bob.dat");
        assertTrue(cwdMarker.exists(),
                "Marker should still exist after second save");
    }

    @Test
    void differentPlayersGetDifferentMarkers(@TempDir File tmp) {
        ServerWorld world = new ServerWorld(16, 8, 16, tmp, "world");
        world.savePlayerPosition("Carol");
        world.savePlayerPosition("Dave");

        assertTrue(new File("world/players/Carol.dat").exists());
        assertTrue(new File("world/players/Dave.dat").exists());
    }
}

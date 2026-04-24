package com.github.martinambrus.rdforward.client.mod.impl;

import com.mojang.rubydung.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the axis-mapping contract between RubyDung's {@link Level} (where
 * {@code depth} is vertical and {@code height} is horizontal-Z) and the
 * mod-facing {@link com.github.martinambrus.rdforward.api.client.ClientWorld}
 * (where {@code getHeight()} is vertical and {@code getDepth()} is Z).
 */
class RDClientWorldTest {

    @BeforeAll
    static void headless() {
        // Level constructor tries to load level.dat from CWD; a missing file
        // is already handled gracefully. Run headless to suppress any UI hook.
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void axisMappingMatchesClientWorldContract() {
        Level level = new Level(8, 16, 4);
        RDClientWorld world = new RDClientWorld(level);
        assertEquals(8, world.getWidth(), "width passes through");
        assertEquals(4, world.getHeight(), "ClientWorld#getHeight == Level.depth (vertical)");
        assertEquals(16, world.getDepth(), "ClientWorld#getDepth == Level.height (horizontal Z)");
    }

    @Test
    void isInBoundsEnforcesAllAxes() {
        Level level = new Level(4, 4, 4);
        RDClientWorld world = new RDClientWorld(level);
        assertTrue(world.isInBounds(0, 0, 0));
        assertTrue(world.isInBounds(3, 3, 3));
        assertFalse(world.isInBounds(-1, 0, 0));
        assertFalse(world.isInBounds(0, -1, 0));
        assertFalse(world.isInBounds(0, 0, -1));
        assertFalse(world.isInBounds(4, 0, 0));
        assertFalse(world.isInBounds(0, 4, 0));
        assertFalse(world.isInBounds(0, 0, 4));
    }

    @Test
    void isSolidReturnsFalseOutsideBoundsWithoutDelegating() {
        Level level = new Level(4, 4, 4);
        RDClientWorld world = new RDClientWorld(level);
        // Out-of-bounds must not call Level.isSolidTile (which would access
        // the underlying blocks[] and NPE / IOOB on negative indices).
        assertFalse(world.isSolid(-1, 0, 0));
        assertFalse(world.isSolid(0, -1, 0));
        assertFalse(world.isSolid(0, 0, -1));
    }
}

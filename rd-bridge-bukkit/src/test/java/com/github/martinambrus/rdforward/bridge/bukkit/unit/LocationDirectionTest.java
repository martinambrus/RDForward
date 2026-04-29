package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pins {@link Location#getDirection()} — the unit vector used by
 * Essentials's {@code Commandfireball} to derive projectile velocity.
 * Pre-fix, the method did not exist and {@code /fireball} threw
 * {@link NoSuchMethodError}.
 *
 * <p>Yaw here is Bukkit-convention (0 = South, 90 = West, 180 = North,
 * 270 = East). {@code BukkitPlayer.Handler.getLocation} converts from
 * Classic yaw before the Location is constructed, so the formula here
 * must match real Bukkit's.
 */
class LocationDirectionTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    void yawZeroPitchZeroPointsTowardPositiveZ() {
        // Yaw 0 in Bukkit faces South (+Z); pitch 0 keeps it horizontal.
        Location loc = new Location(null, 0, 0, 0, 0f, 0f);
        Vector dir = loc.getDirection();
        assertEquals(0.0, dir.getX(), EPSILON);
        assertEquals(0.0, dir.getY(), EPSILON);
        assertEquals(1.0, dir.getZ(), EPSILON);
    }

    @Test
    void yaw180PointsTowardNegativeZ() {
        // Yaw 180 = facing North (-Z).
        Location loc = new Location(null, 0, 0, 0, 180f, 0f);
        Vector dir = loc.getDirection();
        assertEquals(0.0, dir.getX(), EPSILON);
        assertEquals(0.0, dir.getY(), EPSILON);
        assertEquals(-1.0, dir.getZ(), EPSILON);
    }

    @Test
    void yaw270PointsTowardPositiveX() {
        // Yaw 270 = facing East (+X).
        Location loc = new Location(null, 0, 0, 0, 270f, 0f);
        Vector dir = loc.getDirection();
        assertEquals(1.0, dir.getX(), EPSILON);
        assertEquals(0.0, dir.getY(), EPSILON);
        assertEquals(0.0, dir.getZ(), EPSILON);
    }

    @Test
    void pitchPositiveLooksDown() {
        // Bukkit pitch is positive-down. 90 degrees = straight down.
        Location loc = new Location(null, 0, 0, 0, 0f, 90f);
        Vector dir = loc.getDirection();
        assertEquals(0.0, dir.getX(), EPSILON);
        assertEquals(-1.0, dir.getY(), EPSILON);
        assertEquals(0.0, dir.getZ(), EPSILON);
    }

    @Test
    void resultingVectorIsUnitLength() {
        Location loc = new Location(null, 0, 0, 0, 45f, 30f);
        Vector dir = loc.getDirection();
        double mag = Math.sqrt(dir.getX() * dir.getX()
                + dir.getY() * dir.getY()
                + dir.getZ() * dir.getZ());
        assertEquals(1.0, mag, EPSILON);
    }

    @Test
    void setDirectionRoundTripsThroughGetDirection() {
        Location loc = new Location(null, 0, 0, 0);
        Location ret = loc.setDirection(new Vector(1.0, 0.0, 0.0));
        assertSame(loc, ret, "setDirection follows the fluent return contract");
        Vector dir = loc.getDirection();
        assertEquals(1.0, dir.getX(), EPSILON);
        assertEquals(0.0, dir.getY(), EPSILON);
        assertEquals(0.0, dir.getZ(), EPSILON);
    }
}

package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * VanishNoPacket calls {@code player.getLocation().add(0,1,0)} during
 * the toggle flow. The legacy mutate-and-return-self contract from real
 * Bukkit must be preserved or the client disconnects with
 * NoSuchMethodError on the legacy 3-double overload.
 */
class LocationArithmeticTest {

    @Test
    void addDoublesMutatesAndReturnsSelf() {
        Location loc = new Location(null, 1.0, 2.0, 3.0);
        Location ret = loc.add(0.5, 1.0, -2.0);
        assertSame(loc, ret, "add must return this for fluent chaining");
        assertEquals(1.5, loc.getX());
        assertEquals(3.0, loc.getY());
        assertEquals(1.0, loc.getZ());
    }

    @Test
    void subtractDoublesMutatesAndReturnsSelf() {
        Location loc = new Location(null, 5.0, 6.0, 7.0);
        Location ret = loc.subtract(1.0, 2.0, 3.0);
        assertSame(loc, ret);
        assertEquals(4.0, loc.getX());
        assertEquals(4.0, loc.getY());
        assertEquals(4.0, loc.getZ());
    }

    @Test
    void addLocationDelegatesToCoords() {
        Location a = new Location(null, 1.0, 1.0, 1.0);
        Location b = new Location(null, 2.0, 3.0, 4.0);
        a.add(b);
        assertEquals(3.0, a.getX());
        assertEquals(4.0, a.getY());
        assertEquals(5.0, a.getZ());
    }

    @Test
    void addNullLocationIsNoOp() {
        Location a = new Location(null, 1.0, 2.0, 3.0);
        a.add((Location) null);
        assertEquals(1.0, a.getX());
        assertEquals(2.0, a.getY());
        assertEquals(3.0, a.getZ());
    }

    @Test
    void addVectorDelegatesToCoords() {
        Location a = new Location(null, 0.0, 0.0, 0.0);
        a.add(new Vector(1.0, 2.0, 3.0));
        assertEquals(1.0, a.getX());
        assertEquals(2.0, a.getY());
        assertEquals(3.0, a.getZ());
    }

    @Test
    void distanceSquaredEuclidean() {
        Location a = new Location(null, 0.0, 0.0, 0.0);
        Location b = new Location(null, 3.0, 4.0, 0.0);
        assertEquals(25.0, a.distanceSquared(b));
        assertEquals(5.0, a.distance(b));
    }

    @Test
    void distanceToNullReturnsZero() {
        Location a = new Location(null, 1.0, 2.0, 3.0);
        assertEquals(0.0, a.distance(null));
    }
}

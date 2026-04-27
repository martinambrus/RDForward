package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * RDForward stores Y at eye-level (feet + 1.62) and yaw with the
 * Classic convention (0 = North). Bukkit plugins expect feet Y from
 * Player.getLocation() and Bukkit yaw (0 = South). The bridge is the
 * single conversion layer — without it, WorldEdit's //pos1/2 marked
 * positions ~2 blocks too high and //hpos1/2 ray-traced behind the
 * player.
 */
class BukkitPlayerLocationConversionTest {

    private static final double EYE_HEIGHT = (double) 1.62f;

    @AfterEach
    void wipeCache() {
        BukkitPlayer.evict("p-feet");
        BukkitPlayer.evict("p-eye");
        BukkitPlayer.evict("p-eye-height");
        BukkitPlayer.evict("p-yaw-zero");
        BukkitPlayer.evict("p-yaw-90");
        BukkitPlayer.evict("p-yaw-270");
        BukkitPlayer.evict("p-yaw-negative");
    }

    @Test
    void getLocationReturnsFeetYAndBukkitYaw() {
        Player p = newPlayer("p-feet", 10, 64, -5, 0f, 0f);
        Location loc = p.getLocation();
        assertNotNull(loc);
        assertEquals(10.0, loc.getX(), 1e-9);
        // Eye-level 64 -> feet 64 - 1.62.
        assertEquals(64.0 - EYE_HEIGHT, loc.getY(), 1e-3);
        assertEquals(-5.0, loc.getZ(), 1e-9);
        // Classic yaw 0 (North) -> Bukkit yaw 180 (still pointing North,
        // expressed under the 0=South convention).
        assertEquals(180.0f, loc.getYaw(), 1e-3);
        assertEquals(0.0f, loc.getPitch(), 1e-3);
    }

    @Test
    void getEyeLocationKeepsEyeYAndBukkitYaw() {
        // Eye location is what plugins use for ray traces — the Y must
        // stay at eye level while yaw still gets the +180 conversion
        // so the look vector points the right way.
        Player p = newPlayer("p-eye", 1, 50, 2, 90f, -10f);
        Location eye = p.getEyeLocation();
        assertNotNull(eye);
        assertEquals(50.0, eye.getY(), 1e-9, "eye Y must NOT subtract 1.62");
        assertEquals((90f + 180f) % 360f, eye.getYaw(), 1e-3);
        assertEquals(-10.0f, eye.getPitch(), 1e-3);
    }

    @Test
    void getEyeHeightReturnsConstant() {
        Player p = newPlayer("p-eye-height", 0, 0, 0, 0f, 0f);
        assertEquals(EYE_HEIGHT, ((Number) p.getEyeHeight()).doubleValue(), 1e-9);
    }

    @Test
    void classicYawZeroBecomesBukkitYaw180() {
        Player p = newPlayer("p-yaw-zero", 0, 0, 0, 0f, 0f);
        assertEquals(180f, p.getLocation().getYaw(), 1e-3);
    }

    @Test
    void classicYaw90BecomesBukkitYaw270() {
        Player p = newPlayer("p-yaw-90", 0, 0, 0, 90f, 0f);
        assertEquals(270f, p.getLocation().getYaw(), 1e-3);
    }

    @Test
    void classicYaw270BecomesBukkitYaw90() {
        Player p = newPlayer("p-yaw-270", 0, 0, 0, 270f, 0f);
        assertEquals(90f, p.getLocation().getYaw(), 1e-3);
    }

    @Test
    void classicYawNegativeNormalisesToPositive() {
        // yaw -90 + 180 = 90; in [0, 360) range.
        Player p = newPlayer("p-yaw-negative", 0, 0, 0, -90f, 0f);
        assertEquals(90f, p.getLocation().getYaw(), 1e-3);
    }

    private static Player newPlayer(String name, double x, double y, double z, float yaw, float pitch) {
        com.github.martinambrus.rdforward.api.world.Location loc =
                new com.github.martinambrus.rdforward.api.world.Location("stub-world", x, y, z, yaw, pitch);
        StubRdServer.StubRdPlayer backing = new StubRdServer.StubRdPlayer(name, loc);
        return BukkitPlayer.create(name, backing, null);
    }
}

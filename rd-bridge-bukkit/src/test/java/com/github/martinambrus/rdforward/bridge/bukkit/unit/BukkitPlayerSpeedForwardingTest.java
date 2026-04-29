package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.world.Location;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pins the contract that {@code Player.setFlySpeed} / {@code setWalkSpeed}
 * on the BukkitPlayer proxy forward through to the rd-api backing.
 * Essentials's {@code /speed} (and aliases {@code /flyspeed} /
 * {@code /wspeed}) call these — without forwarding, the Bukkit defaults
 * land on the proxy's no-op interceptor and the change never reaches
 * the client.
 */
class BukkitPlayerSpeedForwardingTest {

    @AfterEach
    void wipe() {
        BukkitPlayer.evict("ZathrusW");
        BukkitBridge.uninstall();
    }

    @Test
    void setFlySpeedForwardsToBacking() {
        StubRdServer rd = new StubRdServer();
        StubRdServer.StubRdPlayer rdPlayer = new StubRdServer.StubRdPlayer(
                "ZathrusW", new Location("stub-world", 0, 64, 0, 0f, 0f));
        rd.players.put("ZathrusW", rdPlayer);
        BukkitBridge.install(rd);

        Player p = BukkitPlayer.create("ZathrusW");
        p.setFlySpeed(0.4f);

        assertEquals(1, rdPlayer.flySpeedSets.size(),
                "setFlySpeed must dispatch exactly once to the rd-api backing");
        assertEquals(0.4f, rdPlayer.flySpeedSets.get(0), 1e-6f);
        assertEquals(0.4f, p.getFlySpeed(), 1e-6f,
                "getFlySpeed must read back the value just set");
    }

    @Test
    void setWalkSpeedForwardsToBacking() {
        StubRdServer rd = new StubRdServer();
        StubRdServer.StubRdPlayer rdPlayer = new StubRdServer.StubRdPlayer(
                "ZathrusW", new Location("stub-world", 0, 64, 0, 0f, 0f));
        rd.players.put("ZathrusW", rdPlayer);
        BukkitBridge.install(rd);

        Player p = BukkitPlayer.create("ZathrusW");
        p.setWalkSpeed(0.3f);

        assertEquals(1, rdPlayer.walkSpeedSets.size(),
                "setWalkSpeed must dispatch exactly once to the rd-api backing");
        assertEquals(0.3f, rdPlayer.walkSpeedSets.get(0), 1e-6f);
        assertEquals(0.3f, p.getWalkSpeed(), 1e-6f,
                "getWalkSpeed must read back the value just set");
    }

    @Test
    void outOfRangeFlySpeedThrows() {
        StubRdServer rd = new StubRdServer();
        StubRdServer.StubRdPlayer rdPlayer = new StubRdServer.StubRdPlayer(
                "ZathrusW", new Location("stub-world", 0, 64, 0, 0f, 0f));
        rd.players.put("ZathrusW", rdPlayer);
        BukkitBridge.install(rd);

        Player p = BukkitPlayer.create("ZathrusW");
        assertThrows(IllegalArgumentException.class, () -> p.setFlySpeed(2.5f));
        assertThrows(IllegalArgumentException.class, () -> p.setWalkSpeed(-1.5f));
    }

    @Test
    void getSpeedReturnsBukkitDefaultsBeforeBackingResolves() {
        // No backing wired: getFlySpeed/getWalkSpeed must surface Bukkit's
        // API defaults so plugins reading them pre-join don't see 0.
        BukkitBridge.install(new StubRdServer());
        Player p = BukkitPlayer.create("ZathrusW");
        assertEquals(0.1f, p.getFlySpeed(), 1e-6f);
        assertEquals(0.2f, p.getWalkSpeed(), 1e-6f);
    }
}

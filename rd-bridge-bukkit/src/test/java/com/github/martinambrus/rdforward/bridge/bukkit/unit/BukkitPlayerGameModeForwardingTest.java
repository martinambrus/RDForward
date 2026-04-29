package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.world.Location;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pins that {@code Player.setGameMode(GameMode)} on the BukkitPlayer
 * proxy forwards through to the rd-api backing as the right protocol
 * int (0=survival, 1=creative, 2=adventure, 3=spectator). Essentials's
 * {@code /gamemode} (and aliases {@code /gms}, {@code /gmc},
 * {@code /gma}, {@code /gmsp}) drive this — without forwarding, the
 * change never reaches the client and the proxy's
 * {@code defaultValue(void)} interceptor swallows the call.
 *
 * <p>Also pins that {@code getGameMode()} reads from the backing rather
 * than always returning the configured server gamemode, so plugin
 * bookkeeping after a switch reflects the new value.
 */
class BukkitPlayerGameModeForwardingTest {

    @AfterEach
    void wipe() {
        BukkitPlayer.evict("ZathrusW");
        BukkitBridge.uninstall();
    }

    private static StubRdServer.StubRdPlayer setUpBacking() {
        StubRdServer rd = new StubRdServer();
        StubRdServer.StubRdPlayer rdPlayer = new StubRdServer.StubRdPlayer(
                "ZathrusW", new Location("stub-world", 0, 64, 0, 0f, 0f));
        rd.players.put("ZathrusW", rdPlayer);
        BukkitBridge.install(rd);
        return rdPlayer;
    }

    @Test
    void survivalMapsToZero() {
        StubRdServer.StubRdPlayer rdPlayer = setUpBacking();
        Player p = BukkitPlayer.create("ZathrusW");
        p.setGameMode(GameMode.SURVIVAL);
        assertEquals(1, rdPlayer.gameModeSets.size());
        assertEquals(0, rdPlayer.gameModeSets.get(0));
    }

    @Test
    void creativeMapsToOne() {
        StubRdServer.StubRdPlayer rdPlayer = setUpBacking();
        Player p = BukkitPlayer.create("ZathrusW");
        p.setGameMode(GameMode.CREATIVE);
        assertEquals(1, rdPlayer.gameModeSets.get(0));
    }

    @Test
    void adventureMapsToTwo() {
        StubRdServer.StubRdPlayer rdPlayer = setUpBacking();
        Player p = BukkitPlayer.create("ZathrusW");
        p.setGameMode(GameMode.ADVENTURE);
        assertEquals(2, rdPlayer.gameModeSets.get(0));
    }

    @Test
    void spectatorMapsToThree() {
        StubRdServer.StubRdPlayer rdPlayer = setUpBacking();
        Player p = BukkitPlayer.create("ZathrusW");
        p.setGameMode(GameMode.SPECTATOR);
        assertEquals(3, rdPlayer.gameModeSets.get(0));
    }

    @Test
    void getGameModeReadsThroughToBacking() {
        StubRdServer.StubRdPlayer rdPlayer = setUpBacking();
        rdPlayer.gameMode = 0; // simulate post-clamp survival
        Player p = BukkitPlayer.create("ZathrusW");
        GameMode gm = p.getGameMode();
        assertNotNull(gm, "getGameMode contract: never null");
        assertEquals(GameMode.SURVIVAL, gm,
                "getGameMode must reflect the per-player backing, not just the server config");
    }

    @Test
    void roundTripCreativeReadback() {
        setUpBacking();
        Player p = BukkitPlayer.create("ZathrusW");
        p.setGameMode(GameMode.CREATIVE);
        assertEquals(GameMode.CREATIVE, p.getGameMode(),
                "setGameMode → getGameMode must round-trip the same constant");
    }
}

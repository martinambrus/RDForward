package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * EssentialsX 2.21.2's {@code AsyncTeleport.respawnNow} fires {@code
 * pluginManager.callEvent(new PlayerRespawnEvent(player, spawnLoc, false))}
 * during {@code /home}, and {@code EssentialsPlayerListener.onPlayerRespawn}
 * (registered via {@code Bukkit.getPluginManager().registerEvents}) reads
 * {@code event.getPlayer()} to look up the user before calling
 * {@code updateCompass}. The previous auto-generated stub forwarded
 * {@code null} to the {@code PlayerEvent} super-ctor, so the listener
 * received an event with {@code getPlayer() == null} — and the
 * {@code updateCompass(null)} call NPE'd on every {@code /home}.
 *
 * <p>This integration test exercises the full path: install the bridge,
 * register a {@link Listener} via {@link BukkitEventAdapter#register},
 * fire the event through {@link Bukkit#getPluginManager() callEvent},
 * and assert the listener observed the player + location it was given.
 */
class PlayerRespawnEventDispatchTest {

    private StubRdServer rd;

    @BeforeEach void setUp() {
        rd = new StubRdServer();
        BukkitBridge.install(rd);
        BukkitEventAdapter.clearAll();
    }

    @AfterEach void tearDown() {
        BukkitEventAdapter.clearAll();
        BukkitBridge.uninstall();
    }

    static final class CapturingRespawnListener implements Listener {
        Player capturedPlayer;
        Location capturedLocation;
        int hits;

        @EventHandler
        public void onRespawn(PlayerRespawnEvent e) {
            hits++;
            capturedPlayer = e.getPlayer();
            capturedLocation = e.getRespawnLocation();
        }
    }

    @Test
    void callEventDeliversPlayerAndLocationToRegisteredListener() {
        // Mirrors AsyncTeleport.respawnNow's call shape end-to-end:
        // construct the event with player+location, fire via the plugin
        // manager, listener reads getPlayer() / getRespawnLocation().
        CapturingRespawnListener listener = new CapturingRespawnListener();
        BukkitEventAdapter.register(listener, "essentials");

        Player player = BukkitPlayer.create("alice");
        Location loc = new Location(null, 100, 64, 100);
        PlayerRespawnEvent event = new PlayerRespawnEvent(player, loc, false);

        Bukkit.getPluginManager().callEvent(event);

        assertEquals(1, listener.hits, "listener must be invoked exactly once");
        assertNotNull(listener.capturedPlayer,
                "Essentials's updateCompass NPEs if event.getPlayer() returns null");
        assertSame(player, listener.capturedPlayer);
        assertSame(loc, listener.capturedLocation);
    }
}

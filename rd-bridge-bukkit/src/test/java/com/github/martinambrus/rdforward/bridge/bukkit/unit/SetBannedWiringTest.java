package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import com.github.martinambrus.rdforward.server.api.BanManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Essentials's /ban routes through legacy CB-1.x
 * {@code OfflinePlayer.setBanned(boolean)}; without it the command
 * NoSuchMethodErrors after the kick succeeds. Both the live BukkitPlayer
 * proxy (online target) and the offline-player stub (offline target)
 * must forward to the rd-server {@link BanManager} so the ban survives
 * across logins and disk restarts.
 */
class SetBannedWiringTest {

    @BeforeEach
    void clearBans() {
        // Defensive: a previous test might have left state in BanManager.
        for (String n : BanManager.getBannedPlayers().toArray(new String[0])) {
            BanManager.unbanPlayer(n);
        }
    }

    @AfterEach
    void clearBansAfter() {
        for (String n : BanManager.getBannedPlayers().toArray(new String[0])) {
            BanManager.unbanPlayer(n);
        }
    }

    @Test
    void onlinePlayerSetBannedTrueRoutesToBanManager() {
        Player p = makePlayer("ban-online");
        p.setBanned(true);
        assertTrue(BanManager.isPlayerBanned("ban-online"));
        // isBanned() must read back from the manager so isBanned reflects
        // the live state rather than a constant false.
        assertTrue(p.isBanned());
    }

    @Test
    void onlinePlayerSetBannedFalseUnbans() {
        Player p = makePlayer("ban-toggle");
        p.setBanned(true);
        assertTrue(p.isBanned());
        p.setBanned(false);
        assertFalse(BanManager.isPlayerBanned("ban-toggle"));
        assertFalse(p.isBanned());
    }

    @Test
    void offlineStubSetBannedRoutesToBanManager() {
        OfflinePlayer offline = invokeOfflinePlayerStub("ban-offline");
        offline.setBanned(true);
        assertTrue(BanManager.isPlayerBanned("ban-offline"));
        assertTrue(offline.isBanned());
    }

    @Test
    void offlineStubSetBannedFalseUnbans() {
        OfflinePlayer offline = invokeOfflinePlayerStub("ban-offline-2");
        offline.setBanned(true);
        offline.setBanned(false);
        assertFalse(BanManager.isPlayerBanned("ban-offline-2"));
        assertFalse(offline.isBanned());
    }

    @Test
    void onlineIsBannedReflectsExternalBanManagerMutation() {
        // Real flow: /ban target hits Player.setBanned, but the kick
        // gating on a later login uses BanManager.isPlayerBanned. Ensure
        // an out-of-band BanManager.banPlayer also flips the proxy view
        // so the reverse direction is consistent too.
        Player p = makePlayer("ban-extern");
        assertFalse(p.isBanned());
        BanManager.banPlayer("ban-extern");
        assertTrue(p.isBanned());
    }

    private static Player makePlayer(String name) {
        com.github.martinambrus.rdforward.api.world.Location loc =
                new com.github.martinambrus.rdforward.api.world.Location("stub", 0, 0, 0, 0f, 0f);
        StubRdServer.StubRdPlayer backing = new StubRdServer.StubRdPlayer(name, loc);
        return BukkitPlayer.create(name, backing, null);
    }

    /** Reach into ServerSupport's package-private {@code offlinePlayerStub}
     *  factory directly — this is the same Proxy the production
     *  {@code Server.getOfflinePlayer} default returns, so verifying its
     *  setBanned/isBanned wiring exercises the legitimate runtime path
     *  without instantiating the (legacy-patched, JDK-Proxy-incompatible)
     *  Server interface. */
    private static OfflinePlayer invokeOfflinePlayerStub(String name) {
        try {
            Class<?> support = Class.forName("org.bukkit.ServerSupport");
            Method m = support.getDeclaredMethod("offlinePlayerStub", String.class);
            m.setAccessible(true);
            return (OfflinePlayer) m.invoke(null, name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

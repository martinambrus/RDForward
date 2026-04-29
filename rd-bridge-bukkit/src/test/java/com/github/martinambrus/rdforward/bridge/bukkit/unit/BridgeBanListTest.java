package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.ban.BridgeBanList;
import com.github.martinambrus.rdforward.server.api.BanManager;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.BanList$Type;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the {@link BridgeBanList} routing introduced for Essentials's
 * {@code /banip}, {@code /unbanip}, {@code /ban}, and {@code /pardon}
 * commands. Pre-fix, {@code Server.getBanList(BanList.Type)} did not
 * exist; Essentials's {@code Commandunbanip} threw NoSuchMethodError on
 * dispatch.
 *
 * <p>The contract validated here:
 * <ul>
 *   <li>IP-typed pardon delegates to {@link BanManager#unbanIp}.</li>
 *   <li>NAME-typed pardon delegates to {@link BanManager#unbanPlayer}.</li>
 *   <li>{@code addBan / isBanned / getBanEntry} round-trip through the
 *       same flat-file backing that gates join-time enforcement.</li>
 * </ul>
 */
class BridgeBanListTest {

    @BeforeEach
    void clearBans() {
        for (String n : BanManager.getBannedPlayers().toArray(new String[0])) {
            BanManager.unbanPlayer(n);
        }
        for (String ip : BanManager.getBannedIps().toArray(new String[0])) {
            BanManager.unbanIp(ip);
        }
    }

    @AfterEach
    void clearBansAfter() {
        clearBans();
    }

    @Test
    void ipPardonRemovesIpFromBanManager() {
        BanManager.banIp("192.168.0.1");
        BanList ipList = new BridgeBanList(BanList$Type.IP);
        assertTrue(ipList.isBanned("192.168.0.1"));
        ipList.pardon("192.168.0.1");
        assertFalse(BanManager.isIpBanned("192.168.0.1"));
    }

    @Test
    void namePardonRemovesPlayerFromBanManager() {
        BanManager.banPlayer("Notch");
        BanList nameList = new BridgeBanList(BanList$Type.NAME);
        assertTrue(nameList.isBanned("Notch"));
        nameList.pardon("Notch");
        assertFalse(BanManager.isPlayerBanned("Notch"));
    }

    @Test
    void profileTypeFollowsNameSemantics() {
        // PROFILE was added in 1.20+ for UUID-keyed bans. RDForward does
        // not model offline UUIDs separately, so PROFILE folds into NAME
        // — verify the pardon still routes through the player file.
        BanManager.banPlayer("ProfileTarget");
        BanList profileList = new BridgeBanList(BanList$Type.PROFILE);
        profileList.pardon("ProfileTarget");
        assertFalse(BanManager.isPlayerBanned("ProfileTarget"));
    }

    @Test
    void addBanIpStoresInBanManager() {
        BanList ipList = new BridgeBanList(BanList$Type.IP);
        BanEntry entry = ipList.addBan("10.0.0.5", "test", (Date) null, "console");
        assertNotNull(entry);
        assertEquals("10.0.0.5", entry.getTarget());
        assertTrue(BanManager.isIpBanned("10.0.0.5"));
    }

    @Test
    void getBanEntryReturnsNullForUnbannedTarget() {
        BanList ipList = new BridgeBanList(BanList$Type.IP);
        assertNull(ipList.getBanEntry("untouched-ip"));
    }

    @Test
    void getBanEntriesReflectsLiveSet() {
        BanManager.banPlayer("EntryA");
        BanManager.banPlayer("EntryB");
        BanList nameList = new BridgeBanList(BanList$Type.NAME);
        // BanManager stores names lower-cased; size should reflect what's there.
        assertEquals(2, nameList.getBanEntries().size());
    }

    @Test
    void banEntryRemovePardonsThroughManager() {
        BanManager.banIp("172.16.0.1");
        BanList ipList = new BridgeBanList(BanList$Type.IP);
        BanEntry entry = ipList.getBanEntry("172.16.0.1");
        assertNotNull(entry);
        entry.remove();
        assertFalse(BanManager.isIpBanned("172.16.0.1"));
    }
}

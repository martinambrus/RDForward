package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.world.Location;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import com.github.martinambrus.rdforward.server.api.PermissionManager;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that an OP player resolves arbitrary {@code hasPermission} nodes
 * to {@code true} through the BukkitPlayer proxy. Essentials's
 * {@code SuperpermsHandler} delegates straight to {@code base.hasPermission},
 * so the proxy must honour the OP flag for unregistered permission nodes —
 * otherwise OP-default commands like {@code /tree} get denied.
 */
class OpPlayerHasPermissionTest {

    @BeforeEach
    void seedOps(@TempDir Path dir) throws Exception {
        // Force PermissionManager to load from a clean tempdir, then add op.
        File data = dir.toFile();
        new File(data, "ops.txt").createNewFile();
        PermissionManager.load(data);
        PermissionManager.addOp("zathrusw", PermissionManager.MAX_OP_LEVEL);
    }

    @AfterEach
    void wipe() {
        BukkitPlayer.evict("ZathrusW");
        try { PermissionManager.removeOp("zathrusw"); } catch (Exception ignored) {}
        BukkitBridge.uninstall();
    }

    @Test
    void opPlayerHasArbitraryPermissionViaProxy() {
        StubRdServer rd = new StubRdServer();
        StubRdServer.StubRdPlayer rdPlayer =
                new StubRdServer.StubRdPlayer("ZathrusW",
                        new Location("stub-world", 0, 64, 0, 0f, 0f));
        rdPlayer.op = true;
        rd.players.put("ZathrusW", rdPlayer);
        BukkitBridge.install(rd);

        Player p = BukkitPlayer.create("ZathrusW");
        assertTrue(p.isOp(), "proxy.isOp must reflect rd-api backing.isOp");
        assertTrue(p.hasPermission("essentials.tree"),
                "OP must satisfy unregistered permission nodes via fallback to isOp");
    }
}

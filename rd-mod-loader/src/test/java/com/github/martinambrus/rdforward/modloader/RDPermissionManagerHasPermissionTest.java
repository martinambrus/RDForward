package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.permission.PermissionDefault;
import com.github.martinambrus.rdforward.api.permission.RegisteredPermission;
import com.github.martinambrus.rdforward.modloader.impl.RDPermissionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests for {@link RDPermissionManager#hasPermission} resolution
 * order: explicit grant beats registry default beats op fallback. The
 * server-layer static {@code PermissionManager} is reset to a temp
 * {@code ops.txt} per test so OP state is deterministic and does not leak
 * between cases.
 */
class RDPermissionManagerHasPermissionTest {

    private RDPermissionManager pm;

    @BeforeEach
    void resetOps(@TempDir Path dir) {
        // Point ops.txt at a per-test temp dir + clear any state from prior tests.
        com.github.martinambrus.rdforward.server.api.PermissionManager.load(dir.toFile());
        pm = new RDPermissionManager();
    }

    @Test
    void explicitGrantWinsRegardlessOfRegistry() {
        pm.getRegistry().register(new RegisteredPermission("custom.foo", PermissionDefault.FALSE));
        pm.grant("alice", "custom.foo");
        // FALSE default would deny — explicit grant wins.
        assertTrue(pm.hasPermission("alice", "custom.foo"));
    }

    @Test
    void registeredTrueGrantsEveryone() {
        pm.getRegistry().register(new RegisteredPermission("custom.public", PermissionDefault.TRUE));
        assertTrue(pm.hasPermission("alice", "custom.public"));
        assertTrue(pm.hasPermission("bob", "custom.public"));
    }

    @Test
    void registeredFalseDeniesEveryoneEvenOps() {
        com.github.martinambrus.rdforward.server.api.PermissionManager.addOp("admin", 4);
        pm.getRegistry().register(new RegisteredPermission("custom.locked", PermissionDefault.FALSE));
        assertFalse(pm.hasPermission("admin", "custom.locked"));
        assertFalse(pm.hasPermission("alice", "custom.locked"));
    }

    @Test
    void registeredOpGrantsOpsOnly() {
        com.github.martinambrus.rdforward.server.api.PermissionManager.addOp("admin", 4);
        pm.getRegistry().register(new RegisteredPermission("custom.adminonly", PermissionDefault.OP));
        assertTrue(pm.hasPermission("admin", "custom.adminonly"));
        assertFalse(pm.hasPermission("alice", "custom.adminonly"));
    }

    @Test
    void registeredNotOpGrantsNonOpsOnly() {
        com.github.martinambrus.rdforward.server.api.PermissionManager.addOp("admin", 4);
        pm.getRegistry().register(new RegisteredPermission("custom.guestonly", PermissionDefault.NOT_OP));
        assertFalse(pm.hasPermission("admin", "custom.guestonly"));
        assertTrue(pm.hasPermission("alice", "custom.guestonly"));
    }

    @Test
    void unknownPermissionFallsBackToOpStatus() {
        com.github.martinambrus.rdforward.server.api.PermissionManager.addOp("admin", 4);
        // No registry entry → legacy behaviour: ops have it, others don't.
        assertTrue(pm.hasPermission("admin", "unknown.perm"));
        assertFalse(pm.hasPermission("alice", "unknown.perm"));
    }
}

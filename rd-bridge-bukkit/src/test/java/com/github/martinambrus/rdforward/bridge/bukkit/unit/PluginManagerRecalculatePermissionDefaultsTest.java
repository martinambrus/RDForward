package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.SimplePluginManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Pinning regression test for OpenWarp v1.1's command-handler vendored
 * library ({@code com.pneumaticraft.commandhandler}), which calls
 * {@code pm.recalculatePermissionDefaults(perm)} after every {@code
 * pm.addPermission(perm)}. RDForward delegates default-permission
 * resolution to its own {@code PermissionManager} so the recalc is a
 * logged no-op; the call site just needs to link cleanly so
 * {@code Command.addToParentPerms} no longer
 * {@link NoSuchMethodError}s mid-onEnable.
 */
class PluginManagerRecalculatePermissionDefaultsTest {

    @Test
    void defaultRecalculatePermissionDefaultsIsNoOp() {
        SimplePluginManager pm = new SimplePluginManager();
        Permission perm = new Permission("rdforward.test", PermissionDefault.TRUE);
        assertDoesNotThrow(() -> pm.recalculatePermissionDefaults(perm),
                "stub default must accept the call without throwing");
    }

    @Test
    void nullPermissionAccepted() {
        SimplePluginManager pm = new SimplePluginManager();
        assertDoesNotThrow(() -> pm.recalculatePermissionDefaults(null),
                "stub must be null-safe -- pre-1.x plugins occasionally pass null on edge paths");
    }
}

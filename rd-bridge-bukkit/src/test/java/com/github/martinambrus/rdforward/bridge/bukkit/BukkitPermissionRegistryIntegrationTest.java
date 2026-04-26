package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import com.github.martinambrus.rdforward.api.mod.ModManager;
import com.github.martinambrus.rdforward.api.permission.RegisteredPermission;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for the Bukkit permission registry bridging:
 *
 * <ul>
 *   <li>{@code Bukkit.getPluginManager().addPermission(new Permission(...))}
 *       lands in the rd-api {@link com.github.martinambrus.rdforward.api.permission.PermissionRegistry}
 *       and survives a {@code getPermission(name)} round-trip.</li>
 *   <li>{@code rd.getPermissionManager().hasPermission(...)} respects the
 *       registered default-grant rule (OP-only here) for non-granted players.</li>
 *   <li>{@code removePermission} clears both the registry and the Bukkit
 *       map.</li>
 *   <li>{@code isPluginEnabled} answers via {@link ModManager#isLoaded} when
 *       a ModManager is wired in, otherwise silently returns false.</li>
 * </ul>
 */
class BukkitPermissionRegistryIntegrationTest {

    private StubRdServer rd;

    @BeforeEach
    void install(@TempDir Path dir) {
        // Reset the static op store for the test so addOp/isOp behave deterministically.
        com.github.martinambrus.rdforward.server.api.PermissionManager.load(dir.toFile());
        BukkitBridge.uninstall();
        rd = new StubRdServer();
        BukkitBridge.install(rd);
    }

    @AfterEach
    void uninstall() {
        BukkitBridge.uninstall();
    }

    @Test
    void addPermissionLandsInRegistry() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        Permission perm = new Permission("custom.foo", PermissionDefault.OP);
        pm.addPermission(perm);

        RegisteredPermission rp = rd.permissionManager.getRegistry().lookup("custom.foo");
        assertNotNull(rp, "rd-api registry must see the Bukkit-side registration");
        assertEquals("custom.foo", rp.name());
        assertEquals(com.github.martinambrus.rdforward.api.permission.PermissionDefault.OP,
                rp.defaultValue());

        assertEquals(perm, pm.getPermission("custom.foo"),
                "getPermission must round-trip the same instance");
    }

    @Test
    void hasPermissionRespectsRegisteredOpDefault() {
        com.github.martinambrus.rdforward.server.api.PermissionManager.addOp("admin", 4);

        Bukkit.getServer().getPluginManager().addPermission(
                new Permission("custom.adminonly", PermissionDefault.OP));

        assertTrue(rd.permissionManager.hasPermission("admin", "custom.adminonly"));
        assertFalse(rd.permissionManager.hasPermission("alice", "custom.adminonly"));
    }

    @Test
    void removePermissionClearsRegistryAndPluginMap() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.addPermission(new Permission("custom.toberemoved", PermissionDefault.TRUE));
        assertNotNull(pm.getPermission("custom.toberemoved"));

        pm.removePermission("custom.toberemoved");
        assertNull(pm.getPermission("custom.toberemoved"));
        assertNull(rd.permissionManager.getRegistry().lookup("custom.toberemoved"));
    }

    @Test
    void getDefaultPermissionsDerivedFromRegistry() {
        // The default-permissions getter lives on SimplePluginManager (not the
        // bare PluginManager interface), and our StubPluginManager extends it.
        SimplePluginManager pm = (SimplePluginManager) Bukkit.getServer().getPluginManager();
        pm.addPermission(new Permission("custom.always", PermissionDefault.TRUE));
        pm.addPermission(new Permission("custom.opsonly", PermissionDefault.OP));
        pm.addPermission(new Permission("custom.guests", PermissionDefault.NOT_OP));

        Collection<Permission> opDefaults = pm.getDefaultPermissions(true);
        assertTrue(opDefaults.stream().anyMatch(p -> "custom.always".equals(p.getName())));
        assertTrue(opDefaults.stream().anyMatch(p -> "custom.opsonly".equals(p.getName())));
        assertFalse(opDefaults.stream().anyMatch(p -> "custom.guests".equals(p.getName())));

        Collection<Permission> nonOpDefaults = pm.getDefaultPermissions(false);
        assertTrue(nonOpDefaults.stream().anyMatch(p -> "custom.always".equals(p.getName())));
        assertFalse(nonOpDefaults.stream().anyMatch(p -> "custom.opsonly".equals(p.getName())));
        assertTrue(nonOpDefaults.stream().anyMatch(p -> "custom.guests".equals(p.getName())));
    }

    @Test
    void isPluginEnabledFalseSilentlyWhenNoModManager() {
        // StubRdServer.modManager defaults to null — bridge must answer false
        // without spamming StubCallLog.
        assertFalse(Bukkit.getServer().getPluginManager().isPluginEnabled("Vault"));
    }

    @Test
    void isPluginEnabledQueriesModManagerWhenWired() {
        rd.modManager = new ModManager() {
            @Override public ModDescriptor get(String modId) { return null; }
            @Override public boolean isLoaded(String modId) { return "RealMod".equals(modId); }
            @Override public Collection<ModDescriptor> all() { return List.of(); }
        };
        // Re-install so the bridge picks up the new mod manager reference.
        BukkitBridge.uninstall();
        BukkitBridge.install(rd);

        assertTrue(Bukkit.getServer().getPluginManager().isPluginEnabled("RealMod"));
        assertFalse(Bukkit.getServer().getPluginManager().isPluginEnabled("MissingMod"));
    }
}

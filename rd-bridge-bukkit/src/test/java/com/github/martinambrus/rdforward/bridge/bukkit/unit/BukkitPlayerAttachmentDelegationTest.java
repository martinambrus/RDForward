package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.world.Location;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BukkitPlayerAttachmentDelegationTest {

    @BeforeEach
    void setUp(@TempDir Path dir) throws Exception {
        File data = dir.toFile();
        new File(data, "ops.txt").createNewFile();
        com.github.martinambrus.rdforward.server.api.PermissionManager.load(data);
    }

    @AfterEach
    void tearDown() {
        BukkitPlayer.evict("TestPlayer");
        BukkitBridge.uninstall();
        try { com.github.martinambrus.rdforward.server.api.PermissionManager.removeOp("testplayer"); } catch (Exception ignored) {}
    }

    @Test
    void addAttachmentReturnsNonNull() {
        installWithPlayer(false);
        Player p = BukkitPlayer.create("TestPlayer");
        StubPlugin plugin = new StubPlugin();
        PermissionAttachment att = p.addAttachment(plugin);
        assertNotNull(att, "addAttachment must return a real PermissionAttachment");
        assertSame(plugin, att.getPlugin());
    }

    @Test
    void hasPermissionResolvesFromAttachment() {
        installWithPlayer(false);
        Player p = BukkitPlayer.create("TestPlayer");
        p.addAttachment(new StubPlugin(), "essentials.fly", true);
        assertTrue(p.hasPermission("essentials.fly"),
                "hasPermission must resolve from the perm field's attachment");
    }

    @Test
    void hasPermissionAttachmentDenialOverridesOp() {
        com.github.martinambrus.rdforward.server.api.PermissionManager.addOp(
                "testplayer", com.github.martinambrus.rdforward.server.api.PermissionManager.MAX_OP_LEVEL);
        installWithPlayer(true);
        Player p = BukkitPlayer.create("TestPlayer");
        p.addAttachment(new StubPlugin(), "essentials.fly", false);
        assertFalse(p.hasPermission("essentials.fly"),
                "attachment denial must override OP fallback");
    }

    @Test
    void isPermissionSetReflectsAttachment() {
        installWithPlayer(false);
        Player p = BukkitPlayer.create("TestPlayer");
        p.addAttachment(new StubPlugin(), "worldedit.region.set", true);
        assertTrue(p.isPermissionSet("worldedit.region.set"));
        assertFalse(p.isPermissionSet("nonexistent.perm"));
    }

    @Test
    void getEffectivePermissionsFromAttachment() {
        installWithPlayer(false);
        Player p = BukkitPlayer.create("TestPlayer");
        p.addAttachment(new StubPlugin(), "perm.a", true);
        p.addAttachment(new StubPlugin(), "perm.b", false);

        Set<PermissionAttachmentInfo> eff = p.getEffectivePermissions();
        assertEquals(2, eff.size());
    }

    @Test
    void removeAttachmentRevokesPermission() {
        installWithPlayer(false);
        Player p = BukkitPlayer.create("TestPlayer");
        PermissionAttachment att = p.addAttachment(new StubPlugin(), "test.perm", true);
        assertTrue(p.hasPermission("test.perm"));
        p.removeAttachment(att);
        assertFalse(p.hasPermission("test.perm"));
    }

    private void installWithPlayer(boolean op) {
        StubRdServer rd = new StubRdServer();
        StubRdServer.StubRdPlayer rdPlayer =
                new StubRdServer.StubRdPlayer("TestPlayer",
                        new Location("stub-world", 0, 64, 0, 0f, 0f));
        rdPlayer.op = op;
        rd.players.put("TestPlayer", rdPlayer);
        BukkitBridge.install(rd);
    }

    private static class StubPlugin implements Plugin {
        @Override public boolean isEnabled() { return true; }
        @Override public String getName() { return "StubPlugin"; }
        @Override public org.bukkit.plugin.PluginDescriptionFile getDescription() { return null; }
        @Override public java.util.logging.Logger getLogger() { return java.util.logging.Logger.getLogger("StubPlugin"); }
        @Override public org.bukkit.Server getServer() { return null; }
        @Override public void onLoad() {}
        @Override public void onEnable() {}
        @Override public void onDisable() {}
    }
}

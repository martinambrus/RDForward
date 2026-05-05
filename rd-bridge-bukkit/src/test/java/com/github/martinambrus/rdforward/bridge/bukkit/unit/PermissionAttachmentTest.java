package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PermissionAttachmentTest {

    private final Permissible permissible = new PermissibleBase();
    private final StubPlugin plugin = new StubPlugin();

    @Test
    void storesPluginAndPermissible() {
        PermissionAttachment att = new PermissionAttachment(plugin, permissible);
        assertSame(plugin, att.getPlugin());
        assertSame(permissible, att.getPermissible());
    }

    @Test
    void permissionsMapStartsEmpty() {
        PermissionAttachment att = new PermissionAttachment(plugin, permissible);
        assertTrue(att.getPermissions().isEmpty());
    }

    @Test
    void setPermissionAddsToMap() {
        PermissionAttachment att = new PermissionAttachment(plugin, permissible);
        att.setPermission("essentials.fly", true);
        att.setPermission("essentials.god", false);

        Map<String, Boolean> perms = att.getPermissions();
        assertEquals(2, perms.size());
        assertTrue(perms.get("essentials.fly"));
        assertFalse(perms.get("essentials.god"));
    }

    @Test
    void unsetPermissionRemovesFromMap() {
        PermissionAttachment att = new PermissionAttachment(plugin, permissible);
        att.setPermission("essentials.fly", true);
        att.unsetPermission("essentials.fly");

        assertFalse(att.getPermissions().containsKey("essentials.fly"));
    }

    @Test
    void setPermissionOverwrites() {
        PermissionAttachment att = new PermissionAttachment(plugin, permissible);
        att.setPermission("test.perm", true);
        att.setPermission("test.perm", false);

        assertFalse(att.getPermissions().get("test.perm"));
    }

    @Test
    void noArgCtorHasNullPluginAndPermissible() {
        PermissionAttachment att = new PermissionAttachment();
        assertNull(att.getPlugin());
        assertNull(att.getPermissible());
    }

    @Test
    void removalCallbackRoundTrip() {
        PermissionAttachment att = new PermissionAttachment(plugin, permissible);
        assertNull(att.getRemovalCallback());

        StubRemovalCallback cb = new StubRemovalCallback();
        att.setRemovalCallback(cb);
        assertSame(cb, att.getRemovalCallback());
    }

    @Test
    void removeCallsPermissibleRemoveAttachment() {
        TrackingPermissible trackable = new TrackingPermissible();
        PermissionAttachment att = new PermissionAttachment(plugin, trackable);
        assertTrue(att.remove());
        assertEquals(1, trackable.removeAttachmentCalls);
        assertTrue(trackable.lastRemoved == att);
    }

    @Test
    void removeCallsRemovalCallback() {
        StubRemovalCallback cb = new StubRemovalCallback();
        PermissionAttachment att = new PermissionAttachment(plugin, permissible);
        att.setRemovalCallback(cb);
        att.remove();
        assertTrue(cb.called);
        assertSame(att, cb.attachment);
    }

    /** Minimal Plugin stub. */
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

    private static class StubRemovalCallback implements org.bukkit.permissions.PermissionRemovedExecutor {
        boolean called;
        PermissionAttachment attachment;
        @Override public void attachmentRemoved(PermissionAttachment attachment) {
            this.called = true;
            this.attachment = attachment;
        }
    }

    /** Permissible that tracks removeAttachment calls. */
    private static class TrackingPermissible extends PermissibleBase {
        int removeAttachmentCalls;
        PermissionAttachment lastRemoved;

        @Override public void removeAttachment(PermissionAttachment att) {
            removeAttachmentCalls++;
            lastRemoved = att;
        }
    }
}

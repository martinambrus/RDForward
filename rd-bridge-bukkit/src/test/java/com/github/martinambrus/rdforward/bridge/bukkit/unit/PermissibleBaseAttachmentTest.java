package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PermissibleBaseAttachmentTest {

    private PermissibleBase perm;
    private final StubPlugin plugin = new StubPlugin();

    @BeforeEach
    void setUp() {
        perm = new PermissibleBase();
    }

    // --- addAttachment ---

    @Test
    void addAttachmentReturnsNonNull() {
        PermissionAttachment att = perm.addAttachment(plugin);
        assertNotNull(att);
        assertSame(plugin, att.getPlugin());
        assertSame(perm, att.getPermissible());
    }

    @Test
    void addAttachmentWithNameAndValue() {
        PermissionAttachment att = perm.addAttachment(plugin, "essentials.fly", true);
        assertNotNull(att);
        assertTrue(att.getPermissions().get("essentials.fly"));
    }

    @Test
    void addAttachmentWithTicksReturnsSameAsBasic() {
        PermissionAttachment att = perm.addAttachment(plugin, 10);
        assertNotNull(att);
        assertSame(plugin, att.getPlugin());
    }

    // --- hasPermission from attachments ---

    @Test
    void hasPermissionFromAttachment() {
        perm.addAttachment(plugin, "essentials.fly", true);
        assertTrue(perm.hasPermission("essentials.fly"));
    }

    @Test
    void hasPermissionNegativeFromAttachment() {
        perm.addAttachment(plugin, "essentials.fly", false);
        assertFalse(perm.hasPermission("essentials.fly"));
    }

    @Test
    void hasPermissionFallsThroughToOp() {
        OpHolder opHolder = new OpHolder(true);
        PermissibleBase opPerm = new PermissibleBase(opHolder);
        // No attachments — should fall through to op=true
        assertTrue(opPerm.hasPermission("any.random.perm"));
    }

    @Test
    void hasPermissionReturnsFalseForNonOpNoAttachments() {
        assertFalse(perm.hasPermission("any.random.perm"));
    }

    @Test
    void lastAttachmentWins() {
        StubPlugin plugin2 = new StubPlugin();
        perm.addAttachment(plugin, "test.perm", false);
        perm.addAttachment(plugin2, "test.perm", true);
        // Last attachment added = highest priority
        assertTrue(perm.hasPermission("test.perm"));
    }

    @Test
    void attachmentOverrideWinsOverOp() {
        OpHolder opHolder = new OpHolder(true);
        PermissibleBase opPerm = new PermissibleBase(opHolder);
        opPerm.addAttachment(plugin, "essentials.fly", false);
        // Attachment explicitly denies, even though op
        assertFalse(opPerm.hasPermission("essentials.fly"));
    }

    // --- isPermissionSet ---

    @Test
    void isPermissionSetTrueWhenInAttachment() {
        perm.addAttachment(plugin, "essentials.fly", true);
        assertTrue(perm.isPermissionSet("essentials.fly"));
    }

    @Test
    void isPermissionSetFalseWhenNotInAnyAttachment() {
        assertFalse(perm.isPermissionSet("essentials.fly"));
    }

    // --- removeAttachment ---

    @Test
    void removeAttachmentRemovesPermission() {
        PermissionAttachment att = perm.addAttachment(plugin, "essentials.fly", true);
        assertTrue(perm.hasPermission("essentials.fly"));
        perm.removeAttachment(att);
        assertFalse(perm.hasPermission("essentials.fly"));
    }

    // --- clearPermissions ---

    @Test
    void clearPermissionsRemovesAllAttachments() {
        perm.addAttachment(plugin, "perm.a", true);
        perm.addAttachment(plugin, "perm.b", false);
        perm.clearPermissions();
        assertFalse(perm.hasPermission("perm.a"));
        assertFalse(perm.isPermissionSet("perm.b"));
    }

    // --- recalculatePermissions + getEffectivePermissions ---

    @Test
    void getEffectivePermissionsReturnsAttachmentEntries() {
        perm.addAttachment(plugin, "essentials.fly", true);
        perm.addAttachment(plugin, "essentials.god", false);
        perm.recalculatePermissions();

        Set<PermissionAttachmentInfo> eff = perm.getEffectivePermissions();
        assertEquals(2, eff.size());

        boolean foundFly = false, foundGod = false;
        for (PermissionAttachmentInfo info : eff) {
            if ("essentials.fly".equals(info.getPermission())) {
                assertTrue(info.getValue());
                foundFly = true;
            }
            if ("essentials.god".equals(info.getPermission())) {
                assertFalse(info.getValue());
                foundGod = true;
            }
        }
        assertTrue(foundFly, "essentials.fly should be in effective permissions");
        assertTrue(foundGod, "essentials.god should be in effective permissions");
    }

    @Test
    void getEffectivePermissionsAutoRecalculatesWhenEmpty() {
        perm.addAttachment(plugin, "test.perm", true);
        // Don't call recalculatePermissions manually
        Set<PermissionAttachmentInfo> eff = perm.getEffectivePermissions();
        assertEquals(1, eff.size());
        assertEquals("test.perm", eff.iterator().next().getPermission());
    }

    // --- reflection compatibility ---

    @Test
    void permissionsFieldIsAccessibleViaReflection() throws Exception {
        java.lang.reflect.Field field = PermissibleBase.class.getDeclaredField("permissions");
        field.setAccessible(true);
        Object val = field.get(perm);
        assertNotNull(val);
        assertInstanceOf(java.util.Map.class, val);
    }

    @Test
    void attachmentsFieldIsAccessibleViaReflection() throws Exception {
        java.lang.reflect.Field field = PermissibleBase.class.getDeclaredField("attachments");
        field.setAccessible(true);
        Object val = field.get(perm);
        assertNotNull(val);
        assertInstanceOf(java.util.List.class, val);
    }

    // --- helpers ---

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

    private static class OpHolder implements ServerOperator {
        final boolean op;
        OpHolder(boolean op) { this.op = op; }
        @Override public boolean isOp() { return op; }
        @Override public void setOp(boolean value) {}
    }
}

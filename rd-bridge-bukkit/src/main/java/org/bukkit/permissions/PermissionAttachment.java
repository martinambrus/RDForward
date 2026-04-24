package org.bukkit.permissions;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PermissionAttachment {
    public PermissionAttachment(org.bukkit.plugin.Plugin arg0, org.bukkit.permissions.Permissible arg1) {}
    public PermissionAttachment() {}
    public org.bukkit.plugin.Plugin getPlugin() {
        return null;
    }
    public void setRemovalCallback(org.bukkit.permissions.PermissionRemovedExecutor arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissionAttachment.setRemovalCallback(Lorg/bukkit/permissions/PermissionRemovedExecutor;)V");
    }
    public org.bukkit.permissions.PermissionRemovedExecutor getRemovalCallback() {
        return null;
    }
    public org.bukkit.permissions.Permissible getPermissible() {
        return null;
    }
    public java.util.Map getPermissions() {
        return java.util.Collections.emptyMap();
    }
    public void setPermission(java.lang.String arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissionAttachment.setPermission(Ljava/lang/String;Z)V");
    }
    public void setPermission(org.bukkit.permissions.Permission arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissionAttachment.setPermission(Lorg/bukkit/permissions/Permission;Z)V");
    }
    public void unsetPermission(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissionAttachment.unsetPermission(Ljava/lang/String;)V");
    }
    public void unsetPermission(org.bukkit.permissions.Permission arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissionAttachment.unsetPermission(Lorg/bukkit/permissions/Permission;)V");
    }
    public boolean remove() {
        return false;
    }
}

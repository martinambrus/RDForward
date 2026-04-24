package org.bukkit.permissions;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PermissibleBase implements org.bukkit.permissions.Permissible {
    public PermissibleBase(org.bukkit.permissions.ServerOperator arg0) {}
    public PermissibleBase() {}
    public boolean isOp() {
        return false;
    }
    public void setOp(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissibleBase.setOp(Z)V");
    }
    public boolean isPermissionSet(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissibleBase.isPermissionSet(Ljava/lang/String;)Z");
        return false;
    }
    public boolean isPermissionSet(org.bukkit.permissions.Permission arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissibleBase.isPermissionSet(Lorg/bukkit/permissions/Permission;)Z");
        return false;
    }
    public boolean hasPermission(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissibleBase.hasPermission(Ljava/lang/String;)Z");
        return false;
    }
    public boolean hasPermission(org.bukkit.permissions.Permission arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissibleBase.hasPermission(Lorg/bukkit/permissions/Permission;)Z");
        return false;
    }
    public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissibleBase.addAttachment(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;Z)Lorg/bukkit/permissions/PermissionAttachment;");
        return null;
    }
    public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissibleBase.addAttachment(Lorg/bukkit/plugin/Plugin;)Lorg/bukkit/permissions/PermissionAttachment;");
        return null;
    }
    public void removeAttachment(org.bukkit.permissions.PermissionAttachment arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissibleBase.removeAttachment(Lorg/bukkit/permissions/PermissionAttachment;)V");
    }
    public void recalculatePermissions() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissibleBase.recalculatePermissions()V");
    }
    public void clearPermissions() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissibleBase.clearPermissions()V");
    }
    public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, boolean arg2, int arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissibleBase.addAttachment(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;ZI)Lorg/bukkit/permissions/PermissionAttachment;");
        return null;
    }
    public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.PermissibleBase.addAttachment(Lorg/bukkit/plugin/Plugin;I)Lorg/bukkit/permissions/PermissionAttachment;");
        return null;
    }
    public java.util.Set getEffectivePermissions() {
        return java.util.Collections.emptySet();
    }
}

package org.bukkit.permissions;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class Permission {
    public static final org.bukkit.permissions.PermissionDefault DEFAULT_PERMISSION = null;
    public Permission(java.lang.String arg0) {}
    public Permission(java.lang.String arg0, java.lang.String arg1) {}
    public Permission(java.lang.String arg0, org.bukkit.permissions.PermissionDefault arg1) {}
    public Permission(java.lang.String arg0, java.lang.String arg1, org.bukkit.permissions.PermissionDefault arg2) {}
    public Permission(java.lang.String arg0, java.util.Map arg1) {}
    public Permission(java.lang.String arg0, java.lang.String arg1, java.util.Map arg2) {}
    public Permission(java.lang.String arg0, org.bukkit.permissions.PermissionDefault arg1, java.util.Map arg2) {}
    public Permission(java.lang.String arg0, java.lang.String arg1, org.bukkit.permissions.PermissionDefault arg2, java.util.Map arg3) {}
    public Permission() {}
    public java.lang.String getName() {
        return null;
    }
    public java.util.Map getChildren() {
        return java.util.Collections.emptyMap();
    }
    public org.bukkit.permissions.PermissionDefault getDefault() {
        return null;
    }
    public void setDefault(org.bukkit.permissions.PermissionDefault arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.Permission.setDefault(Lorg/bukkit/permissions/PermissionDefault;)V");
    }
    public java.lang.String getDescription() {
        return null;
    }
    public void setDescription(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.Permission.setDescription(Ljava/lang/String;)V");
    }
    public java.util.Set getPermissibles() {
        return java.util.Collections.emptySet();
    }
    public void recalculatePermissibles() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.Permission.recalculatePermissibles()V");
    }
    public org.bukkit.permissions.Permission addParent(java.lang.String arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.Permission.addParent(Ljava/lang/String;Z)Lorg/bukkit/permissions/Permission;");
        return this;
    }
    public void addParent(org.bukkit.permissions.Permission arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.Permission.addParent(Lorg/bukkit/permissions/Permission;Z)V");
    }
    public static java.util.List loadPermissions(java.util.Map arg0, java.lang.String arg1, org.bukkit.permissions.PermissionDefault arg2) {
        return java.util.Collections.emptyList();
    }
    public static org.bukkit.permissions.Permission loadPermission(java.lang.String arg0, java.util.Map arg1) {
        return null;
    }
    public static org.bukkit.permissions.Permission loadPermission(java.lang.String arg0, java.util.Map arg1, org.bukkit.permissions.PermissionDefault arg2, java.util.List arg3) {
        return null;
    }
}

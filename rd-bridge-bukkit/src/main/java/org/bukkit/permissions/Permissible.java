package org.bukkit.permissions;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Permissible extends org.bukkit.permissions.ServerOperator {
    boolean isPermissionSet(java.lang.String arg0);
    boolean isPermissionSet(org.bukkit.permissions.Permission arg0);
    boolean hasPermission(java.lang.String arg0);
    boolean hasPermission(org.bukkit.permissions.Permission arg0);
    org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, boolean arg2);
    org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin arg0);
    org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, boolean arg2, int arg3);
    org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin arg0, int arg1);
    void removeAttachment(org.bukkit.permissions.PermissionAttachment arg0);
    void recalculatePermissions();
    java.util.Set getEffectivePermissions();
    default net.kyori.adventure.util.TriState permissionValue(org.bukkit.permissions.Permission arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.Permissible.permissionValue(Lorg/bukkit/permissions/Permission;)Lnet/kyori/adventure/util/TriState;");
        return null;
    }
    default net.kyori.adventure.util.TriState permissionValue(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.permissions.Permissible.permissionValue(Ljava/lang/String;)Lnet/kyori/adventure/util/TriState;");
        return null;
    }
}

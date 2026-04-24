package io.papermc.paper.plugin;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PermissionManager {
    org.bukkit.permissions.Permission getPermission(java.lang.String arg0);
    void addPermission(org.bukkit.permissions.Permission arg0);
    void removePermission(org.bukkit.permissions.Permission arg0);
    void removePermission(java.lang.String arg0);
    java.util.Set getDefaultPermissions(boolean arg0);
    void recalculatePermissionDefaults(org.bukkit.permissions.Permission arg0);
    void subscribeToPermission(java.lang.String arg0, org.bukkit.permissions.Permissible arg1);
    void unsubscribeFromPermission(java.lang.String arg0, org.bukkit.permissions.Permissible arg1);
    java.util.Set getPermissionSubscriptions(java.lang.String arg0);
    void subscribeToDefaultPerms(boolean arg0, org.bukkit.permissions.Permissible arg1);
    void unsubscribeFromDefaultPerms(boolean arg0, org.bukkit.permissions.Permissible arg1);
    java.util.Set getDefaultPermSubscriptions(boolean arg0);
    java.util.Set getPermissions();
    void addPermissions(java.util.List arg0);
    void clearPermissions();
}

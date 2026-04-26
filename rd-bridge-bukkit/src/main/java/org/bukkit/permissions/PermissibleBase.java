// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.permissions;

/**
 * Bukkit-shaped {@code PermissibleBase}. Carries the upstream
 * {@code attachments} field name and a backing {@link java.util.List}
 * so LuckPerms's
 * {@code me.lucko.luckperms.bukkit.inject.permissible.PermissibleInjector.<clinit>}
 * — which reflects {@code PermissibleBase.class.getDeclaredField("attachments")}
 * — completes its static initializer instead of failing on
 * {@link NoSuchFieldException}.
 *
 * <p>Permission resolution itself is handled by RDForward's rd-api
 * {@code PermissionManager}; this class is only the API-shaped surface
 * plugins compile against.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PermissibleBase implements org.bukkit.permissions.Permissible {

    /** Field name + List type pinned to upstream Bukkit so LuckPerms's
     *  reflective lookup finds it. The list is mutated by LuckPerms via
     *  reflection during injection — keep it non-final. */
    private java.util.List<org.bukkit.permissions.PermissionAttachment> attachments =
            new java.util.LinkedList<>();

    private final org.bukkit.permissions.ServerOperator opable;

    public PermissibleBase(org.bukkit.permissions.ServerOperator opable) {
        this.opable = opable;
    }

    public PermissibleBase() {
        this.opable = null;
    }

    public boolean isOp() {
        return opable != null && opable.isOp();
    }

    public void setOp(boolean value) {
        if (opable != null) opable.setOp(value);
    }

    public boolean isPermissionSet(java.lang.String name) {
        return false;
    }

    public boolean isPermissionSet(org.bukkit.permissions.Permission perm) {
        return perm != null && isPermissionSet(perm.getName());
    }

    public boolean hasPermission(java.lang.String name) {
        return false;
    }

    public boolean hasPermission(org.bukkit.permissions.Permission perm) {
        return perm != null && hasPermission(perm.getName());
    }

    public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin,
                                                                     java.lang.String name,
                                                                     boolean value) {
        return null;
    }

    public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin) {
        return null;
    }

    public void removeAttachment(org.bukkit.permissions.PermissionAttachment attachment) {
        if (attachment != null) attachments.remove(attachment);
    }

    public void recalculatePermissions() {}

    public void clearPermissions() {
        attachments.clear();
    }

    public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin,
                                                                     java.lang.String name,
                                                                     boolean value, int ticks) {
        return null;
    }

    public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin, int ticks) {
        return null;
    }

    public java.util.Set getEffectivePermissions() {
        return java.util.Collections.emptySet();
    }
}

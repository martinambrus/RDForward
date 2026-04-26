// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.permissions;

/**
 * Bukkit-shaped {@code Permission}. Carries the four upstream fields so
 * RDForward's permission registry, plugins (LuckPerms reflectively
 * reads the {@code children} field via
 * {@link Class#getDeclaredField(String)} from
 * {@code LuckPermsPermissionMap.<clinit>}), and consumer plugins that
 * iterate {@code getDefaultPermissions} all see real values instead of
 * the auto-gen {@code null}/{@code emptyMap} stubs.
 *
 * <p>Field NAMES match real Paper exactly because LuckPerms identifies
 * the field by its String name; renaming would re-break the same
 * NoSuchFieldException reported in the original boot trace.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class Permission {

    public static final org.bukkit.permissions.PermissionDefault DEFAULT_PERMISSION =
            org.bukkit.permissions.PermissionDefault.OP;

    private String name;
    private final java.util.Map<String, Boolean> children = new java.util.LinkedHashMap<>();
    private org.bukkit.permissions.PermissionDefault defaultValue = DEFAULT_PERMISSION;
    private String description = "";

    public Permission() {}

    public Permission(String name) {
        this(name, null, null, null);
    }

    public Permission(String name, String description) {
        this(name, description, null, null);
    }

    public Permission(String name, org.bukkit.permissions.PermissionDefault defaultValue) {
        this(name, null, defaultValue, null);
    }

    public Permission(String name, String description, org.bukkit.permissions.PermissionDefault defaultValue) {
        this(name, description, defaultValue, null);
    }

    public Permission(String name, java.util.Map<String, Boolean> children) {
        this(name, null, null, children);
    }

    public Permission(String name, String description, java.util.Map<String, Boolean> children) {
        this(name, description, null, children);
    }

    public Permission(String name, org.bukkit.permissions.PermissionDefault defaultValue,
                      java.util.Map<String, Boolean> children) {
        this(name, null, defaultValue, children);
    }

    public Permission(String name, String description,
                      org.bukkit.permissions.PermissionDefault defaultValue,
                      java.util.Map<String, Boolean> children) {
        this.name = name;
        this.description = description == null ? "" : description;
        this.defaultValue = defaultValue == null ? DEFAULT_PERMISSION : defaultValue;
        if (children != null) this.children.putAll(children);
    }

    public java.lang.String getName() {
        return name;
    }

    public java.util.Map getChildren() {
        return children;
    }

    public org.bukkit.permissions.PermissionDefault getDefault() {
        return defaultValue;
    }

    public void setDefault(org.bukkit.permissions.PermissionDefault value) {
        this.defaultValue = value == null ? DEFAULT_PERMISSION : value;
    }

    public java.lang.String getDescription() {
        return description;
    }

    public void setDescription(java.lang.String value) {
        this.description = value == null ? "" : value;
    }

    public java.util.Set getPermissibles() {
        return java.util.Collections.emptySet();
    }

    public void recalculatePermissibles() {}

    public org.bukkit.permissions.Permission addParent(java.lang.String name, boolean value) {
        return this;
    }

    public void addParent(org.bukkit.permissions.Permission parent, boolean value) {}

    public static java.util.List loadPermissions(java.util.Map data, java.lang.String error,
                                                 org.bukkit.permissions.PermissionDefault def) {
        return java.util.Collections.emptyList();
    }

    public static org.bukkit.permissions.Permission loadPermission(java.lang.String name,
                                                                   java.util.Map data) {
        return null;
    }

    public static org.bukkit.permissions.Permission loadPermission(java.lang.String name,
                                                                   java.util.Map data,
                                                                   org.bukkit.permissions.PermissionDefault def,
                                                                   java.util.List output) {
        return null;
    }

    @Override
    public String toString() {
        return "Permission{name=" + name + ", default=" + defaultValue + "}";
    }
}

package com.github.martinambrus.rdforward.api.permission;

import java.util.Map;
import java.util.Objects;

/**
 * A permission node declared by a plugin or mod via
 * {@link PermissionRegistry#register(RegisteredPermission)}. Carries the
 * default-grant rule used by {@link PermissionManager#hasPermission} when
 * the player has no explicit grant, plus the optional Bukkit-style
 * {@code children} map (sub-permissions and their inheritance flag).
 */
public record RegisteredPermission(String name, PermissionDefault defaultValue, Map<String, Boolean> children) {

    public RegisteredPermission {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(defaultValue, "defaultValue");
        children = children == null ? Map.of() : Map.copyOf(children);
    }

    /** Convenience constructor for a permission with no children. */
    public RegisteredPermission(String name, PermissionDefault defaultValue) {
        this(name, defaultValue, Map.of());
    }
}

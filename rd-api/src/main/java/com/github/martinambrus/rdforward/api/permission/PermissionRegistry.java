package com.github.martinambrus.rdforward.api.permission;

import java.util.Collection;
import java.util.Set;

/**
 * Registry of permission node declarations. Mods and bridge plugins
 * register {@link RegisteredPermission} entries here so that
 * {@link PermissionManager#hasPermission} can resolve unknown nodes
 * against their default-grant rule instead of falling back to op-only
 * checks.
 *
 * <p>Implementations must be thread-safe — {@link #register} can be called
 * from arbitrary plugin threads while gameplay code reads the registry on
 * the server thread.
 */
public interface PermissionRegistry {

    /** Register or replace the given permission. */
    void register(RegisteredPermission perm);

    /** Remove the permission with the given name, if any. */
    void unregister(String name);

    /** @return the registered entry, or {@code null} if no permission with that name is registered. */
    RegisteredPermission lookup(String name);

    /** @return a snapshot of every registered permission. */
    Collection<RegisteredPermission> all();

    /**
     * Names of every permission whose default-grant rule applies for the
     * given op state. Used by Bukkit-style {@code getDefaultPermissions}
     * queries.
     */
    Set<String> defaultsFor(boolean op);
}

package com.github.martinambrus.rdforward.api.permission;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * No-op registry returned by the default {@link PermissionManager#getRegistry}
 * implementation so that legacy / test-only PermissionManagers don't need to
 * supply their own registry just to satisfy the interface contract.
 */
final class EmptyPermissionRegistry implements PermissionRegistry {

    static final EmptyPermissionRegistry INSTANCE = new EmptyPermissionRegistry();

    private EmptyPermissionRegistry() {}

    @Override public void register(RegisteredPermission perm) {}
    @Override public void unregister(String name) {}
    @Override public RegisteredPermission lookup(String name) { return null; }
    @Override public Collection<RegisteredPermission> all() { return List.of(); }
    @Override public Set<String> defaultsFor(boolean op) { return Set.of(); }
}

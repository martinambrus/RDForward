package com.github.martinambrus.rdforward.modloader.impl;

import com.github.martinambrus.rdforward.api.permission.PermissionManager;
import com.github.martinambrus.rdforward.api.permission.PermissionRegistry;
import com.github.martinambrus.rdforward.api.permission.RegisteredPermission;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter over rd-server's static {@link com.github.martinambrus.rdforward.server.api.PermissionManager}.
 * The server layer only models op levels; named permission nodes
 * ({@link #grant(String, String)} / {@link #revoke(String, String)} /
 * {@link #hasPermission(String, String)}) are kept in an in-memory map
 * here. Persistence across restarts is a Phase 4 follow-up.
 *
 * <p>Plugins and bridges declare permissions via {@link #getRegistry()};
 * {@link #hasPermission} consults the registry's default-grant rule when
 * the player has no explicit grant, before falling back to the op check.
 */
public final class RDPermissionManager implements PermissionManager {

    private final Map<String, Set<String>> nodesByPlayer = new ConcurrentHashMap<>();
    private final InMemoryPermissionRegistry registry = new InMemoryPermissionRegistry();

    @Override
    public int getOpLevel(String playerName) {
        return com.github.martinambrus.rdforward.server.api.PermissionManager.getOpLevel(playerName);
    }

    @Override
    public boolean isOp(String playerName) {
        return com.github.martinambrus.rdforward.server.api.PermissionManager.isOp(playerName);
    }

    @Override
    public boolean hasPermission(String playerName, String permission) {
        if (permission == null || permission.isEmpty()) return true;
        Set<String> nodes = nodesByPlayer.get(playerName);
        if (nodes != null && nodes.contains(permission)) return true;
        RegisteredPermission rp = registry.lookup(permission);
        if (rp != null) return rp.defaultValue().appliesTo(isOp(playerName));
        return isOp(playerName);
    }

    @Override
    public void grant(String playerName, String permission) {
        if (permission == null || permission.isEmpty()) return;
        nodesByPlayer.computeIfAbsent(playerName, k -> ConcurrentHashMap.newKeySet()).add(permission);
    }

    @Override
    public void revoke(String playerName, String permission) {
        Set<String> nodes = nodesByPlayer.get(playerName);
        if (nodes != null) nodes.remove(permission);
    }

    @Override
    public PermissionRegistry getRegistry() {
        return registry;
    }

    /**
     * Thread-safe, in-memory registry. Two name caches are maintained per op
     * state so that {@link #defaultsFor} is O(1) for the common Bukkit
     * {@code getDefaultPermissions} use case.
     */
    private static final class InMemoryPermissionRegistry implements PermissionRegistry {

        private final Map<String, RegisteredPermission> byName = new ConcurrentHashMap<>();
        private final Set<String> defaultsForOps = ConcurrentHashMap.newKeySet();
        private final Set<String> defaultsForNonOps = ConcurrentHashMap.newKeySet();

        @Override
        public void register(RegisteredPermission perm) {
            RegisteredPermission previous = byName.put(perm.name(), perm);
            if (previous != null) clearCachesFor(previous);
            if (perm.defaultValue().appliesTo(true)) defaultsForOps.add(perm.name());
            if (perm.defaultValue().appliesTo(false)) defaultsForNonOps.add(perm.name());
        }

        @Override
        public void unregister(String name) {
            RegisteredPermission removed = byName.remove(name);
            if (removed != null) clearCachesFor(removed);
        }

        private void clearCachesFor(RegisteredPermission perm) {
            defaultsForOps.remove(perm.name());
            defaultsForNonOps.remove(perm.name());
        }

        @Override
        public RegisteredPermission lookup(String name) {
            return name == null ? null : byName.get(name);
        }

        @Override
        public Collection<RegisteredPermission> all() {
            return Collections.unmodifiableCollection(byName.values());
        }

        @Override
        public Set<String> defaultsFor(boolean op) {
            return new HashSet<>(op ? defaultsForOps : defaultsForNonOps);
        }
    }
}

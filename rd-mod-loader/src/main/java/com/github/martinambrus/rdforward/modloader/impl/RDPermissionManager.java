package com.github.martinambrus.rdforward.modloader.impl;

import com.github.martinambrus.rdforward.api.permission.PermissionManager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter over rd-server's static {@link com.github.martinambrus.rdforward.server.api.PermissionManager}.
 * The server layer only models op levels; named permission nodes
 * ({@link #grant(String, String)} / {@link #revoke(String, String)} /
 * {@link #hasPermission(String, String)}) are kept in an in-memory map
 * here. Persistence across restarts is a Phase 4 follow-up.
 */
public final class RDPermissionManager implements PermissionManager {

    private final Map<String, Set<String>> nodesByPlayer = new ConcurrentHashMap<>();

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
}

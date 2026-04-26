package com.github.martinambrus.rdforward.api.permission;

/**
 * Permission checks. RDForward uses op levels (0 = regular, 1..4 = ops)
 * inherited from vanilla Minecraft plus string permission nodes mods can
 * query or grant.
 */
public interface PermissionManager {

    /** Op level of the given player name. 0 = not an op. */
    int getOpLevel(String playerName);

    boolean isOp(String playerName);

    /** True if the player has the given permission node or is a sufficient op. */
    boolean hasPermission(String playerName, String permission);

    /** Grant a permission node to a player (persisted). */
    void grant(String playerName, String permission);

    /** Revoke a permission node from a player. */
    void revoke(String playerName, String permission);

    /**
     * Registry of permission declarations made by mods and bridge plugins.
     * Default returns an empty registry so legacy {@link PermissionManager}
     * impls (test fixtures, minimal embeds) don't have to supply one to
     * satisfy the interface contract.
     */
    default PermissionRegistry getRegistry() {
        return EmptyPermissionRegistry.INSTANCE;
    }
}

package com.github.martinambrus.rdforward.api.permission;

/**
 * Default rule that decides whether a {@link RegisteredPermission} grants
 * access to a player who has no explicit grant. Mirrors the four-state
 * vanilla / Bukkit model.
 */
public enum PermissionDefault {
    /** Always granted. */
    TRUE,
    /** Never granted (overrides op status). */
    FALSE,
    /** Granted iff the player is an op. */
    OP,
    /** Granted iff the player is NOT an op. */
    NOT_OP;

    /** Apply this default rule given the player's op state. */
    public boolean appliesTo(boolean isOp) {
        return switch (this) {
            case TRUE -> true;
            case FALSE -> false;
            case OP -> isOp;
            case NOT_OP -> !isOp;
        };
    }
}

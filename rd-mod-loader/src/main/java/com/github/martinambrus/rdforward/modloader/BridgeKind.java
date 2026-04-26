package com.github.martinambrus.rdforward.modloader;

/**
 * Source format of a discovered mod jar. {@link #NATIVE} is the canonical
 * RDForward {@code rdmod.json} (or yaml/toml) descriptor; the remaining
 * values describe foreign plugin manifests handled by the {@code rd-bridge-*}
 * modules and dispatched reflectively by {@link BridgeRegistry}.
 */
public enum BridgeKind {
    NATIVE,
    PAPER,
    BUKKIT,
    FABRIC,
    FORGE,
    NEOFORGE,
    POCKETMINE
}

package com.github.martinambrus.rdforward.protocol;

/**
 * Protocol version numbers aligned with real Minecraft versions.
 *
 * RubyDung (version 0) is our custom extension for the pre-Classic prototype.
 * Classic (7), Alpha 1.0.15 (10), and Alpha 1.2.6 (14) match the real
 * Minecraft protocol version numbers from wiki.vg.
 *
 * Using real MC version numbers means our protocol upgrade path follows
 * the same version chain as Minecraft itself, enabling us to reuse
 * existing version translation logic (e.g., ViaLegacy's c0_28_30toa1_0_15).
 *
 * When a client connects, it declares its protocol version in the first
 * packet. The server then builds the appropriate translator pipeline
 * for that connection's Netty channel.
 */
public enum ProtocolVersion {

    /**
     * RubyDung (2009) - the original prototype.
     * 3 block types (air + grass + cobblestone), no multiplayer.
     * Uses Classic wire format. Custom protocol version 0 (not a real MC version).
     */
    RUBYDUNG(0, "RubyDung", 3),

    /**
     * Minecraft Classic (c0.0.20a - c0.30).
     * 50 block types, creative mode, flat world.
     * Real MC protocol version 7.
     */
    CLASSIC(7, "Classic", 50),

    /**
     * Minecraft Alpha 1.2.3-1.2.5.
     * ~82 block types, survival mode, entities, inventory.
     * Real MC protocol version 6.
     */
    ALPHA_1_2_5(6, "Alpha 1.2.5", 82),

    /**
     * Minecraft Alpha 1.0.15 - first SMP version.
     * ~82 block types, survival mode, entities, inventory.
     * Real MC protocol version 10.
     */
    ALPHA_1_0_15(10, "Alpha 1.0.15", 82),

    /**
     * Minecraft Alpha 1.2.6 - last Alpha version.
     * ~82 block types, health, time, mobs, explosions.
     * Real MC protocol version 14.
     */
    ALPHA_1_2_6(14, "Alpha 1.2.6", 82);

    // Future versions:
    // BETA_1_0(14, "Beta 1.0", ...)   — Beta reuses protocol 14 initially
    // RELEASE_1_0(29, "Release 1.0", ...)
    // RELEASE_1_7(4, "Release 1.7", ...)  — Netty rewrite, VarInt protocol

    private final int versionNumber;
    private final String displayName;
    private final int blockCount;

    ProtocolVersion(int versionNumber, String displayName, int blockCount) {
        this.versionNumber = versionNumber;
        this.displayName = displayName;
        this.blockCount = blockCount;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBlockCount() {
        return blockCount;
    }

    /**
     * Look up a protocol version by its numeric ID.
     * Returns null if the version is unknown.
     */
    public static ProtocolVersion fromNumber(int number) {
        for (ProtocolVersion pv : values()) {
            if (pv.versionNumber == number) {
                return pv;
            }
        }
        return null;
    }

    /**
     * Check if this version is at least as new as the given version.
     */
    public boolean isAtLeast(ProtocolVersion other) {
        return this.versionNumber >= other.versionNumber;
    }

    /**
     * Check if this version uses the Classic wire format.
     * Classic and RubyDung use Classic format (fixed-size strings/arrays).
     * Alpha+ uses the pre-Netty format (string16, variable-length fields).
     */
    public boolean isClassicFormat() {
        return this.versionNumber <= CLASSIC.versionNumber;
    }
}

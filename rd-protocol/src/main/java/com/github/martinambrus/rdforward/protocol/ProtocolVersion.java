package com.github.martinambrus.rdforward.protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Protocol version numbers aligned with real Minecraft versions.
 *
 * RubyDung (version 0) is our custom extension for the pre-Classic prototype.
 * Classic (7), Alpha 1.0.15 (13), Alpha 1.0.16 (14), and Alpha 1.2.x (6) match
 * the real Minecraft protocol version numbers from wiki.vg.
 *
 * IMPORTANT: Alpha protocol version numbers are NON-MONOTONIC. The SMP rewrite
 * between Alpha 1.0.16 and 1.0.17 reset the version counter, so later game
 * versions have LOWER protocol numbers (v6) than earlier ones (v14):
 *   v14 = Alpha 1.0.16, v13 = Alpha 1.0.15, ..., v1 = Alpha 1.0.17,
 *   v2 = Alpha 1.1.0, ..., v6 = Alpha 1.2.3-1.2.6
 *
 * Because of this, isAtLeast() uses a separate sortOrder field that reflects
 * the true chronological order rather than the raw protocol number.
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
    RUBYDUNG(0, 0, Family.PRE_CLASSIC, "RubyDung", 3),

    /**
     * Minecraft Classic (c0.0.20a - c0.30).
     * 50 block types, creative mode, flat world.
     * Real MC protocol version 7.
     */
    CLASSIC(7, 1, Family.CLASSIC, "Classic", 50),

    /**
     * Minecraft Alpha 1.0.15 - first SMP version.
     * ~82 block types, survival mode, entities, inventory.
     * Real MC protocol version 13. Pre-rewrite SMP.
     */
    ALPHA_1_0_15(13, 2, Family.ALPHA, "Alpha 1.0.15 (v13)", 82),

    /**
     * Minecraft Alpha 1.0.16 - last pre-rewrite SMP version.
     * ~82 block types, survival mode, entities, inventory.
     * Real MC protocol version 14. Pre-rewrite SMP.
     */
    ALPHA_1_0_16(14, 3, Family.ALPHA, "Alpha 1.0.16 (v14)", 82),

    /**
     * Minecraft Alpha 1.2.3_01-1.2.3_04 - post-rewrite SMP.
     * ~82 block types, health, time, mobs, explosions.
     * Real MC protocol version 5. Same wire formats as v6.
     */
    ALPHA_1_2_3(5, 4, Family.ALPHA, "Alpha 1.2.3 (v5)", 82),

    /**
     * Minecraft Alpha 1.2.3_05-1.2.6 - post-rewrite SMP.
     * ~82 block types, health, time, mobs, explosions.
     * Real MC protocol version 6. Post-rewrite SMP (lower number than v14!).
     */
    ALPHA_1_2_5(6, 5, Family.ALPHA, "Alpha 1.2.x (v6)", 82),

    /**
     * Minecraft Bedrock Edition (1.26.0+).
     * Uses UDP/RakNet on port 19132 with a completely different protocol.
     * Protocol version 924 matches the CloudburstMC codec for 1.26.0.
     * Block count uses the same internal 0-91 range as Alpha.
     */
    BEDROCK(924, 6, Family.BEDROCK, "Bedrock", 92);

    // Future versions:
    // BETA_1_0(14, 7, Family.BETA, "Beta 1.0", ...)
    // RELEASE_1_0(29, 8, Family.RELEASE, "Release 1.0", ...)
    // RELEASE_1_7(4, 9, Family.RELEASE, "Release 1.7", ...)

    /**
     * Protocol family grouping. Used to show relevant supported versions
     * when rejecting a client with an unsupported protocol version.
     */
    public enum Family {
        PRE_CLASSIC, CLASSIC, ALPHA, BETA, RELEASE, BEDROCK
    }

    private final int versionNumber;
    private final int sortOrder;
    private final Family family;
    private final String displayName;
    private final int blockCount;

    ProtocolVersion(int versionNumber, int sortOrder, Family family, String displayName, int blockCount) {
        this.versionNumber = versionNumber;
        this.sortOrder = sortOrder;
        this.family = family;
        this.displayName = displayName;
        this.blockCount = blockCount;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public Family getFamily() {
        return family;
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
     * Get all supported versions belonging to the given family.
     */
    public static List<ProtocolVersion> getByFamily(Family family) {
        List<ProtocolVersion> result = new ArrayList<ProtocolVersion>();
        for (ProtocolVersion pv : values()) {
            if (pv.family == family) {
                result.add(pv);
            }
        }
        return result;
    }

    /**
     * Describe the game versions associated with a raw Alpha protocol number.
     * Useful for logging when a client connects with an unsupported version,
     * since the Alpha protocol doesn't transmit the game version string.
     * Returns null if the number is not a known Alpha protocol version.
     *
     * Source: wiki.vg Protocol version numbers.
     */
    public static String describeAlphaProtocol(int number) {
        switch (number) {
            case 1:  return "Alpha 1.0.17_04";
            case 2:  return "Alpha 1.1.0-1.1.2_01";
            case 3:  return "Alpha 1.2.0-1.2.1_01";
            case 4:  return "Alpha 1.2.2";
            case 5:  return "Alpha 1.2.3_01-1.2.3_04";
            case 6:  return "Alpha 1.2.3_05-1.2.6";
            case 7:  return "Classic c0.0.20a-c0.30";
            case 8:  return "Alpha 1.0.0-1.0.1_01";
            case 9:  return "Alpha 1.0.2-1.0.3";
            case 10: return "Alpha 1.0.4-1.0.11";
            case 11: return "Alpha 1.0.12";
            case 12: return "Alpha 1.0.13-1.0.14";
            case 13: return "Alpha 1.0.15";
            case 14: return "Alpha 1.0.16-1.0.16_02";
            default: return null;
        }
    }

    /**
     * Check if this version is at least as new as the given version.
     * Uses sortOrder (chronological) rather than versionNumber (non-monotonic).
     */
    public boolean isAtLeast(ProtocolVersion other) {
        return this.sortOrder >= other.sortOrder;
    }

    /**
     * Check if this version uses the Classic wire format.
     * Classic and RubyDung use Classic format (fixed-size strings/arrays).
     * Alpha+ uses the pre-Netty format (string16, variable-length fields).
     */
    public boolean isClassicFormat() {
        return this == RUBYDUNG || this == CLASSIC;
    }

    /**
     * Check if this version is Bedrock Edition (UDP/RakNet protocol).
     */
    public boolean isBedrock() {
        return this == BEDROCK;
    }
}

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
     * Minecraft Alpha 1.0.17_04 - first post-rewrite SMP version.
     * ~82 block types, survival mode, entities, inventory.
     * Real MC protocol version 1. Same wire formats as v2/v3/v4/v5/v6.
     */
    ALPHA_1_0_17(1, 4, Family.ALPHA, "Alpha 1.0.17 (v1)", 82),

    /**
     * Minecraft Alpha 1.1.0-1.1.2_01 - post-rewrite SMP, pre-day/night cycle.
     * ~82 block types, survival mode, entities, inventory.
     * Real MC protocol version 2. Same wire formats as v3/v4/v5/v6.
     */
    ALPHA_1_1_0(2, 5, Family.ALPHA, "Alpha 1.1.0 (v2)", 82),

    /**
     * Minecraft Alpha 1.2.0-1.2.1_01 - first post-rewrite SMP with day/night cycle.
     * ~82 block types, health, time, mobs, explosions.
     * Real MC protocol version 3. Same wire formats as v4/v5/v6.
     */
    ALPHA_1_2_0(3, 6, Family.ALPHA, "Alpha 1.2.0 (v3)", 82),

    /**
     * Minecraft Alpha 1.2.2 - post-rewrite SMP.
     * ~82 block types, health, time, mobs, explosions.
     * Real MC protocol version 4. Same wire formats as v5/v6.
     */
    ALPHA_1_2_2(4, 7, Family.ALPHA, "Alpha 1.2.2 (v4)", 82),

    /**
     * Minecraft Alpha 1.2.3_01-1.2.3_04 - post-rewrite SMP.
     * ~82 block types, health, time, mobs, explosions.
     * Real MC protocol version 5. Same wire formats as v6.
     */
    ALPHA_1_2_3(5, 8, Family.ALPHA, "Alpha 1.2.3 (v5)", 82),

    /**
     * Minecraft Alpha 1.2.3_05-1.2.6 - post-rewrite SMP.
     * ~82 block types, health, time, mobs, explosions.
     * Real MC protocol version 6. Post-rewrite SMP (lower number than v14!).
     */
    ALPHA_1_2_5(6, 9, Family.ALPHA, "Alpha 1.2.x (v6)", 82),

    /**
     * Minecraft Beta 1.0-1.1 - first Beta release.
     * ~92 block types, new inventory/window system (SetSlot replaces AddToInventory),
     * new block placement wire format (coordinates first), shorter HoldingChange.
     * Real MC protocol version 7 (clashes with Classic v7 — use family-aware lookup).
     */
    BETA_1_0(7, 10, Family.BETA, "Beta 1.0 (v7)", 92),

    /**
     * Minecraft Beta 1.1_02 through 1.2_02 - protocol version 8.
     * Beta 1.2 changed ItemStack damage from byte to short in S2C packets.
     * Both versions are handled simultaneously via the "phantom KeepAlive" trick:
     * S2C uses short damage (Beta 1.2 format); older clients read byte damage
     * and the trailing 0x00 byte is a valid zero-payload KeepAlive (0x00).
     * C2S keeps byte damage (Beta 1.1_02 format); Beta 1.2 clients' extra 0x00
     * trailing byte is also a phantom KeepAlive.
     * Real MC protocol version 8.
     */
    BETA_1_2(8, 11, Family.BETA, "Beta 1.2 (v8)", 92),

    /**
     * Minecraft Beta 1.3-1.3_01 - protocol version 9.
     * Added beds, redstone repeaters, slabs. Minimal wire-level changes from v8:
     * entity metadata format changed (irrelevant — we don't send metadata),
     * block digging behavior changed (irrelevant — we use instant break).
     * Login format unchanged, phantom KeepAlive trick still applies.
     * Real MC protocol version 9.
     */
    BETA_1_3(9, 12, Family.BETA, "Beta 1.3 (v9)", 92),

    /**
     * Minecraft Beta 1.4-1.4_01 - protocol version 10.
     * Added wolves, cookies, bed spawn points. Wire format identical to v9
     * (ViaLegacy Protocolb1_3_0_1Tob1_4_0_1 is a StatelessProtocol with zero
     * packet transformations). One new S2C packet (0x46 Game Event for rain)
     * which we don't need since we don't send weather.
     * Phantom KeepAlive trick still applies.
     * Real MC protocol version 10 (clashes with pre-rewrite Alpha v10).
     */
    BETA_1_4(10, 13, Family.BETA, "Beta 1.4 (v10)", 92),

    /**
     * Minecraft Bedrock Edition (1.26.0+).
     * Uses UDP/RakNet on port 19132 with a completely different protocol.
     * Protocol version 924 matches the CloudburstMC codec for 1.26.0.
     * Block count uses the same internal 0-91 range as Alpha.
     */
    BEDROCK(924, 14, Family.BEDROCK, "Bedrock", 92);

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
     *
     * WARNING: Protocol version 7 is shared by Classic and Beta 1.0.
     * Use {@link #fromNumber(int, Family...)} to disambiguate.
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
     * Look up a protocol version by its numeric ID, restricted to specific families.
     * Returns null if no matching version is found in the given families.
     *
     * This is needed because protocol version 7 is shared by Classic (c0.0.20a)
     * and Beta 1.0 — the caller must specify which families are valid for the
     * connection context.
     */
    public static ProtocolVersion fromNumber(int number, Family... families) {
        for (ProtocolVersion pv : values()) {
            if (pv.versionNumber == number) {
                for (Family f : families) {
                    if (pv.family == f) {
                        return pv;
                    }
                }
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
            case 7:  return "Beta 1.0-1.1 (or Classic c0.0.20a-c0.30)";
            case 8:  return "Beta 1.1_02-1.2_02 (or Alpha 1.0.0-1.0.1_01)";
            case 9:  return "Beta 1.3 (or Alpha 1.0.2-1.0.3)";
            case 10: return "Beta 1.4 (or Alpha 1.0.4-1.0.11)";
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

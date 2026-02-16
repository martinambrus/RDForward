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
     * Minecraft Beta 1.5-1.5_01 - protocol version 11.
     * Changed string encoding from Java Modified UTF-8 (writeUTF format:
     * 2-byte byte count + UTF-8 bytes) to String16 (2-byte char count +
     * UTF-16BE bytes). Auto-detected in HandshakeC2SPacket by peeking at
     * the first data byte after the length prefix (0x00 = String16 for ASCII).
     * Wire format otherwise identical to v10.
     * Real MC protocol version 11 (clashes with pre-rewrite Alpha v11/1.0.12).
     */
    BETA_1_5(11, 14, Family.BETA, "Beta 1.5 (v11)", 92),

    /**
     * Minecraft Beta 1.6-1.6_test - protocol version 12.
     * Added maps, trapdoors, tall grass, dead bushes. Wire format changes
     * from v11: Respawn (0x09) C2S/S2C gained a dimension byte, AddEntity
     * (0x17) S2C gained int data + conditional velocity. String encoding
     * uses String16 (same as v11). Login format unchanged.
     * Real MC protocol version 12.
     */
    BETA_1_6(12, 15, Family.BETA, "Beta 1.6 (v12)", 92),

    /**
     * Minecraft Beta 1.6.1-1.7_01 - protocol version 13.
     * Wire format identical to v12 (ViaLegacy Protocolb1_6_0_6Tob1_7_0_3
     * has zero packet transformations). Protocol v13 clashes with Alpha
     * 1.0.15 (v13) — disambiguated by String16 detection in Handshake
     * (Beta 1.5+ uses String16, Alpha uses writeUTF).
     * Real MC protocol version 13.
     */
    BETA_1_7(13, 16, Family.BETA, "Beta 1.7 (v13)", 92),

    /**
     * Minecraft Beta 1.7.2-1.7.3 - protocol version 14.
     * Wire format identical to v13 (ViaLegacy Protocolb1_6_0_6Tob1_7_0_3
     * has zero packet transformations). Protocol v14 clashes with Alpha
     * 1.0.16 (v14) — disambiguated by String16 detection in Handshake.
     * Real MC protocol version 14.
     */
    BETA_1_7_3(14, 17, Family.BETA, "Beta 1.7.3 (v14)", 92),

    /**
     * Minecraft Beta 1.8-1.8.1 - protocol version 17.
     * Major wire format overhaul: KeepAlive gained int ID, Login S2C/C2S gained
     * gameMode+difficulty+worldHeight+maxPlayers, Respawn gained multiple new
     * fields. First version with native creative mode (gameMode=1 in Login S2C
     * enables instant break, creative inventory, flying, no fall damage).
     * New C2S packets: CreativeSlot (0x6B), PlayerAbilities (0xCA).
     * String encoding uses String16 (same as v11+).
     * Real MC protocol version 17.
     */
    BETA_1_8(17, 18, Family.BETA, "Beta 1.8 (v17)", 92),

    /**
     * Minecraft Beta 1.9 Prerelease 5-6 - protocol version 21.
     * Hybrid wire format: uses v17's S2C packet formats (Login, Respawn, KeepAlive,
     * MapChunk, PreChunk, BlockChange, SpawnPlayer, DestroyEntity) but v22's
     * conditional-NBT item slot format for all C2S packets containing items
     * (BlockPlacement, WindowClick, CreativeSlot) and S2C SetSlot/WindowItems.
     * No encryption, old Handshake format ("username;host:port"), no PlayerAbilities.
     * ViaLegacy skips this version entirely (jumps v17 to v22).
     * Real MC protocol version 21.
     */
    BETA_1_9_PRE5(21, 19, Family.BETA, "Beta 1.9-pre5 (v21)", 92),

    /**
     * Minecraft Release 1.0.0 (the first official release after Beta).
     * Wire protocol nearly identical to v17 — the only significant change is
     * that item slots gained NBT tag data after the damage field. This affects
     * all packets containing item data (BlockPlacement, WindowClick, CreativeSlot,
     * SetSlot, WindowItems). Login, Handshake, KeepAlive, and Respawn are unchanged.
     * New C2S packets: EnchantItem (0x6C), PlayerAbilities (0xCA, now a real packet).
     * Real MC protocol version 22.
     */
    RELEASE_1_0(22, 20, Family.RELEASE, "Release 1.0.0 (v22)", 92),

    /**
     * Minecraft Release 1.1 (January 2012).
     * Added levelType String16 to Login S2C, Login C2S, and Respawn packets.
     * New bidirectional Custom Payload (0xFA) packet. Item slot format unchanged
     * from v22 (conditional NBT for damageable items).
     * Real MC protocol version 23.
     */
    RELEASE_1_1(23, 21, Family.RELEASE, "Release 1.1 (v23)", 92),

    /**
     * Minecraft Release 1.2.1 (March 2012).
     * Seed removed from Login S2C/C2S and Respawn. Dimension changed from byte to int.
     * Chunk format overhauled to section-based with 256 height + biome data.
     * InputPacket (0x1B) removed. Real MC protocol version 28.
     */
    RELEASE_1_2_1(28, 22, Family.RELEASE, "Release 1.2.1 (v28)", 92),

    /**
     * Minecraft Release 1.2.4-1.2.5 (March 2012).
     * Wire protocol nearly identical to v28. The only change is that
     * PlayerAbilities (0xCA) is now sent C2S by the client (4 booleans,
     * same format as v22). No other wire format changes.
     * Real MC protocol version 29.
     */
    RELEASE_1_2_4(29, 23, Family.RELEASE, "Release 1.2.4 (v29)", 92),

    /**
     * Minecraft Release 1.3.1 (August 2012).
     * Mandatory encryption (RSA + AES/CFB8) added to login flow. New handshake
     * format (byte protocolVer + String16 username + String16 hostname + int port).
     * Login C2S replaced by ClientStatuses (0xCD). PreChunk removed. MapChunk unused
     * int removed. BlockChange block ID byte->short. PlayerAbilities format changed.
     * Item slot NBT now unconditional. BlockPlacement gains cursor offset bytes.
     * DestroyEntity changed to variable-length array. SpawnPlayer gained metadata.
     * Real MC protocol version 39.
     */
    RELEASE_1_3_1(39, 24, Family.RELEASE, "Release 1.3.1 (v39)", 92),

    /**
     * Minecraft Release 1.4.2 (October 2012).
     * Wire format nearly identical to v39. Only C2S change: ClientSettings (0xCC)
     * gained a boolean showCape field. Entity metadata type IDs renumbered but we
     * only send empty metadata (0x7F terminator), so no impact.
     * Real MC protocol version 47.
     */
    RELEASE_1_4_2(47, 25, Family.RELEASE, "Release 1.4.2 (v47)", 92),

    /**
     * Minecraft Release 1.4.4-1.4.5 (November 2012).
     * Wire format nearly identical to v47. Entity metadata type IDs renumbered
     * again, but we only send empty metadata (0x7F terminator), so no impact.
     * Map item data packet changed from unsigned-byte to short length prefix,
     * but we don't send map data. No C2S format changes.
     * Real MC protocol version 49.
     */
    RELEASE_1_4_4(49, 26, Family.RELEASE, "Release 1.4.4 (v49)", 92),

    /**
     * Minecraft Release 1.4.6-1.4.7 (December 2012).
     * Wire format nearly identical to v49. S2C changes: SpawnItem redesigned as
     * AddEntity + SetEntityData, AddEntity gained pitch/yaw fields, bulk chunk
     * format updated — none of which we send. C2S: PlayerDigging status 3/4
     * renumbered (drop item stack vs single), but irrelevant since v17+ uses
     * creative mode and doesn't need drop replenishment.
     * Real MC protocol version 51.
     */
    RELEASE_1_4_6(51, 27, Family.RELEASE, "Release 1.4.6 (v51)", 92),

    /**
     * Minecraft Release 1.5-1.5.1 (March 2013).
     * Wire format nearly identical to v51. S2C changes: AddEntity minecart type
     * remapping, OpenScreen gained boolean field — we send neither. C2S:
     * WindowClick gained new mode values 4/5 (Q-drop, drag) but same wire format.
     * All silently consumed. No packet class changes needed.
     * Real MC protocol version 60.
     */
    RELEASE_1_5(60, 28, Family.RELEASE, "Release 1.5.1 (v60)", 92),

    /**
     * Minecraft Bedrock Edition (1.26.0+).
     * Uses UDP/RakNet on port 19132 with a completely different protocol.
     * Protocol version 924 matches the CloudburstMC codec for 1.26.0.
     * Block count uses the same internal 0-91 range as Alpha.
     */
    BEDROCK(924, 29, Family.BEDROCK, "Bedrock", 92);

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
            case 11: return "Beta 1.5 (or Alpha 1.0.12)";
            case 12: return "Beta 1.6 (or Alpha 1.0.13-1.0.14)";
            case 13: return "Beta 1.6.1-1.7_01 (or Alpha 1.0.15)";
            case 14: return "Beta 1.7.2-1.7.3 (or Alpha 1.0.16-1.0.16_02)";
            case 17: return "Beta 1.8-1.8.1";
            case 21: return "Beta 1.9 Prerelease 5-6";
            case 22: return "Release 1.0.0";
            case 23: return "Release 1.1";
            case 28: return "Release 1.2.1";
            case 29: return "Release 1.2.4-1.2.5";
            case 39: return "Release 1.3.1-1.3.2";
            case 47: return "Release 1.4.2";
            case 49: return "Release 1.4.4-1.4.5";
            case 51: return "Release 1.4.6-1.4.7";
            case 60: return "Release 1.5-1.5.1";
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

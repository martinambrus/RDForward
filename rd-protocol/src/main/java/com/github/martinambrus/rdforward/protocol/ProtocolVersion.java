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
     * Minecraft Release 1.5.2 (May 2013).
     * Wire format identical to v60 (ViaLegacy StatelessProtocol with zero
     * transformations). Pure protocol version bump.
     * Real MC protocol version 61.
     */
    RELEASE_1_5_2(61, 29, Family.RELEASE, "Release 1.5.2 (v61)", 92),

    /**
     * Minecraft Release 1.6.1 (July 2013).
     * PlayerAbilities (0xCA) speeds changed from byte to float.
     * Chat (0x03) S2C messages changed from plain text to JSON text components.
     * Login, encryption, handshake, chunk format all unchanged from v39+.
     * Real MC protocol version 73.
     */
    RELEASE_1_6_1(73, 30, Family.RELEASE, "Release 1.6.1 (v73)", 92),

    /**
     * Minecraft Release 1.6.2 (July 2013).
     * Entity Properties (0x2C) gained modifier list (short count + modifiers per property).
     * Wire format otherwise identical to v73.
     * Real MC protocol version 74.
     */
    RELEASE_1_6_2(74, 31, Family.RELEASE, "Release 1.6.2 (v74)", 92),

    /**
     * Minecraft Release 1.6.4 (September 2013).
     * Wire format identical to v74 (ViaLegacy Protocolr1_6_2Tor1_6_4 is empty).
     * Pure protocol version bump.
     * Real MC protocol version 78.
     */
    RELEASE_1_6_4(78, 32, Family.RELEASE, "Release 1.6.4 (v78)", 92),

    /**
     * Minecraft Release 1.7.2-1.7.5 (October 2013).
     * The Netty rewrite — biggest wire format change in MC history. VarInt-framed
     * packets, connection states (Handshaking/Status/Login/Play), all packet IDs
     * remapped, strings changed from short+UTF-16BE to VarInt+UTF-8, slot data NBT
     * changed from short(-1) to byte(0x00) for empty NBT, S2C Player Position lost
     * the separate stance field. Protocol v4 clashes with Alpha 1.2.2 — safe because
     * 1.7.2 is detected by VarInt framing (not Handshake byte), and
     * fromNumber(4, Family.RELEASE) disambiguates.
     * Real MC protocol version 4.
     */
    RELEASE_1_7_2(4, 33, Family.RELEASE, "Release 1.7.2 (v4)", 92),

    /**
     * Minecraft Release 1.7.6-1.7.10 (April 2014).
     * SpawnPlayer (0x0C) gained a property list (VarInt count + entries) between
     * playerName and coordinates. All other packets identical to v4.
     * Protocol v5 clashes with Alpha 1.2.3 — safe because 1.7.6 is detected by
     * VarInt framing, and fromNumber(5, Family.RELEASE) disambiguates.
     * Real MC protocol version 5.
     */
    RELEASE_1_7_6(5, 34, Family.RELEASE, "Release 1.7.6 (v5)", 92),

    /**
     * Minecraft Release 1.8-1.8.9 (September 2014).
     * Extensive wire format changes from 1.7: Position packed longs, VarInt entity IDs,
     * restructured PlayerListItem (action-based with UUID), new chunk section format
     * (ushort blockStates instead of byte blocks + nibble metadata), item slot NBT uses
     * byte(0x00) TAG_End instead of short(-1), removed stance from C2S position packets,
     * S2C PlayerPositionAndLook uses byte flags instead of boolean onGround.
     * Netty protocol version 47 (clashes with pre-Netty Release 1.4.2 v47 — disambiguated
     * in NettyConnectionHandler by hardcoding pv==47 to RELEASE_1_8 for Netty clients).
     */
    RELEASE_1_8(47, 35, Family.RELEASE, "Release 1.8 (v47)", 92),

    /**
     * Minecraft Release 1.9 (February 2016).
     * Biggest protocol change since the 1.7 Netty rewrite: ALL Play state packet IDs
     * renumbered, entity positions switched from fixed-point int to double, entity
     * metadata restructured (VarInt type IDs, 0xFF terminator), chunks use paletted
     * block storage, new mandatory packets (TeleportConfirm, UnloadChunk, UseItem).
     * Netty protocol version 107.
     */
    RELEASE_1_9(107, 36, Family.RELEASE, "Release 1.9 (v107)", 92),

    /**
     * Minecraft Release 1.9.1 (March 2016).
     * Wire format identical to v107. Pure protocol version bump.
     * Netty protocol version 108.
     */
    RELEASE_1_9_1(108, 37, Family.RELEASE, "Release 1.9.1 (v108)", 92),

    /**
     * Minecraft Release 1.9.2 (March 2016).
     * Wire format identical to v107. Pure protocol version bump.
     * Netty protocol version 109.
     */
    RELEASE_1_9_2(109, 38, Family.RELEASE, "Release 1.9.2 (v109)", 92),

    /**
     * Minecraft Release 1.9.4 (May 2016).
     * Wire format identical to v107. Pure protocol version bump.
     * Netty protocol version 110.
     */
    RELEASE_1_9_4(110, 39, Family.RELEASE, "Release 1.9.4 (v110)", 92),

    /**
     * Minecraft Release 1.10-1.10.2 (June 2016).
     * Wire format identical to v110 — zero packet ID or format changes.
     * Only data-level changes: sound pitch byte→float (we don't send sounds),
     * resource pack status hash removed (we don't handle), new sound IDs.
     * Netty protocol version 210.
     */
    RELEASE_1_10(210, 40, Family.RELEASE, "Release 1.10.2 (v210)", 92),

    /**
     * Minecraft Release 1.11 (November 2016).
     * Only wire format change relevant to us: C2S Block Placement cursor
     * fields changed from unsigned bytes to floats.
     * All packet IDs identical to v210. Netty protocol version 315.
     */
    RELEASE_1_11(315, 41, Family.RELEASE, "Release 1.11 (v315)", 92),

    /**
     * Minecraft Release 1.11.1-1.11.2 (December 2016).
     * Wire format identical to v315. Pure protocol version bump.
     * Netty protocol version 316.
     */
    RELEASE_1_11_2(316, 42, Family.RELEASE, "Release 1.11.2 (v316)", 92),

    /**
     * Minecraft Release 1.12 (June 2017).
     * 3 new S2C packets (Recipe, SelectAdvancementsTab, UpdateAdvancements) and
     * 3 new C2S packets (CraftingRecipePlacement, RecipeBookUpdate, SeenAdvancements)
     * cause all Play state packet IDs to reshuffle. Entity movement packets shift +1,
     * DestroyEntities +1, SpawnPosition +2, EntityTeleport +2, EntityProperties +3.
     * C2S movement packets reordered (OnGround before Position).
     * Netty protocol version 335.
     */
    RELEASE_1_12(335, 43, Family.RELEASE, "Release 1.12 (v335)", 92),

    /**
     * Minecraft Release 1.12.1 (August 2017).
     * New S2C PlaceGhostRecipe (0x2B) shifts PlayerAbilities and later packets +1.
     * C2S CraftingRecipePlacement (0x01) removed, PlaceRecipe added at 0x12.
     * Netty protocol version 338.
     */
    RELEASE_1_12_1(338, 44, Family.RELEASE, "Release 1.12.1 (v338)", 92),

    /**
     * Minecraft Release 1.12.2 (September 2017).
     * KeepAlive changed from VarInt to Long (8 bytes) for both C2S and S2C.
     * All packet IDs identical to v338. Netty protocol version 340.
     */
    RELEASE_1_12_2(340, 45, Family.RELEASE, "Release 1.12.2 (v340)", 92),

    /**
     * Minecraft Release 1.13-1.13.1 (July 2018) - "The Flattening".
     * Biggest DATA format change in MC history: numeric blockId<<4|metadata replaced
     * by global block state IDs, item damage removed from slot wire format, item IDs
     * renumbered. All Play state packet IDs reshuffled. New mandatory S2C packets
     * (DeclareCommands, UpdateRecipes, UpdateTags). Chunk biome array changed from
     * byte[256] to int[256]. Global palette bitsPerBlock changed from 13 to 14.
     * Netty protocol version 393.
     */
    RELEASE_1_13(393, 46, Family.RELEASE, "Release 1.13 (v393)", 92),

    /**
     * Minecraft Release 1.13.1.
     * No packet format changes from 1.13. Same S2C/C2S packet IDs and wire formats.
     * Netty protocol version 401.
     */
    RELEASE_1_13_1(401, 47, Family.RELEASE, "Release 1.13.1 (v401)", 92),

    /**
     * Minecraft Release 1.13.2.
     * Item slot wire format changed: boolean present + VarInt itemId replaces
     * short itemId (-1 = empty). All other packets and IDs unchanged from 1.13.
     * Netty protocol version 404.
     */
    RELEASE_1_13_2(404, 48, Family.RELEASE, "Release 1.13.2 (v404)", 92),

    /**
     * Minecraft Release 1.14 (April 2019).
     * All S2C/C2S packet IDs reshuffled. Chunk sections no longer include light
     * data (moved to a new UpdateLight packet). Heightmaps added as NBT in the
     * chunk packet. Short blockCount added per chunk section. ViewDistance field
     * added to JoinGame. Difficulty removed from JoinGame. New mandatory packets:
     * SetChunkCacheCenter, SetChunkCacheRadius, UpdateLight.
     * Item slot format unchanged from v404 (boolean+VarInt).
     * Netty protocol version 477.
     */
    RELEASE_1_14(477, 49, Family.RELEASE, "Release 1.14 (v477)", 92),

    /**
     * Minecraft Release 1.14.1 (May 2019).
     * Wire format identical to v477 — same packet IDs, same wire formats.
     * Only data-level changes: Villager/Wandering Trader entity metadata indices
     * shifted (irrelevant — we don't send entity metadata for mobs).
     * Netty protocol version 480.
     */
    RELEASE_1_14_1(480, 50, Family.RELEASE, "Release 1.14.1 (v480)", 92),

    /**
     * Minecraft Release 1.14.2 (May 2019).
     * Wire format identical to v477 — same packet IDs, same wire formats.
     * Pure protocol version bump (bug fixes only).
     * Netty protocol version 485.
     */
    RELEASE_1_14_2(485, 51, Family.RELEASE, "Release 1.14.2 (v485)", 92),

    /**
     * Minecraft Release 1.14.3 (June 2019).
     * Only wire format change: Trade List (0x27) gained a boolean field at the end.
     * We don't send this packet, so effectively wire-compatible with v477.
     * Netty protocol version 490.
     */
    RELEASE_1_14_3(490, 52, Family.RELEASE, "Release 1.14.3 (v490)", 92),

    /**
     * Minecraft Release 1.14.4 (July 2019).
     * Only wire format change: Trade List (0x27) field order changed.
     * We don't send this packet, so effectively wire-compatible with v477.
     * Netty protocol version 498.
     */
    RELEASE_1_14_4(498, 53, Family.RELEASE, "Release 1.14.4 (v498)", 92),

    /**
     * Minecraft Release 1.15 (December 2019).
     * New BLOCK_BREAK_ACK (0x08) S2C packet shifts all S2C IDs from 0x08 onward +1.
     * SpawnPlayer removed entity metadata. JoinGame added hashedSeed + enableRespawnScreen.
     * Biomes changed from int[256] to int[1024] (3D biome storage).
     * Netty protocol version 573.
     */
    RELEASE_1_15(573, 54, Family.RELEASE, "Release 1.15 (v573)", 92),

    /**
     * Minecraft Release 1.15.1 (December 2019).
     * Wire format identical to v573 — same packet IDs, same wire formats.
     * Pure protocol version bump (bug fixes only).
     * Netty protocol version 575.
     */
    RELEASE_1_15_1(575, 55, Family.RELEASE, "Release 1.15.1 (v575)", 92),

    /**
     * Minecraft Release 1.15.2 (January 2020).
     * Wire format identical to v573 — same packet IDs, same wire formats.
     * Pure protocol version bump (bug fixes only).
     * Netty protocol version 578.
     */
    RELEASE_1_15_2(578, 56, Family.RELEASE, "Release 1.15.2 (v578)", 92),

    /**
     * Minecraft Release 1.16 (June 2020) - "The Nether Update".
     * JoinGame completely rewritten with NBT dimension codec + dimension type.
     * Chunk bit-packing changed from spanning to non-spanning.
     * Chat S2C gained UUID sender field. UpdateLight gained trustEdges boolean.
     * ADD_GLOBAL_ENTITY removed; SpawnPosition relocated.
     * C2S: GENERATE_JIGSAW inserted at 0x0F, shifting all C2S >= 0x0F by +1.
     * Netty protocol version 735.
     */
    RELEASE_1_16(735, 57, Family.RELEASE, "Release 1.16 (v735)", 92),

    /**
     * Minecraft Release 1.16.1 (June 2020).
     * Wire format identical to v735 — same packet IDs, same wire formats.
     * Pure protocol version bump (client-side Realms hotfix).
     * Netty protocol version 736.
     */
    RELEASE_1_16_1(736, 58, Family.RELEASE, "Release 1.16.1 (v736)", 92),

    /**
     * Minecraft Release 1.16.2 (August 2020).
     * JoinGame rewritten: isHardcore separated from gameMode, dimension changed from
     * String to NBT compound, dimension codec uses registry format with biome registry,
     * maxPlayers changed from unsigned byte to VarInt. MapChunk removed ignoreOldLightData,
     * biomes changed from raw int[] to VarInt-length-prefixed VarInt array.
     * S2C: CHUNK_BLOCKS_UPDATE removed at 0x0F, SECTION_BLOCKS_UPDATE inserted at 0x3B.
     * C2S: RECIPE_BOOK_UPDATE split into two packets, shifting >= 0x1F by +1.
     * Netty protocol version 751.
     */
    RELEASE_1_16_2(751, 59, Family.RELEASE, "Release 1.16.2 (v751)", 92),

    /**
     * Bug-fix release with no wire format changes from 1.16.2.
     * Netty protocol version 753.
     */
    RELEASE_1_16_3(753, 60, Family.RELEASE, "Release 1.16.3 (v753)", 92),

    /**
     * Release 1.16.4 / 1.16.5 (November 2020 / January 2021).
     * Bug-fix release with no wire format changes from 1.16.3.
     * Netty protocol version 754.
     */
    RELEASE_1_16_4(754, 61, Family.RELEASE, "Release 1.16.4 (v754)", 92),

    /**
     * Minecraft Release 1.17 (June 2021) - "Caves & Cliffs: Part I".
     * JoinGame dimension types gain min_y/height fields. Chunk bitmasks changed from
     * VarInt to BitSet (VarInt longCount + long[]). UpdateLight masks also BitSet with
     * array count prefixes. UpdateTags switched to registry-based format. DestroyEntities
     * changed to single-entity format. ConfirmTransaction (0x11) removed from C2S.
     * Many new S2C packets (ADD_VIBRATION_SIGNAL, CLEAR_TITLES, INITIALIZE_BORDER,
     * PING, PLAYER_COMBAT split, SET_BORDER split, SET_TITLE/SUBTITLE/ANIMATION split)
     * shift S2C IDs by +1 to +11. Block state IDs shifted due to deepslate ores, copper,
     * amethyst, candles, dripstone, etc.
     * Netty protocol version 755.
     */
    RELEASE_1_17(755, 62, Family.RELEASE, "Release 1.17 (v755)", 92),

    /**
     * Minecraft Release 1.17.1 (July 2021) - hotfix release.
     * Wire format changes from 1.17:
     * - DestroyEntities (0x3A) reverted from single-entity format (VarInt entityId) back
     *   to multi-entity format (VarInt count + VarInt[] entityIds), matching pre-1.17.
     * - SetSlot (0x16) gained VarInt stateId before windowId for inventory synchronization.
     * No packet ID shifts, no block state/item ID changes.
     * Netty protocol version 756.
     */
    RELEASE_1_17_1(756, 63, Family.RELEASE, "Release 1.17.1 (v756)", 92),

    /**
     * Minecraft Release 1.18-1.18.1 (November 2021) - "Caves & Cliffs: Part II".
     * Chunk Data and Update Light merged into a single combined packet. Biomes moved
     * from a top-level chunk field into per-section biome paletted containers.
     * primaryBitMask removed (all sections must be present). JoinGame gained
     * VarInt simulationDistance after viewDistance. No packet ID shifts.
     * Block state/item IDs unchanged from 1.17.
     * Netty protocol version 757.
     */
    RELEASE_1_18(757, 64, Family.RELEASE, "Release 1.18 (v757)", 92),

    /**
     * Minecraft Release 1.18.2 (February 2022).
     * Minimal changes from 1.18: infiniburn fields in dimension type NBT gain a '#'
     * prefix (tag reference format). One new block tag: fall_damage_resetting.
     * No packet ID shifts, no block state/item ID changes, no chunk format changes.
     * Netty protocol version 758.
     */
    RELEASE_1_18_2(758, 65, Family.RELEASE, "Release 1.18.2 (v758)", 92),

    /**
     * Minecraft Release 1.19 (June 2022) - "The Wild Update".
     * Major protocol overhaul: S2C/C2S packet IDs reshuffled, chat system rewritten
     * (ChatMessage split into SystemChat + PlayerChat), AcknowledgePlayerDigging replaced
     * by BlockChangedAck (VarInt sequenceId only), C2S digging/placement gain sequence field,
     * new ChatCommand C2S packet, JoinGame dimension type changed from NBT to String identifier,
     * LoginSuccess gains empty property array, block state IDs shifted for most blocks.
     * Netty protocol version 759.
     */
    RELEASE_1_19(759, 66, Family.RELEASE, "Release 1.19 (v759)", 93),

    /**
     * Minecraft Release 1.19.1-1.19.2 (August 2022).
     * Chat signing/reporting system overhaul. 3 new S2C packets (CUSTOM_CHAT_COMPLETIONS,
     * DELETE_CHAT, PLAYER_CHAT_HEADER) and 1 new C2S packet (CHAT_ACK) shift packet IDs.
     * SystemChat changed from VarInt type to Boolean overlay. JoinGame chat_type registry
     * expanded from 1 to 7 entries. No block state, chunk format, or tag changes.
     * Netty protocol version 760.
     */
    RELEASE_1_19_1(760, 67, Family.RELEASE, "Release 1.19.1 (v760)", 93),

    /**
     * Minecraft Release 1.19.3 (December 2022).
     * Chat system simplified: CHAT_PREVIEW, CUSTOM_SOUND, PLAYER_CHAT_HEADER,
     * SET_DISPLAY_CHAT_PREVIEW removed. PlayerInfo split into PlayerInfoRemove +
     * PlayerInfoUpdate (bitmask-based actions). New UpdateEnabledFeatures packet.
     * C2S: CHAT_PREVIEW removed, CHAT_SESSION_UPDATE added.
     * Block state IDs unchanged from V759/V760. Chunk format unchanged.
     * Netty protocol version 761.
     */
    RELEASE_1_19_3(761, 68, Family.RELEASE, "Release 1.19.3 (v761)", 93),

    /**
     * Minecraft Release 1.19.4 (March 2023).
     * 4 new S2C packets (BUNDLE_DELIMITER, CHUNKS_BIOMES, DAMAGE_EVENT, HURT_ANIMATION)
     * shift S2C IDs by +1 to +4. C2S: CHAT_SESSION_UPDATE relocated from 0x20 to 0x06,
     * shifting 0x06-0x1F by +1. PlayerPosition loses dismountVehicle boolean.
     * JoinGame gains minecraft:damage_type registry. Biome precipitation String replaced
     * by has_precipitation Byte. Block state IDs and chunk format unchanged from V761.
     * Netty protocol version 762.
     */
    RELEASE_1_19_4(762, 69, Family.RELEASE, "Release 1.19.4 (v762)", 93),

    /**
     * Minecraft Release 1.20-1.20.1 (June 2023) - "Trails & Tales".
     * Very lightweight protocol upgrade from 1.19.4. No S2C or C2S packet ID changes.
     * JoinGame gains VarInt portalCooldown at the end. UpdateTags has block/item tag
     * additions (bamboo_blocks, cherry_logs, hanging signs, etc.) and one block tag
     * removal (replaceable_plants). Block state IDs, chunk format, item IDs unchanged.
     * Netty protocol version 763.
     */
    RELEASE_1_20(763, 70, Family.RELEASE, "Release 1.20 (v763)", 93),

    /**
     * Minecraft Release 1.20.2 (September 2023).
     * Largest structural protocol change since 1.7.2: new CONFIGURATION connection state
     * between LOGIN and PLAY. Dimension codec moved from JoinGame to Configuration-phase
     * RegistryData packets. SpawnPlayer removed (merged into SpawnEntity). Chunk batch
     * system added. Extensive S2C/C2S packet ID shifts. Block state IDs unchanged for
     * our basic block set.
     * Netty protocol version 764.
     */
    RELEASE_1_20_2(764, 71, Family.RELEASE, "Release 1.20.2 (v764)", 93),

    /**
     * Minecraft Release 1.20.3-1.20.4 (December 2023).
     * Text components switched from JSON strings to NBT tags across all packets.
     * RegistryData switched from single CompoundTag to per-registry packets.
     * S2C shifts: 3 new packets at 0x42-0x44 (+2), 2 new at 0x6E-0x6F (+4 for tail).
     * C2S: CONTAINER_SLOT_STATE_CHANGED inserted at 0x0F (+1 for >= 0x0F).
     * Block state IDs unchanged for our basic block set. Chunk format unchanged.
     * Netty protocol version 765.
     */
    RELEASE_1_20_3(765, 72, Family.RELEASE, "Release 1.20.3 (v765)", 93),

    /**
     * Minecraft Release 1.20.5-1.20.6 (April 2024).
     * RegistryData switched to per-registry per-entry format with Boolean hasData.
     * JoinGame dimensionType changed from String to VarInt. Item slot format switched
     * to data components (count-first, no boolean present). LoginSuccess gained
     * strictErrorHandling boolean. New SelectKnownPacks round-trip in CONFIG state.
     * 5 new S2C PLAY packets shift IDs by +1 to +4. 3 new C2S packets shift +1 to +3.
     * Block state IDs unchanged for our basic block set. Chunk format unchanged.
     * Netty protocol version 766.
     */
    RELEASE_1_20_5(766, 73, Family.RELEASE, "Release 1.20.5 (v766)", 93),

    /**
     * Minecraft Release 1.21-1.21.1 (June 2024) - "Tricky Trials".
     * ALL Play S2C and C2S packet IDs identical to v766. Only CONFIG-phase
     * differences: new enchantment tag registry, updated tags, new damage type
     * (campfire), 9 wolf variants. Wire format changes only affect packets we
     * don't send (attribute modifiers UUID->String, particle system).
     * Netty protocol version 767.
     */
    RELEASE_1_21(767, 74, Family.RELEASE, "Release 1.21 (v767)", 93),

    /**
     * Minecraft Release 1.21.2-1.21.3 (October 2024).
     * Massive S2C ID shifts (7 new packets, 1 removed), 2 new C2S packets.
     * PlayerPosition rewritten (delta velocities, int flags). EntityTeleport
     * replaced by EntityPositionSync (float degrees, delta velocities).
     * JoinGame gains VarInt seaLevel. TimeUpdate gains boolean doDaylightCycle.
     * LoginSuccess removes strictErrorHandling. New instrument registry.
     * 20 new entity types shift player from 128→148.
     * Netty protocol version 768.
     */
    RELEASE_1_21_2(768, 75, Family.RELEASE, "Release 1.21.2 (v768)", 93),

    /**
     * Minecraft Release 1.21.4 (December 2024).
     * Minimal version bump from 1.21.2: NO S2C packet ID changes. C2S shifts:
     * PICK_ITEM split into PICK_ITEM_FROM_BLOCK + PICK_ITEM_FROM_ENTITY,
     * PLAYER_LOADED added. 1 entity type removed (creaking_transient), shifting
     * player from 148→147. Tag updates: block +bee_attractive -tall_flowers,
     * item +5 weapon preference tags -flowers -tall_flowers -trim_templates.
     * Netty protocol version 769.
     */
    RELEASE_1_21_4(769, 76, Family.RELEASE, "Release 1.21.4 (v769)", 93),

    /**
     * Minecraft Release 1.21.5 (2025).
     * ADD_EXPERIENCE_ORB removed at S2C 0x02, shifting 0x03-0x78 by -1.
     * C2S: SET_TEST_BLOCK at 0x39 (+1), TEST_INSTANCE_BLOCK_ACTION at 0x3D (+2).
     * Chunk format: heightmaps changed from NBT to binary, section palette data
     * arrays no longer have VarInt length prefix. lingering_potion inserted at
     * entity type 100 (player: 147 -> 148). 4 new registries: pig_variant,
     * cow_variant, chicken_variant, wolf_sound_variant.
     * Netty protocol version 770.
     */
    RELEASE_1_21_5(770, 77, Family.RELEASE, "Release 1.21.5 (v770)", 93),

    /**
     * Minecraft Release 1.21.6 "Chase the Skies".
     * C2S: CHANGE_GAME_MODE inserted at 0x04 (+1 shift). S2C: no existing ID shifts.
     * happy_ghast inserted at entity type 56 (player: 148 -> 149).
     * 1 new registry: dialog. Tag changes: block +3 net, item +3, entity_type +2.
     * Netty protocol version 771.
     */
    RELEASE_1_21_6(771, 78, Family.RELEASE, "Release 1.21.6 (v771)", 94),

    /**
     * Minecraft Release 1.21.7-1.21.8 (2025).
     * Hotfix release with no packet ID changes, no entity type changes, no tag changes,
     * no chunk format changes. Only content additions: 1 new painting (dennis, 3x3)
     * and 1 new music disc (lava_chicken). Protocol version 772 shared with 1.21.8.
     * Netty protocol version 772.
     */
    RELEASE_1_21_7(772, 79, Family.RELEASE, "Release 1.21.7 (v772)", 94),

    /**
     * Minecraft Release 1.21.9-1.21.10 (2025) - "The Copper Age".
     * 5 new S2C PLAY packets (4 debug + 1 test) shift S2C IDs by +4/+5.
     * SpawnEntity velocity changed from 3 shorts to MOVEMENT_VECTOR (zero = byte 0x00).
     * SpawnPosition changed to GlobalBlockPosition (String dimension + Position + 2 floats).
     * copper_golem and mannequin inserted (player: 149 -> 151).
     * No C2S packet ID changes. No chunk format changes.
     * Netty protocol version 773.
     */
    RELEASE_1_21_9(773, 80, Family.RELEASE, "Release 1.21.9 (v773)", 94),

    /**
     * Minecraft Bedrock Edition (1.26.0+).
     * Uses UDP/RakNet on port 19132 with a completely different protocol.
     * Protocol version 924 matches the CloudburstMC codec for 1.26.0.
     * Block count uses the same internal 0-91 range as Alpha.
     */
    BEDROCK(924, 81, Family.BEDROCK, "Bedrock", 92);

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
            case 4:  return "Alpha 1.2.2 (or Release 1.7.2-1.7.5)";
            case 5:  return "Alpha 1.2.3_01-1.2.3_04 (or Release 1.7.6-1.7.10)";
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
            case 47: return "Release 1.4.2 (or Release 1.8-1.8.9 Netty)";
            case 49: return "Release 1.4.4-1.4.5";
            case 107: return "Release 1.9";
            case 108: return "Release 1.9.1";
            case 109: return "Release 1.9.2";
            case 110: return "Release 1.9.4";
            case 210: return "Release 1.10-1.10.2";
            case 315: return "Release 1.11";
            case 316: return "Release 1.11.1-1.11.2";
            case 335: return "Release 1.12";
            case 338: return "Release 1.12.1";
            case 340: return "Release 1.12.2";
            case 393: return "Release 1.13";
            case 401: return "Release 1.13.1";
            case 404: return "Release 1.13.2";
            case 477: return "Release 1.14";
            case 480: return "Release 1.14.1";
            case 485: return "Release 1.14.2";
            case 490: return "Release 1.14.3";
            case 498: return "Release 1.14.4";
            case 573: return "Release 1.15";
            case 575: return "Release 1.15.1";
            case 578: return "Release 1.15.2";
            case 735: return "Release 1.16";
            case 736: return "Release 1.16.1";
            case 751: return "Release 1.16.2";
            case 753: return "Release 1.16.3";
            case 754: return "Release 1.16.4";
            case 755: return "Release 1.17";
            case 756: return "Release 1.17.1";
            case 757: return "Release 1.18";
            case 758: return "Release 1.18.2";
            case 759: return "Release 1.19";
            case 760: return "Release 1.19.1-1.19.2";
            case 761: return "Release 1.19.3";
            case 51: return "Release 1.4.6-1.4.7";
            case 60: return "Release 1.5-1.5.1";
            case 61: return "Release 1.5.2";
            case 73: return "Release 1.6.1";
            case 74: return "Release 1.6.2";
            case 78: return "Release 1.6.4";
            case 762: return "Release 1.19.4";
            case 763: return "Release 1.20-1.20.1";
            case 764: return "Release 1.20.2";
            case 765: return "Release 1.20.3-1.20.4";
            case 766: return "Release 1.20.5-1.20.6";
            case 767: return "Release 1.21-1.21.1";
            case 768: return "Release 1.21.2-1.21.3";
            case 769: return "Release 1.21.4";
            case 770: return "Release 1.21.5";
            case 771: return "Release 1.21.6";
            case 772: return "Release 1.21.7-1.21.8";
            case 773: return "Release 1.21.9-1.21.10";
            default: return null;
        }
    }

    /**
     * Describe the game versions associated with a Netty (1.7.2+) protocol number.
     * Returns the game version range string (e.g. "1.8-1.8.9" for v47),
     * or null if the number is not a known Netty protocol version.
     *
     * Includes versions beyond what the server supports for gameplay,
     * so that server list pings from newer clients still log clearly.
     */
    public static String describeNettyProtocol(int number) {
        switch (number) {
            case 4:   return "1.7.2-1.7.5";
            case 5:   return "1.7.6-1.7.10";
            case 47:  return "1.8-1.8.9";
            case 107: return "1.9";
            case 108: return "1.9.1";
            case 109: return "1.9.2";
            case 110: return "1.9.3-1.9.4";
            case 210: return "1.10-1.10.2";
            case 315: return "1.11";
            case 316: return "1.11.1-1.11.2";
            case 335: return "1.12";
            case 338: return "1.12.1";
            case 340: return "1.12.2";
            case 393: return "1.13";
            case 401: return "1.13.1";
            case 404: return "1.13.2";
            case 477: return "1.14";
            case 480: return "1.14.1";
            case 485: return "1.14.2";
            case 490: return "1.14.3";
            case 498: return "1.14.4";
            case 573: return "1.15";
            case 575: return "1.15.1";
            case 578: return "1.15.2";
            case 735: return "1.16";
            case 736: return "1.16.1";
            case 751: return "1.16.2";
            // Beyond supported gameplay versions (ping-only)
            case 753: return "1.16.3";
            case 754: return "1.16.4-1.16.5";
            case 755: return "1.17";
            case 756: return "1.17.1";
            case 757: return "1.18-1.18.1";
            case 758: return "1.18.2";
            case 759: return "1.19";
            case 760: return "1.19.1-1.19.2";
            case 761: return "1.19.3";
            case 762: return "1.19.4";
            case 763: return "1.20-1.20.1";
            case 764: return "1.20.2";
            case 765: return "1.20.3-1.20.4";
            case 766: return "1.20.5-1.20.6";
            case 767: return "1.21-1.21.1";
            case 768: return "1.21.2-1.21.3";
            case 769: return "1.21.4";
            case 770: return "1.21.5";
            case 771: return "1.21.6";
            case 772: return "1.21.7-1.21.8";
            case 773: return "1.21.9-1.21.10";
            default:  return null;
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

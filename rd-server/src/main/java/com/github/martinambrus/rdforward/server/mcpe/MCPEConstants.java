package com.github.martinambrus.rdforward.server.mcpe;

/**
 * Protocol constants for MCPE 0.7.0 (protocol version 11).
 * Includes RakNet wire constants and game-level packet IDs.
 */
public final class MCPEConstants {

    private MCPEConstants() {}

    // --- Protocol ---
    public static final int MCPE_PROTOCOL_VERSION_9 = 9;   // 0.6.1
    public static final int MCPE_PROTOCOL_VERSION_11 = 11; // 0.7.0-0.7.3
    public static final int MCPE_PROTOCOL_VERSION_12 = 12; // 0.7.4-0.7.6
    public static final int MCPE_PROTOCOL_VERSION_14 = 14; // 0.8.0 (13 was dev-only)
    public static final int MCPE_PROTOCOL_VERSION_17 = 17; // 0.9.0 (15-16 were dev-only)
    public static final int MCPE_PROTOCOL_VERSION_18 = 18; // 0.9.5
    public static final int MCPE_PROTOCOL_VERSION_20 = 20; // 0.10.0 (19 was pre-release)
    public static final int MCPE_PROTOCOL_VERSION_27 = 27; // 0.11.0 (21-26 were dev-only)
    public static final int MCPE_PROTOCOL_VERSION_34 = 34; // 0.12.1 (28-33 were dev-only)
    public static final int MCPE_PROTOCOL_VERSION_38 = 38; // 0.13.0 (35-37 were dev-only)
    public static final int MCPE_PROTOCOL_VERSION_45 = 45; // 0.14.0 (39-44 were dev-only)
    public static final int MCPE_PROTOCOL_VERSION_81 = 81; // 0.15.0 (46-80 were dev-only)
    /** Highest supported protocol version (for pong advertisement). */
    public static final int MCPE_PROTOCOL_VERSION_MAX = MCPE_PROTOCOL_VERSION_81;
    public static final String MCPE_VERSION_STRING = "0.15.0";
    public static final int DEFAULT_PORT = 19132;

    // --- RakNet ---
    public static final byte[] RAKNET_MAGIC = {
        0x00, (byte) 0xFF, (byte) 0xFF, 0x00,
        (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
        (byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD,
        0x12, 0x34, 0x56, 0x78
    };
    public static final int RAKNET_MAGIC_LENGTH = 16;

    /** RakNet protocol version used by MCPE 0.7.x (6) and 0.6.x (5). Both accepted. */
    public static final byte RAKNET_PROTOCOL_VERSION = 6;

    // RakNet unconnected packet IDs
    public static final byte UNCONNECTED_PING         = 0x01;
    public static final byte UNCONNECTED_PING_OPEN    = 0x02;
    public static final byte UNCONNECTED_PONG         = 0x1C;
    public static final byte OPEN_CONNECTION_REQUEST_1 = 0x05;
    public static final byte OPEN_CONNECTION_REPLY_1   = 0x06;
    public static final byte OPEN_CONNECTION_REQUEST_2 = 0x07;
    public static final byte OPEN_CONNECTION_REPLY_2   = 0x08;

    // RakNet connected packet IDs
    public static final byte CLIENT_CONNECT     = 0x09;
    public static final byte SERVER_HANDSHAKE   = 0x10;
    public static final byte CLIENT_HANDSHAKE   = 0x13;
    public static final byte CLIENT_DISCONNECT  = 0x15;
    public static final byte ACK                = (byte) 0xC0;
    public static final byte NACK               = (byte) 0xA0;

    /** Data packets (0x80-0x8F) — any ID in this range carries encapsulated payloads. */
    public static final byte DATA_PACKET_MIN = (byte) 0x80;
    public static final byte DATA_PACKET_MAX = (byte) 0x8F;

    // RakNet reliability types (top 3 bits of encapsulation flags byte)
    public static final byte UNRELIABLE            = 0;
    public static final byte UNRELIABLE_SEQUENCED  = 1;
    public static final byte RELIABLE              = 2;
    public static final byte RELIABLE_ORDERED      = 3;
    public static final byte RELIABLE_SEQUENCED    = 4;

    // --- v9 unique packets (0.6.1 — no SetEntityLink, IDs 0xA8+ shift -1 from v11) ---
    /** v9: C2S block placement packet (removed in v11, replaced by UseItem). */
    public static final byte V9_PLACE_BLOCK      = (byte) 0x95;
    /** v9: C2S chat message (separate from S2C MESSAGE at 0x85; merged in v11). */
    public static final byte V9_CLIENT_MESSAGE    = (byte) 0xB4;

    // --- MCPE Game Packets (0x82-0xB9 + 0xFF) ---
    public static final byte LOGIN               = (byte) 0x82;
    public static final byte LOGIN_STATUS        = (byte) 0x83;
    public static final byte READY               = (byte) 0x84;
    public static final byte MESSAGE             = (byte) 0x85;
    public static final byte SET_TIME            = (byte) 0x86;
    public static final byte START_GAME          = (byte) 0x87;
    public static final byte ADD_MOB             = (byte) 0x88;
    public static final byte ADD_PLAYER          = (byte) 0x89;
    public static final byte REMOVE_PLAYER       = (byte) 0x8A;
    // 0x8B unused
    public static final byte ADD_ENTITY          = (byte) 0x8C;
    public static final byte REMOVE_ENTITY       = (byte) 0x8D;
    public static final byte ADD_ITEM_ENTITY     = (byte) 0x8E;
    public static final byte TAKE_ITEM_ENTITY    = (byte) 0x8F;
    public static final byte MOVE_ENTITY         = (byte) 0x90; // batch header / empty
    // 0x91, 0x92 unused
    public static final byte MOVE_ENTITY_POSROT  = (byte) 0x93;
    /** v14 only: RotateHeadPacket inserted at 0x94 (int eid, byte headYaw). */
    public static final byte ROTATE_HEAD_V14     = (byte) 0x94;
    // All packet IDs below are v12 canonical. In v14, IDs >= 0x94 shift +1 on the wire.
    public static final byte MOVE_PLAYER         = (byte) 0x94;
    // 0x95 unused
    public static final byte REMOVE_BLOCK        = (byte) 0x96;
    public static final byte UPDATE_BLOCK        = (byte) 0x97;
    public static final byte ADD_PAINTING        = (byte) 0x98;
    public static final byte EXPLODE             = (byte) 0x99;
    public static final byte LEVEL_EVENT         = (byte) 0x9A;
    public static final byte TILE_EVENT          = (byte) 0x9B;
    public static final byte ENTITY_EVENT        = (byte) 0x9C;
    public static final byte REQUEST_CHUNK       = (byte) 0x9D;
    public static final byte CHUNK_DATA          = (byte) 0x9E;
    public static final byte PLAYER_EQUIPMENT    = (byte) 0x9F;
    public static final byte PLAYER_ARMOR        = (byte) 0xA0;
    public static final byte INTERACT            = (byte) 0xA1;
    public static final byte USE_ITEM            = (byte) 0xA2;
    public static final byte PLAYER_ACTION       = (byte) 0xA3;
    // 0xA4 unused
    public static final byte HURT_ARMOR          = (byte) 0xA5;
    public static final byte SET_ENTITY_DATA     = (byte) 0xA6;
    public static final byte SET_ENTITY_MOTION   = (byte) 0xA7;
    public static final byte SET_ENTITY_LINK     = (byte) 0xA8;
    public static final byte SET_HEALTH          = (byte) 0xA9;
    public static final byte SET_SPAWN_POSITION  = (byte) 0xAA;
    public static final byte ANIMATE             = (byte) 0xAB;
    public static final byte RESPAWN             = (byte) 0xAC;
    public static final byte SEND_INVENTORY      = (byte) 0xAD;
    public static final byte DROP_ITEM           = (byte) 0xAE;
    public static final byte CONTAINER_OPEN      = (byte) 0xAF;
    public static final byte CONTAINER_CLOSE     = (byte) 0xB0;
    public static final byte CONTAINER_SET_SLOT  = (byte) 0xB1;
    public static final byte CONTAINER_SET_DATA  = (byte) 0xB2;
    public static final byte CONTAINER_SET_CONTENT = (byte) 0xB3;
    // 0xB4 unused
    public static final byte CHAT                = (byte) 0xB5;
    // Protocol 11: 0xB6 = SIGN_UPDATE, 0xB7 = ADVENTURE_SETTINGS
    // Protocol 12: 0xB6 = ADVENTURE_SETTINGS, 0xB7 = ENTITY_DATA (SIGN_UPDATE removed)
    public static final byte SIGN_UPDATE         = (byte) 0xB6; // protocol 11 only
    public static final byte ADVENTURE_SETTINGS_V11 = (byte) 0xB7; // protocol 11
    public static final byte ADVENTURE_SETTINGS_V12 = (byte) 0xB6; // protocol 12+
    public static final byte ENTITY_DATA_V12     = (byte) 0xB7; // protocol 12+ (tile entity NBT)
    // 0xB8 unused
    public static final byte PLAYER_INPUT        = (byte) 0xB9;
    /** v17+: Full column chunk data (zlib compressed). Wire ID not shifted — v17-specific. */
    public static final byte FULL_CHUNK_DATA_V17 = (byte) 0xBA;
    /** v17+: Server tells client to unload a chunk. */
    public static final byte UNLOAD_CHUNK_V17    = (byte) 0xBB;
    public static final byte ROTATE_HEAD         = (byte) 0xFF;

    // --- v27 Game Packets (0x01-0x30) — completely renumbered from v11-v20 ---
    public static final byte V27_LOGIN               = 0x01;
    public static final byte V27_PLAY_STATUS         = 0x02;
    public static final byte V27_DISCONNECT          = 0x03;
    public static final byte V27_TEXT                = 0x04;
    public static final byte V27_SET_TIME            = 0x05;
    public static final byte V27_START_GAME          = 0x06;
    public static final byte V27_ADD_PLAYER          = 0x07;
    public static final byte V27_REMOVE_PLAYER       = 0x08;
    public static final byte V27_ADD_ENTITY          = 0x09;
    public static final byte V27_REMOVE_ENTITY       = 0x0A;
    public static final byte V27_ADD_ITEM_ENTITY     = 0x0B;
    public static final byte V27_TAKE_ITEM_ENTITY    = 0x0C;
    public static final byte V27_MOVE_ENTITY         = 0x0D;
    public static final byte V27_MOVE_PLAYER         = 0x0E;
    public static final byte V27_REMOVE_BLOCK        = 0x0F;
    public static final byte V27_UPDATE_BLOCK        = 0x10;
    public static final byte V27_ADD_PAINTING        = 0x11;
    public static final byte V27_EXPLODE             = 0x12;
    public static final byte V27_LEVEL_EVENT         = 0x13;
    public static final byte V27_TILE_EVENT          = 0x14;
    public static final byte V27_ENTITY_EVENT        = 0x15;
    public static final byte V27_MOB_EFFECT          = 0x16;
    public static final byte V27_PLAYER_EQUIPMENT    = 0x17;
    public static final byte V27_PLAYER_ARMOR        = 0x18;
    public static final byte V27_INTERACT            = 0x19;
    public static final byte V27_USE_ITEM            = 0x1A;
    public static final byte V27_PLAYER_ACTION       = 0x1B;
    public static final byte V27_HURT_ARMOR          = 0x1C;
    public static final byte V27_SET_ENTITY_DATA     = 0x1D;
    public static final byte V27_SET_ENTITY_MOTION   = 0x1E;
    public static final byte V27_SET_ENTITY_LINK     = 0x1F;
    public static final byte V27_SET_HEALTH          = 0x20;
    public static final byte V27_SET_SPAWN_POSITION  = 0x21;
    public static final byte V27_ANIMATE             = 0x22;
    public static final byte V27_RESPAWN             = 0x23;
    public static final byte V27_DROP_ITEM           = 0x24;
    public static final byte V27_CONTAINER_OPEN      = 0x25;
    public static final byte V27_CONTAINER_CLOSE     = 0x26;
    public static final byte V27_CONTAINER_SET_SLOT  = 0x27;
    public static final byte V27_CONTAINER_SET_DATA  = 0x28;
    public static final byte V27_CONTAINER_SET_CONTENT = 0x29;
    public static final byte V27_CONTAINER_ACK       = 0x2A;
    public static final byte V27_ADVENTURE_SETTINGS  = 0x2B;
    public static final byte V27_TILE_ENTITY_DATA    = 0x2C;
    public static final byte V27_PLAYER_INPUT        = 0x2D;
    public static final byte V27_FULL_CHUNK_DATA     = 0x2E;
    public static final byte V27_SET_DIFFICULTY      = 0x2F;
    /** BatchPacket — NOT renumbered with v27, stays at 0xB1 (same ID space as v11-v20). */
    public static final byte V27_BATCH               = (byte) 0xB1;

    /**
     * v45+ (0.14.0) wrapper byte: all game packets are prefixed with 0x8E on the wire.
     * Both C2S and S2C. Inner batch sub-packets are also prefixed.
     * This byte is NOT a packet ID — it is stripped on receive and prepended on send.
     */
    public static final byte V45_WRAPPER = (byte) 0x8E;

    /**
     * v81+ (0.15.0) wrapper byte: replaces 0x8E. All game packets prefixed with 0xFE.
     */
    public static final byte V81_WRAPPER = (byte) 0xFE;

    // --- v81 Game Packets (0x01-0x41) — completely renumbered from v34/v45 ---
    public static final byte V81_LOGIN               = 0x01;
    public static final byte V81_PLAY_STATUS         = 0x02;
    public static final byte V81_DISCONNECT          = 0x05;
    public static final byte V81_BATCH               = 0x06;
    public static final byte V81_TEXT                = 0x07;
    public static final byte V81_SET_TIME            = 0x08;
    public static final byte V81_START_GAME          = 0x09;
    public static final byte V81_ADD_PLAYER          = 0x0A;
    public static final byte V81_ADD_ENTITY          = 0x0B;
    public static final byte V81_REMOVE_ENTITY       = 0x0C;
    public static final byte V81_ADD_ITEM_ENTITY     = 0x0D;
    public static final byte V81_TAKE_ITEM_ENTITY    = 0x0E;
    public static final byte V81_MOVE_ENTITY         = 0x0F;
    public static final byte V81_MOVE_PLAYER         = 0x10;
    public static final byte V81_REMOVE_BLOCK        = 0x12;
    public static final byte V81_UPDATE_BLOCK        = 0x13;
    public static final byte V81_LEVEL_EVENT         = 0x16;
    public static final byte V81_ENTITY_EVENT        = 0x18;
    public static final byte V81_UPDATE_ATTRIBUTES   = 0x1A;
    public static final byte V81_MOB_EQUIPMENT       = 0x1B;
    public static final byte V81_MOB_ARMOR           = 0x1C;
    public static final byte V81_INTERACT            = 0x1E;
    public static final byte V81_USE_ITEM            = 0x1F;
    public static final byte V81_PLAYER_ACTION       = 0x20;
    public static final byte V81_SET_ENTITY_DATA     = 0x22;
    public static final byte V81_SET_ENTITY_MOTION   = 0x23;
    public static final byte V81_SET_HEALTH          = 0x25;
    public static final byte V81_SET_SPAWN_POSITION  = 0x26;
    public static final byte V81_ANIMATE             = 0x27;
    public static final byte V81_RESPAWN             = 0x28;
    public static final byte V81_CONTAINER_OPEN      = 0x2A;
    public static final byte V81_CONTAINER_CLOSE     = 0x2B;
    public static final byte V81_CONTAINER_SET_SLOT  = 0x2C;
    public static final byte V81_CONTAINER_SET_CONTENT = 0x2E;
    public static final byte V81_ADVENTURE_SETTINGS  = 0x31;
    public static final byte V81_PLAYER_INPUT        = 0x33;
    public static final byte V81_FULL_CHUNK_DATA     = 0x34;
    public static final byte V81_SET_DIFFICULTY      = 0x35;
    public static final byte V81_SET_PLAYER_GAMETYPE = 0x37;
    public static final byte V81_PLAYER_LIST         = 0x38;
    public static final byte V81_REQUEST_CHUNK_RADIUS = 0x3D;
    public static final byte V81_CHUNK_RADIUS_UPDATED = 0x3E;

    // --- v34 Game Packets (0x8F-0xC4) — renumbered again from v27 ---
    public static final byte V34_LOGIN               = (byte) 0x8F;
    public static final byte V34_PLAY_STATUS         = (byte) 0x90;
    public static final byte V34_DISCONNECT          = (byte) 0x91;
    public static final byte V34_BATCH               = (byte) 0x92;
    public static final byte V34_TEXT                = (byte) 0x93;
    public static final byte V34_SET_TIME            = (byte) 0x94;
    public static final byte V34_START_GAME          = (byte) 0x95;
    public static final byte V34_ADD_PLAYER          = (byte) 0x96;
    public static final byte V34_REMOVE_PLAYER       = (byte) 0x97;
    public static final byte V34_ADD_ENTITY          = (byte) 0x98;
    public static final byte V34_REMOVE_ENTITY       = (byte) 0x99;
    public static final byte V34_ADD_ITEM_ENTITY     = (byte) 0x9A;
    public static final byte V34_TAKE_ITEM_ENTITY    = (byte) 0x9B;
    public static final byte V34_MOVE_ENTITY         = (byte) 0x9C;
    public static final byte V34_MOVE_PLAYER         = (byte) 0x9D;
    public static final byte V34_REMOVE_BLOCK        = (byte) 0x9E;
    public static final byte V34_UPDATE_BLOCK        = (byte) 0x9F;
    public static final byte V34_ADD_PAINTING        = (byte) 0xA0;
    public static final byte V34_EXPLODE             = (byte) 0xA1;
    public static final byte V34_LEVEL_EVENT         = (byte) 0xA2;
    public static final byte V34_TILE_EVENT          = (byte) 0xA3;
    public static final byte V34_ENTITY_EVENT        = (byte) 0xA4;
    public static final byte V34_MOB_EFFECT          = (byte) 0xA5;
    public static final byte V34_UPDATE_ATTRIBUTES   = (byte) 0xA6; // NEW in v34
    public static final byte V34_MOB_EQUIPMENT       = (byte) 0xA7;
    public static final byte V34_MOB_ARMOR           = (byte) 0xA8;
    public static final byte V34_INTERACT            = (byte) 0xA9;
    public static final byte V34_USE_ITEM            = (byte) 0xAA;
    public static final byte V34_PLAYER_ACTION       = (byte) 0xAB;
    public static final byte V34_HURT_ARMOR          = (byte) 0xAC;
    public static final byte V34_SET_ENTITY_DATA     = (byte) 0xAD;
    public static final byte V34_SET_ENTITY_MOTION   = (byte) 0xAE;
    public static final byte V34_SET_ENTITY_LINK     = (byte) 0xAF;
    public static final byte V34_SET_HEALTH          = (byte) 0xB0;
    public static final byte V34_SET_SPAWN_POSITION  = (byte) 0xB1;
    public static final byte V34_ANIMATE             = (byte) 0xB2;
    public static final byte V34_RESPAWN             = (byte) 0xB3;
    public static final byte V34_DROP_ITEM           = (byte) 0xB4;
    public static final byte V34_CONTAINER_OPEN      = (byte) 0xB5;
    public static final byte V34_CONTAINER_CLOSE     = (byte) 0xB6;
    public static final byte V34_CONTAINER_SET_SLOT  = (byte) 0xB7;
    public static final byte V34_CONTAINER_SET_DATA  = (byte) 0xB8;
    public static final byte V34_CONTAINER_SET_CONTENT = (byte) 0xB9;
    public static final byte V34_CRAFTING_DATA       = (byte) 0xBA; // NEW in v34
    public static final byte V34_CRAFTING_EVENT      = (byte) 0xBB; // NEW in v34
    public static final byte V34_ADVENTURE_SETTINGS  = (byte) 0xBC;
    public static final byte V34_TILE_ENTITY_DATA    = (byte) 0xBD;
    public static final byte V34_PLAYER_INPUT        = (byte) 0xBE;
    public static final byte V34_FULL_CHUNK_DATA     = (byte) 0xBF;
    public static final byte V34_SET_DIFFICULTY      = (byte) 0xC0;
    public static final byte V34_CHANGE_DIMENSION    = (byte) 0xC1; // NEW in v34
    public static final byte V34_SET_PLAYER_GAMETYPE = (byte) 0xC2; // NEW in v34
    public static final byte V34_PLAYER_LIST         = (byte) 0xC3; // NEW in v34

    // --- Login status codes ---
    public static final int LOGIN_SUCCESS         = 0;
    public static final int LOGIN_CLIENT_OUTDATED = 1;
    public static final int LOGIN_SERVER_OUTDATED = 2;

    // --- Ready status codes ---
    public static final byte READY_SPAWN_REQUEST  = 1;
    public static final byte READY_CHUNK_LOADED   = 2;

    // --- Player action types ---
    public static final int ACTION_START_BREAK    = 0;
    public static final int ACTION_ABORT_BREAK    = 1;
    public static final int ACTION_STOP_BREAK     = 2;
    public static final int ACTION_RELEASE_ITEM   = 5;
    public static final int ACTION_STOP_SLEEPING  = 6;
    public static final int ACTION_RESPAWN        = 7;
    public static final int ACTION_JUMP           = 8;
    public static final int ACTION_START_SPRINT   = 9;
    public static final int ACTION_STOP_SPRINT    = 10;
    public static final int ACTION_START_SNEAK    = 11;
    public static final int ACTION_STOP_SNEAK     = 12;
    public static final int ACTION_CREATIVE_DESTROY = 13; // v34+: creative mode instant block destroy

    // --- World generator types ---
    public static final int GENERATOR_OLD      = 0;
    public static final int GENERATOR_INFINITE = 1;
    public static final int GENERATOR_FLAT     = 2;

    // --- Game modes ---
    public static final int GAMEMODE_SURVIVAL = 0;
    public static final int GAMEMODE_CREATIVE = 1;

    // --- Entity metadata types (values encoded in Little-Endian) ---
    public static final byte META_TYPE_BYTE     = 0;
    public static final byte META_TYPE_SHORT    = 1;
    public static final byte META_TYPE_INT      = 2;
    public static final byte META_TYPE_FLOAT    = 3;
    public static final byte META_TYPE_STRING   = 4;
    public static final byte META_TYPE_SLOT     = 5;
    public static final byte META_TYPE_POSITION = 6;
    public static final byte META_TERMINATOR    = 0x7F;

    // --- Entity metadata indices ---
    public static final int META_FLAGS          = 0;  // byte: bit 0=onFire, bit 4=sprinting
    public static final int META_AIR            = 1;  // short
    public static final int META_NAMETAG        = 2;  // string
    public static final int META_SHOW_NAMETAG   = 3;  // byte

    // --- Default skin for cross-protocol players (64x64 RGBA = 16384 bytes) ---
    // Real Steve skin extracted from Minecraft 1.8 client JAR (assets/minecraft/textures/entity/steve.png).
    public static final byte[] DEFAULT_SKIN_64x64 = loadSteveSkin();

    private static byte[] loadSteveSkin() {
        try (java.io.InputStream is = MCPEConstants.class.getResourceAsStream("/mcpe/steve_skin_64x64.raw")) {
            if (is == null) {
                System.err.println("[MCPE] WARNING: steve_skin_64x64.raw not found in resources, using blank skin");
                return new byte[16384];
            }
            byte[] data = new byte[16384];
            int offset = 0;
            while (offset < data.length) {
                int read = is.read(data, offset, data.length - offset);
                if (read == -1) break;
                offset += read;
            }
            System.out.println("[MCPE] Loaded Steve skin: " + offset + " bytes");
            return data;
        } catch (java.io.IOException e) {
            System.err.println("[MCPE] WARNING: Failed to load Steve skin: " + e.getMessage());
            return new byte[16384];
        }
    }

    // --- Pong advertisement format ---
    // MCPE 0.7.x client checks for "MCCPP;Demo;" prefix (with fallback to "MCCPP;MINECON;").
    // Format: "MCCPP;Demo;server name"
    public static final String PONG_PREFIX = "MCCPP;Demo;";

    // --- Protocol version helpers ---

    /**
     * Convert a v12-canonical packet ID to the wire ID for the given protocol version.
     * v27: map to v27 canonical via toV27Id(), then apply wire encoding (+0x81).
     * v14-v20: RotateHeadPacket inserted at 0x94, shifting IDs >= 0x94 by +1.
     * v11/v12: returns the ID unchanged.
     */
    public static int toWireId(int canonicalId, int protocolVersion) {
        if (protocolVersion >= MCPE_PROTOCOL_VERSION_81) {
            return toV81WireId(canonicalId);
        }
        if (protocolVersion >= MCPE_PROTOCOL_VERSION_34) {
            return toV34WireId(canonicalId);
        }
        if (protocolVersion >= MCPE_PROTOCOL_VERSION_27) {
            return (toV27Id(canonicalId) + 0x81) & 0xFF;
        }
        // v14-v20: RotateHeadPacket inserted at 0x94, shifting IDs >= 0x94 by +1.
        if (protocolVersion >= MCPE_PROTOCOL_VERSION_14 && (canonicalId & 0xFF) >= 0x94) {
            return (canonicalId + 1) & 0xFF;
        }
        // v9: no SetEntityLink (canonical 0xA8) — IDs >= 0xA9 shift -1.
        if (protocolVersion < MCPE_PROTOCOL_VERSION_11 && (canonicalId & 0xFF) >= 0xA9) {
            return (canonicalId - 1) & 0xFF;
        }
        return canonicalId & 0xFF;
    }

    /** Map v12-canonical packet ID to v27 wire ID. */
    private static int toV27Id(int canonicalId) {
        switch (canonicalId & 0xFF) {
            case 0x82: return V27_LOGIN & 0xFF;
            case 0x83: return V27_PLAY_STATUS & 0xFF;
            case 0x85: return V27_TEXT & 0xFF;
            case 0x86: return V27_SET_TIME & 0xFF;
            case 0x87: return V27_START_GAME & 0xFF;
            case 0x89: return V27_ADD_PLAYER & 0xFF;
            case 0x8A: return V27_REMOVE_PLAYER & 0xFF;
            case 0x8D: return V27_REMOVE_ENTITY & 0xFF;
            case 0x90: return V27_MOVE_ENTITY & 0xFF;
            case 0x93: return V27_MOVE_ENTITY & 0xFF;
            case 0x94: return V27_MOVE_PLAYER & 0xFF;
            case 0x96: return V27_REMOVE_BLOCK & 0xFF;
            case 0x97: return V27_UPDATE_BLOCK & 0xFF;
            case 0x9C: return V27_ENTITY_EVENT & 0xFF;
            case 0x9E: return V27_FULL_CHUNK_DATA & 0xFF;
            case 0x9F: return V27_PLAYER_EQUIPMENT & 0xFF;
            case 0xA0: return V27_PLAYER_ARMOR & 0xFF;
            case 0xA1: return V27_INTERACT & 0xFF;
            case 0xA2: return V27_USE_ITEM & 0xFF;
            case 0xA3: return V27_PLAYER_ACTION & 0xFF;
            case 0xA6: return V27_SET_ENTITY_DATA & 0xFF;
            case 0xA7: return V27_SET_ENTITY_MOTION & 0xFF;
            case 0xA9: return V27_SET_HEALTH & 0xFF;
            case 0xAA: return V27_SET_SPAWN_POSITION & 0xFF;
            case 0xAB: return V27_ANIMATE & 0xFF;
            case 0xAC: return V27_RESPAWN & 0xFF;
            case 0xAD: return V27_CONTAINER_SET_CONTENT & 0xFF;
            case 0xB0: return V27_CONTAINER_CLOSE & 0xFF;
            case 0xB3: return V27_CONTAINER_SET_CONTENT & 0xFF;
            case 0xB5: return V27_TEXT & 0xFF;
            case 0xB6: return V27_ADVENTURE_SETTINGS & 0xFF;
            case 0xB7: return V27_ADVENTURE_SETTINGS & 0xFF;
            case 0xB9: return V27_PLAYER_INPUT & 0xFF;
            case 0xBA: return V27_FULL_CHUNK_DATA & 0xFF;
            default: return canonicalId & 0xFF;
        }
    }

    /**
     * Convert an incoming wire packet ID to the v12-canonical ID.
     * v27: apply wire decode (+0x7F) to get v27 canonical, then map via fromV27Id().
     * v14-v20: returns -1 for v14-only packets (RotateHead at 0x94).
     * v11/v12: returns the ID unchanged.
     */
    public static int toCanonicalId(int wireId, int protocolVersion) {
        if (protocolVersion >= MCPE_PROTOCOL_VERSION_81) {
            return fromV81WireId(wireId);
        }
        if (protocolVersion >= MCPE_PROTOCOL_VERSION_34) {
            return fromV34WireId(wireId);
        }
        if (protocolVersion >= MCPE_PROTOCOL_VERSION_27) {
            int v27Canonical = (wireId + 0x7F) & 0xFF;
            return fromV27Id(v27Canonical);
        }
        // v14-v20: RotateHead inserted at 0x94
        if (protocolVersion >= MCPE_PROTOCOL_VERSION_14) {
            if (wireId == (ROTATE_HEAD_V14 & 0xFF)) return -1; // v14-only
            if (wireId > (ROTATE_HEAD_V14 & 0xFF)) return wireId - 1;
        }
        // v9: no SetEntityLink — IDs >= 0xA8 shift +1 to get v12-canonical.
        // PlaceBlock (0x95) stays as-is — handled specially in gameplay handler.
        if (protocolVersion < MCPE_PROTOCOL_VERSION_11 && wireId >= 0xA8) {
            return wireId + 1;
        }
        return wireId;
    }

    /** Map v27 wire ID to v12-canonical packet ID. */
    private static int fromV27Id(int wireId) {
        switch (wireId) {
            case 0x01: return 0x82; // LOGIN
            case 0x04: return 0x85; // TEXT → MESSAGE
            case 0x0E: return 0x94; // MOVE_PLAYER
            case 0x0F: return 0x96; // REMOVE_BLOCK
            case 0x15: return 0x9C; // ENTITY_EVENT
            case 0x17: return 0x9F; // PLAYER_EQUIPMENT
            case 0x19: return 0xA1; // INTERACT
            case 0x1A: return 0xA2; // USE_ITEM
            case 0x1B: return 0xA3; // PLAYER_ACTION
            case 0x22: return 0xAB; // ANIMATE
            case 0x26: return 0xB0; // CONTAINER_CLOSE
            case 0x2D: return 0xB9; // PLAYER_INPUT
            default: return wireId;
        }
    }

    /** Map v12-canonical packet ID to v34 wire ID (no offset — IDs are direct). */
    private static int toV34WireId(int canonicalId) {
        switch (canonicalId & 0xFF) {
            case 0x82: return V34_LOGIN & 0xFF;
            case 0x83: return V34_PLAY_STATUS & 0xFF;
            case 0x85: return V34_TEXT & 0xFF;
            case 0x86: return V34_SET_TIME & 0xFF;
            case 0x87: return V34_START_GAME & 0xFF;
            case 0x89: return V34_ADD_PLAYER & 0xFF;
            case 0x8A: return V34_REMOVE_PLAYER & 0xFF;
            case 0x8D: return V34_REMOVE_ENTITY & 0xFF;
            case 0x90: return V34_MOVE_ENTITY & 0xFF;
            case 0x93: return V34_MOVE_ENTITY & 0xFF;
            case 0x94: return V34_MOVE_PLAYER & 0xFF;
            case 0x96: return V34_REMOVE_BLOCK & 0xFF;
            case 0x97: return V34_UPDATE_BLOCK & 0xFF;
            case 0x9C: return V34_ENTITY_EVENT & 0xFF;
            case 0x9E: return V34_FULL_CHUNK_DATA & 0xFF;
            case 0x9F: return V34_MOB_EQUIPMENT & 0xFF;
            case 0xA0: return V34_MOB_ARMOR & 0xFF;
            case 0xA1: return V34_INTERACT & 0xFF;
            case 0xA2: return V34_USE_ITEM & 0xFF;
            case 0xA3: return V34_PLAYER_ACTION & 0xFF;
            case 0xA6: return V34_SET_ENTITY_DATA & 0xFF;
            case 0xA7: return V34_SET_ENTITY_MOTION & 0xFF;
            case 0xA9: return V34_SET_HEALTH & 0xFF;
            case 0xAA: return V34_SET_SPAWN_POSITION & 0xFF;
            case 0xAB: return V34_ANIMATE & 0xFF;
            case 0xAC: return V34_RESPAWN & 0xFF;
            case 0xAD: return V34_CONTAINER_SET_CONTENT & 0xFF;
            case 0xB0: return V34_CONTAINER_CLOSE & 0xFF;
            case 0xB3: return V34_CONTAINER_SET_CONTENT & 0xFF;
            case 0xB5: return V34_TEXT & 0xFF;
            case 0xB6: return V34_ADVENTURE_SETTINGS & 0xFF;
            case 0xB7: return V34_ADVENTURE_SETTINGS & 0xFF;
            case 0xB9: return V34_PLAYER_INPUT & 0xFF;
            case 0xBA: return V34_FULL_CHUNK_DATA & 0xFF;
            default: return canonicalId & 0xFF;
        }
    }

    /** Map v34 wire ID to v12-canonical packet ID. */
    private static int fromV34WireId(int wireId) {
        switch (wireId & 0xFF) {
            case 0x8F: return 0x82; // LOGIN
            case 0x93: return 0x85; // TEXT → MESSAGE
            case 0x9D: return 0x94; // MOVE_PLAYER
            case 0x9E: return 0x96; // REMOVE_BLOCK
            case 0xA4: return 0x9C; // ENTITY_EVENT
            case 0xA7: return 0x9F; // MOB_EQUIPMENT
            case 0xA9: return 0xA1; // INTERACT
            case 0xAA: return 0xA2; // USE_ITEM
            case 0xAB: return 0xA3; // PLAYER_ACTION
            case 0xB2: return 0xAB; // ANIMATE
            case 0xB6: return 0xB0; // CONTAINER_CLOSE
            case 0xBE: return 0xB9; // PLAYER_INPUT
            default: return wireId;
        }
    }

    /** Map v12-canonical packet ID to v81 wire ID. */
    private static int toV81WireId(int canonicalId) {
        switch (canonicalId & 0xFF) {
            case 0x82: return V81_LOGIN & 0xFF;
            case 0x83: return V81_PLAY_STATUS & 0xFF;
            case 0x85: return V81_TEXT & 0xFF;
            case 0x86: return V81_SET_TIME & 0xFF;
            case 0x87: return V81_START_GAME & 0xFF;
            case 0x89: return V81_ADD_PLAYER & 0xFF;
            case 0x8D: return V81_REMOVE_ENTITY & 0xFF;
            case 0x90: return V81_MOVE_ENTITY & 0xFF;
            case 0x93: return V81_MOVE_ENTITY & 0xFF;
            case 0x94: return V81_MOVE_PLAYER & 0xFF;
            case 0x96: return V81_REMOVE_BLOCK & 0xFF;
            case 0x97: return V81_UPDATE_BLOCK & 0xFF;
            case 0x9C: return V81_ENTITY_EVENT & 0xFF;
            case 0x9E: return V81_FULL_CHUNK_DATA & 0xFF;
            case 0x9F: return V81_MOB_EQUIPMENT & 0xFF;
            case 0xA0: return V81_MOB_ARMOR & 0xFF;
            case 0xA1: return V81_INTERACT & 0xFF;
            case 0xA2: return V81_USE_ITEM & 0xFF;
            case 0xA3: return V81_PLAYER_ACTION & 0xFF;
            case 0xA6: return V81_SET_ENTITY_DATA & 0xFF;
            case 0xA7: return V81_SET_ENTITY_MOTION & 0xFF;
            case 0xA9: return V81_SET_HEALTH & 0xFF;
            case 0xAA: return V81_SET_SPAWN_POSITION & 0xFF;
            case 0xAB: return V81_ANIMATE & 0xFF;
            case 0xAC: return V81_RESPAWN & 0xFF;
            case 0xAD: return V81_CONTAINER_SET_CONTENT & 0xFF;
            case 0xB0: return V81_CONTAINER_CLOSE & 0xFF;
            case 0xB3: return V81_CONTAINER_SET_CONTENT & 0xFF;
            case 0xB5: return V81_TEXT & 0xFF;
            case 0xB6: return V81_ADVENTURE_SETTINGS & 0xFF;
            case 0xB7: return V81_ADVENTURE_SETTINGS & 0xFF;
            case 0xB9: return V81_PLAYER_INPUT & 0xFF;
            case 0xBA: return V81_FULL_CHUNK_DATA & 0xFF;
            default: return canonicalId & 0xFF;
        }
    }

    /** Map v81 wire ID to v12-canonical packet ID. */
    private static int fromV81WireId(int wireId) {
        switch (wireId & 0xFF) {
            case 0x01: return 0x82; // LOGIN
            case 0x07: return 0x85; // TEXT
            case 0x10: return 0x94; // MOVE_PLAYER
            case 0x12: return 0x96; // REMOVE_BLOCK
            case 0x18: return 0x9C; // ENTITY_EVENT
            case 0x1B: return 0x9F; // MOB_EQUIPMENT
            case 0x1E: return 0xA1; // INTERACT
            case 0x1F: return 0xA2; // USE_ITEM
            case 0x20: return 0xA3; // PLAYER_ACTION
            case 0x27: return 0xAB; // ANIMATE
            case 0x2B: return 0xB0; // CONTAINER_CLOSE
            case 0x33: return 0xB9; // PLAYER_INPUT
            case 0x3D: return 0x9D; // REQUEST_CHUNK_RADIUS
            default: return wireId;
        }
    }
}

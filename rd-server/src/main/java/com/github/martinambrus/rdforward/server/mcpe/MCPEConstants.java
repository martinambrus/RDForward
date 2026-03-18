package com.github.martinambrus.rdforward.server.mcpe;

/**
 * Protocol constants for MCPE 0.7.0 (protocol version 11).
 * Includes RakNet wire constants and game-level packet IDs.
 */
public final class MCPEConstants {

    private MCPEConstants() {}

    // --- Protocol ---
    public static final int MCPE_PROTOCOL_VERSION = 11;
    public static final String MCPE_VERSION_STRING = "0.7.0";
    public static final int DEFAULT_PORT = 19133;

    // --- RakNet ---
    public static final byte[] RAKNET_MAGIC = {
        0x00, (byte) 0xFF, (byte) 0xFF, 0x00,
        (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
        (byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD,
        0x12, 0x34, 0x56, 0x78
    };
    public static final int RAKNET_MAGIC_LENGTH = 16;

    /** RakNet protocol version used by MCPE 0.7.x. */
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
    public static final byte SIGN_UPDATE         = (byte) 0xB6;
    public static final byte ADVENTURE_SETTINGS  = (byte) 0xB7;
    // 0xB8 unused
    public static final byte PLAYER_INPUT        = (byte) 0xB9;
    public static final byte ROTATE_HEAD         = (byte) 0xFF;

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

    // --- Pong advertisement format ---
    // MCPE 0.7.x client checks for "MCCPP;Demo;" prefix (with fallback to "MCCPP;MINECON;").
    // Format: "MCCPP;Demo;server name"
    public static final String PONG_PREFIX = "MCCPP;Demo;";
}

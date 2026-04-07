package com.github.martinambrus.rdforward.server.hytale;

/**
 * Constants for the Hytale protocol integration.
 *
 * Hytale uses QUIC over UDP (port 5520) with TLS 1.3.
 * Wire format is little-endian with 4-byte length + 4-byte packet ID framing.
 * Compression is Zstd (not zlib).
 */
public final class HytaleProtocolConstants {

    /** Protocol version number (sent in Connect packet). */
    public static final int PROTOCOL_VERSION = 1;

    /**
     * Protocol CRC (int32, sent in Connect packet).
     * Must match the Hytale client's expected CRC for the connection to succeed.
     * Extracted from HyCraft source: ProtocolConstants.HYTALE_PROTOCOL_CRC.
     */
    public static final int PROTOCOL_CRC = -1356075132;

    /** Protocol build number (sent in Connect packet alongside CRC). */
    public static final int PROTOCOL_BUILD_NUMBER = 0;

    /** Client version string (20-char fixed ASCII, null-padded). */
    public static final String CLIENT_VERSION = "1.0.0";

    /** Default Hytale server port (QUIC/UDP). */
    public static final int DEFAULT_PORT = 5520;

    /** Zstd compression level for outbound packets. */
    public static final int ZSTD_COMPRESSION_LEVEL = 3;

    /** Payload size threshold for Zstd compression. */
    public static final int COMPRESSION_THRESHOLD = 256;

    /** Maximum payload size per packet (bytes). */
    public static final int MAX_PAYLOAD_SIZE = 2 * 1024 * 1024;

    /** QUIC idle timeout in seconds. */
    public static final int IDLE_TIMEOUT_SECONDS = 30;

    // -- QUIC Stream Channel IDs --

    /** Default stream: login, movement, chat, entities, blocks. */
    public static final int STREAM_DEFAULT = 0;

    /** Chunks stream: chunk data (SetChunk, UnloadChunk, ServerSetBlock). */
    public static final int STREAM_CHUNKS = 1;

    /** WorldMap stream: map markers (low priority, deferred). */
    public static final int STREAM_WORLDMAP = 2;

    // -- Packet IDs (used across handlers) --

    public static final int PACKET_CONNECT = 0;
    public static final int PACKET_CLIENT_DISCONNECT = 1;
    public static final int PACKET_SERVER_DISCONNECT = 2;
    public static final int PACKET_PING = 3;
    public static final int PACKET_PONG = 4;
    public static final int PACKET_AUTH_GRANT = 11;
    public static final int PACKET_AUTH_TOKEN = 12;
    public static final int PACKET_SERVER_AUTH_TOKEN = 13;
    public static final int PACKET_CONNECT_ACCEPT = 14;
    public static final int PACKET_PASSWORD_RESPONSE = 15;

    public static final int PACKET_WORLD_SETTINGS = 20;
    public static final int PACKET_WORLD_LOAD_PROGRESS = 21;
    public static final int PACKET_WORLD_LOAD_FINISHED = 22;
    public static final int PACKET_REQUEST_ASSETS = 23;
    public static final int PACKET_VIEW_RADIUS = 32;
    public static final int PACKET_PLAYER_OPTIONS = 33;

    public static final int PACKET_UPDATE_BLOCK_TYPES = 40;

    public static final int PACKET_SET_CLIENT_ID = 100;
    public static final int PACKET_SET_GAME_MODE = 101;
    public static final int PACKET_JOIN_WORLD = 104;
    public static final int PACKET_CLIENT_READY = 105;
    public static final int PACKET_CLIENT_MOVEMENT = 108;
    public static final int PACKET_CLIENT_TELEPORT = 109;
    public static final int PACKET_MOUSE_INTERACTION = 111;
    public static final int PACKET_CLIENT_PLACE_BLOCK = 117;

    public static final int PACKET_SET_CHUNK = 131;
    public static final int PACKET_UNLOAD_CHUNK = 135;
    public static final int PACKET_SERVER_SET_BLOCK = 140;

    public static final int PACKET_ENTITY_UPDATES = 161;

    public static final int PACKET_SERVER_MESSAGE = 210;
    public static final int PACKET_CHAT_MESSAGE = 211;

    public static final int PACKET_SERVER_INFO = 223;
    public static final int PACKET_ADD_TO_PLAYER_LIST = 224;
    public static final int PACKET_UPDATE_SERVER_PLAYER_LIST = 226;
    public static final int PACKET_UPDATE_WORLD_MAP_SETTINGS = 240;
    public static final int PACKET_UPDATE_SUN_SETTINGS = 360;
    public static final int PACKET_UPDATE_POST_FX_SETTINGS = 361;
    public static final int PACKET_VOICE_CONFIG = 452;

    private HytaleProtocolConstants() {}
}

package com.github.martinambrus.rdforward.server.eaglecraft;

import io.netty.util.AttributeKey;

/**
 * Constants for the EagleCraft WebSocket protocol handshake.
 *
 * Packet type bytes, state machine states, and channel attributes
 * used during the pre-Minecraft EagleCraft handshake.
 */
public final class EagleCraftConstants {

    private EagleCraftConstants() {}

    // ---- Packet type bytes (from HandshakePacketTypes) ----

    /** C2S: client sends supported protocol versions, brand, username. */
    public static final int PROTOCOL_CLIENT_VERSION = 0x01;

    /** S2C: server sends selected protocol version, brand, version string. */
    public static final int PROTOCOL_SERVER_VERSION = 0x02;

    /** S2C: server rejects due to protocol version mismatch. */
    public static final int PROTOCOL_VERSION_MISMATCH = 0x03;

    /** C2S: client requests login with username, requested server, password. */
    public static final int PROTOCOL_CLIENT_REQUEST_LOGIN = 0x04;

    /** S2C: server allows login, sends back username + UUID. */
    public static final int PROTOCOL_SERVER_ALLOW_LOGIN = 0x05;

    /** S2C: server denies login with a message. */
    public static final int PROTOCOL_SERVER_DENY_LOGIN = 0x06;

    /** C2S: client sends profile data (skin). */
    public static final int PROTOCOL_CLIENT_PROFILE_DATA = 0x07;

    /** C2S: client signals it has finished sending profile data. */
    public static final int PROTOCOL_CLIENT_FINISH_LOGIN = 0x08;

    /** S2C: server signals handshake complete, MC protocol begins. */
    public static final int PROTOCOL_SERVER_FINISH_LOGIN = 0x09;

    /** S2C: server error. */
    public static final int PROTOCOL_SERVER_ERROR = 0xFF;

    // ---- Handshake state machine states ----

    public static final int STATE_OPENED = 0x00;
    public static final int STATE_CLIENT_VERSION = 0x01;
    public static final int STATE_CLIENT_LOGIN = 0x02;
    public static final int STATE_CLIENT_COMPLETE = 0x03;

    // ---- Skin types ----

    /** Preset skin (5 bytes total: type + 4-byte preset ID). */
    public static final int SKIN_TYPE_PRESET = 0x01;

    /** Custom skin (type + model byte + raw pixel data). */
    public static final int SKIN_TYPE_CUSTOM = 0x02;

    /** Profile data type string for skins. */
    public static final String PROFILE_DATA_TYPE_SKIN = "skin_v1";

    // ---- Supported EagleCraft handshake protocol versions ----
    // We support V2 (the most widely used by EagleCraft 1.8.8 clients).

    public static final int EAGLER_PROTOCOL_V1 = 1;
    public static final int EAGLER_PROTOCOL_V2 = 2;
    public static final int EAGLER_PROTOCOL_V3 = 3;

    /** The protocol version we select during handshake. */
    public static final int SELECTED_EAGLER_PROTOCOL = EAGLER_PROTOCOL_V2;

    /** Minecraft protocol version for MC 1.8. */
    public static final int MC_PROTOCOL_47 = 47;

    /** Minecraft protocol version for MC 1.12.2. */
    public static final int MC_PROTOCOL_340 = 340;

    // ---- Server identity ----

    public static final String SERVER_BRAND = "RDForward";
    public static final String SERVER_VERSION = "0.2.0";

    // ---- Channel attributes ----

    /** Username negotiated during the EagleCraft handshake. */
    public static final AttributeKey<String> ATTR_EAGLER_USERNAME =
            AttributeKey.valueOf("eaglerUsername");

    /** Skin data stored during handshake (raw CLIENT_PROFILE_DATA payload). */
    public static final AttributeKey<byte[]> ATTR_EAGLER_SKIN =
            AttributeKey.valueOf("eaglerSkin");

    /** Whether this connection is an EagleCraft client. */
    public static final AttributeKey<Boolean> ATTR_IS_EAGLECRAFT =
            AttributeKey.valueOf("isEaglecraft");
}

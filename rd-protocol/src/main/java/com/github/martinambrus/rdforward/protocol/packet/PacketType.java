package com.github.martinambrus.rdforward.protocol.packet;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;

/**
 * All packet types in the RDForward protocol.
 *
 * Packet IDs are stable across versions — a BLOCK_CHANGE packet
 * always has ID 0x03 regardless of protocol version. The content
 * and field count may differ between versions, but the ID stays fixed.
 * This is critical for forward compatibility.
 */
public enum PacketType {

    // === Handshake (both directions) ===
    HANDSHAKE(0x00, Direction.BOTH, ProtocolVersion.RUBYDUNG_1),
    HANDSHAKE_RESPONSE(0x01, Direction.SERVER_TO_CLIENT, ProtocolVersion.RUBYDUNG_1),

    // === Connection lifecycle ===
    DISCONNECT(0x02, Direction.BOTH, ProtocolVersion.RUBYDUNG_1),

    // === World data ===
    BLOCK_CHANGE(0x03, Direction.BOTH, ProtocolVersion.RUBYDUNG_1),
    CHUNK_DATA(0x04, Direction.SERVER_TO_CLIENT, ProtocolVersion.RUBYDUNG_1),

    // === Player ===
    PLAYER_POSITION(0x05, Direction.BOTH, ProtocolVersion.RUBYDUNG_1),
    PLAYER_JOIN(0x06, Direction.SERVER_TO_CLIENT, ProtocolVersion.RUBYDUNG_1),
    PLAYER_LEAVE(0x07, Direction.SERVER_TO_CLIENT, ProtocolVersion.RUBYDUNG_1),

    // === Chat ===
    CHAT_MESSAGE(0x08, Direction.BOTH, ProtocolVersion.RUBYDUNG_1),

    // === Alpha-era packets ===
    PLAYER_HEALTH(0x10, Direction.SERVER_TO_CLIENT, ProtocolVersion.ALPHA_2),
    INVENTORY_UPDATE(0x11, Direction.BOTH, ProtocolVersion.ALPHA_2),
    TIME_UPDATE(0x12, Direction.SERVER_TO_CLIENT, ProtocolVersion.ALPHA_2),
    ENTITY_SPAWN(0x13, Direction.SERVER_TO_CLIENT, ProtocolVersion.ALPHA_2),
    ENTITY_DESPAWN(0x14, Direction.SERVER_TO_CLIENT, ProtocolVersion.ALPHA_2),
    ENTITY_MOVE(0x15, Direction.SERVER_TO_CLIENT, ProtocolVersion.ALPHA_2),
    MINING_PROGRESS(0x16, Direction.SERVER_TO_CLIENT, ProtocolVersion.ALPHA_2);

    private final int id;
    private final Direction direction;
    private final ProtocolVersion introducedIn;

    PacketType(int id, Direction direction, ProtocolVersion introducedIn) {
        this.id = id;
        this.direction = direction;
        this.introducedIn = introducedIn;
    }

    public int getId() {
        return id;
    }

    public Direction getDirection() {
        return direction;
    }

    public ProtocolVersion getIntroducedIn() {
        return introducedIn;
    }

    /**
     * Whether this packet type exists in the given protocol version.
     */
    public boolean existsIn(ProtocolVersion version) {
        return version.getVersionNumber() >= introducedIn.getVersionNumber();
    }

    /**
     * Look up a packet type by its numeric ID.
     */
    public static PacketType fromId(int id) {
        for (PacketType pt : values()) {
            if (pt.id == id) {
                return pt;
            }
        }
        return null;
    }

    public enum Direction {
        CLIENT_TO_SERVER,
        SERVER_TO_CLIENT,
        BOTH
    }
}

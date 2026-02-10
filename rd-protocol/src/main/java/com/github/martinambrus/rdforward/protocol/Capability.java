package com.github.martinambrus.rdforward.protocol;

/**
 * Capabilities that a client or server can support.
 * During handshake, both sides advertise their capabilities,
 * and the server determines the active set (intersection).
 *
 * This enables forward-compatible protocol design:
 * older clients simply don't advertise newer capabilities,
 * and the server knows not to send those packet types.
 */
public enum Capability {

    // === Block Operations (RubyDung v1+) ===
    BLOCK_PLACE(1, ProtocolVersion.RUBYDUNG_1),
    BLOCK_BREAK(2, ProtocolVersion.RUBYDUNG_1),

    // === Player Movement (RubyDung v1+) ===
    PLAYER_POSITION(3, ProtocolVersion.RUBYDUNG_1),

    // === Chunk Data (RubyDung v1+) ===
    CHUNK_DATA(4, ProtocolVersion.RUBYDUNG_1),

    // === Chat (RubyDung v1+ - simple text) ===
    CHAT_MESSAGE(5, ProtocolVersion.RUBYDUNG_1),

    // === Alpha-era capabilities ===
    PLAYER_HEALTH(10, ProtocolVersion.ALPHA_2),
    INVENTORY(11, ProtocolVersion.ALPHA_2),
    DAY_NIGHT_CYCLE(12, ProtocolVersion.ALPHA_2),
    ENTITY_SPAWN(13, ProtocolVersion.ALPHA_2),
    BLOCK_METADATA(14, ProtocolVersion.ALPHA_2),
    MINING_PROGRESS(15, ProtocolVersion.ALPHA_2);

    private final int id;
    private final ProtocolVersion introducedIn;

    Capability(int id, ProtocolVersion introducedIn) {
        this.id = id;
        this.introducedIn = introducedIn;
    }

    public int getId() {
        return id;
    }

    public ProtocolVersion getIntroducedIn() {
        return introducedIn;
    }

    /**
     * Check if this capability is available for a given protocol version.
     */
    public boolean isAvailableIn(ProtocolVersion version) {
        return version.getVersionNumber() >= introducedIn.getVersionNumber();
    }
}

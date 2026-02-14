package com.github.martinambrus.rdforward.protocol;

/**
 * Capabilities that a client or server can support.
 *
 * Each capability is tied to the protocol version that introduced it.
 * The server determines active capabilities based on the client's
 * protocol version — capabilities are inferred, not negotiated
 * (matching how real MC servers work: the protocol version determines
 * what features are available).
 *
 * This can be extended in the future with CPE-style (Classic Protocol
 * Extension) explicit capability negotiation for custom features.
 */
public enum Capability {

    // === Block Operations (RubyDung v0+) ===
    BLOCK_PLACE(1, ProtocolVersion.RUBYDUNG),
    BLOCK_BREAK(2, ProtocolVersion.RUBYDUNG),

    // === Player Movement (RubyDung v0+) ===
    PLAYER_POSITION(3, ProtocolVersion.RUBYDUNG),

    // === Chunk Data (RubyDung v0+) ===
    CHUNK_DATA(4, ProtocolVersion.RUBYDUNG),

    // === Chat (RubyDung v0+ — simple text) ===
    CHAT_MESSAGE(5, ProtocolVersion.RUBYDUNG),

    // === Alpha-era capabilities (v10+) ===
    PLAYER_HEALTH(10, ProtocolVersion.ALPHA_1_0_15),
    INVENTORY(11, ProtocolVersion.ALPHA_1_0_15),
    DAY_NIGHT_CYCLE(12, ProtocolVersion.ALPHA_1_2_0),
    ENTITY_SPAWN(13, ProtocolVersion.ALPHA_1_0_15),
    BLOCK_METADATA(14, ProtocolVersion.ALPHA_1_0_15),
    MINING_PROGRESS(15, ProtocolVersion.ALPHA_1_0_15);

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
        return version.isAtLeast(introducedIn);
    }
}

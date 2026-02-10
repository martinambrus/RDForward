package com.github.martinambrus.rdforward.protocol;

/**
 * Defines protocol versions for each supported game version.
 * Each version corresponds to a distinct set of capabilities,
 * block IDs, and packet types.
 *
 * When a client connects, it declares its protocol version.
 * The server then knows which translator pipeline to build
 * for that connection's Netty channel.
 */
public enum ProtocolVersion {

    /**
     * RubyDung (2009) - the original prototype.
     * 2 block types (grass + cobblestone), no multiplayer,
     * no inventory, no entities, instant block break.
     */
    RUBYDUNG_1(1, "RubyDung", 2),

    /**
     * Minecraft Alpha (v1.0.0 - v1.2.6).
     * ~80 block types, survival mode, health, inventory,
     * day/night cycle, Nether dimension.
     */
    ALPHA_2(2, "Alpha", 82);

    // Future versions will be added here:
    // BETA_3(3, "Beta", ...)
    // RELEASE_1_0(4, "Release 1.0", ...)

    private final int versionNumber;
    private final String displayName;
    private final int blockCount;

    ProtocolVersion(int versionNumber, String displayName, int blockCount) {
        this.versionNumber = versionNumber;
        this.displayName = displayName;
        this.blockCount = blockCount;
    }

    public int getVersionNumber() {
        return versionNumber;
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
     */
    public static ProtocolVersion fromNumber(int number) {
        for (ProtocolVersion pv : values()) {
            if (pv.versionNumber == number) {
                return pv;
            }
        }
        return null;
    }
}

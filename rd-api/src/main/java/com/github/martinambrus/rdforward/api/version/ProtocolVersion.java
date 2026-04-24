package com.github.martinambrus.rdforward.api.version;

/**
 * Mod-facing view of a protocol version.
 *
 * <p>Implementations are provided by {@code rd-protocol} (the authoritative
 * enum) and wrapped by the mod loader's version adapter.
 */
public interface ProtocolVersion {

    /** Enum-style constant name, e.g. "BETA_1_8" or "RELEASE_1_20_5". */
    String name();

    /** Numeric protocol id as on-the-wire. May be non-monotonic across versions. */
    int getVersionNumber();

    /** Chronological sort index — use this for version ordering, not getVersionNumber. */
    int getSortOrder();

    /** Human-readable version label, e.g. "Beta 1.8", "1.20.5", "Bedrock". */
    String getDisplayName();

    /** True if this is a Bedrock Edition protocol. */
    boolean isBedrock();

    /**
     * Compare versions chronologically.
     * @return true if this version is the same as or newer than {@code other}
     */
    boolean isAtLeast(ProtocolVersion other);
}

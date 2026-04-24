package com.github.martinambrus.rdforward.api.version;

/** Convenience static API — mirrors {@link VersionCapability#isSupported}. */
public final class VersionSupport {

    private VersionSupport() {}

    public static boolean isSupported(VersionCapability cap, ProtocolVersion version) {
        return cap != null && cap.isSupported(version);
    }
}

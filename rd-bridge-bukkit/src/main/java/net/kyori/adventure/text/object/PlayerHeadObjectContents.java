package net.kyori.adventure.text.object;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PlayerHeadObjectContents extends net.kyori.adventure.text.object.ObjectContents {
    public static final boolean DEFAULT_HAT = true;
    java.lang.String name();
    java.util.UUID id();
    java.util.List profileProperties();
    boolean hat();
    net.kyori.adventure.key.Key texture();
    net.kyori.adventure.text.object.PlayerHeadObjectContents$Builder toBuilder();
    static net.kyori.adventure.text.object.PlayerHeadObjectContents$ProfileProperty property(java.lang.String arg0, java.lang.String arg1) {
        return null;
    }
    static net.kyori.adventure.text.object.PlayerHeadObjectContents$ProfileProperty property(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2) {
        return null;
    }
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
}

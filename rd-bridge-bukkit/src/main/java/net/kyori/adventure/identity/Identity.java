package net.kyori.adventure.identity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Identity extends net.kyori.examination.Examinable, net.kyori.adventure.identity.Identified {
    public static final net.kyori.adventure.pointer.Pointer NAME = null;
    public static final net.kyori.adventure.pointer.Pointer UUID = null;
    public static final net.kyori.adventure.pointer.Pointer DISPLAY_NAME = null;
    public static final net.kyori.adventure.pointer.Pointer LOCALE = null;
    static net.kyori.adventure.identity.Identity nil() {
        return null;
    }
    static net.kyori.adventure.identity.Identity identity(java.util.UUID arg0) {
        return null;
    }
    java.util.UUID uuid();
    default net.kyori.adventure.identity.Identity identity() {
        return null;
    }
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
}

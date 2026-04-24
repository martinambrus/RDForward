package net.kyori.adventure.chat;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SignedMessage extends net.kyori.adventure.identity.Identified, net.kyori.examination.Examinable {
    static net.kyori.adventure.chat.SignedMessage$Signature signature(byte[] arg0) {
        return null;
    }
    static net.kyori.adventure.chat.SignedMessage system(java.lang.String arg0, net.kyori.adventure.text.ComponentLike arg1) {
        return null;
    }
    java.time.Instant timestamp();
    long salt();
    net.kyori.adventure.chat.SignedMessage$Signature signature();
    net.kyori.adventure.text.Component unsignedContent();
    java.lang.String message();
    default boolean isSystem() {
        return false;
    }
    default boolean canDelete() {
        return false;
    }
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
}

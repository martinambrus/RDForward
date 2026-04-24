package net.kyori.adventure.text.serializer.legacy;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface LegacyComponentSerializer extends net.kyori.adventure.text.serializer.ComponentSerializer, net.kyori.adventure.util.Buildable {
    public static final char SECTION_CHAR = (char) 167;
    public static final char AMPERSAND_CHAR = (char) 38;
    public static final char HEX_CHAR = (char) 35;
    static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer legacySection() {
        return null;
    }
    static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer legacyAmpersand() {
        return null;
    }
    static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer legacy(char arg0) {
        return null;
    }
    static net.kyori.adventure.text.serializer.legacy.LegacyFormat parseChar(char arg0) {
        return null;
    }
    static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder builder() {
        return null;
    }
    net.kyori.adventure.text.TextComponent deserialize(java.lang.String arg0);
    java.lang.String serialize(net.kyori.adventure.text.Component arg0);
    default net.kyori.adventure.text.Component deserialize(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.deserialize(Ljava/lang/Object;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
}

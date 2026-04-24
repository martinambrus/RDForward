package net.kyori.adventure.text.serializer.plain;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PlainTextComponentSerializer extends net.kyori.adventure.text.serializer.ComponentSerializer, net.kyori.adventure.util.Buildable {
    static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer plainText() {
        return null;
    }
    static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer$Builder builder() {
        return null;
    }
    default net.kyori.adventure.text.TextComponent deserialize(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.deserialize(Ljava/lang/String;)Lnet/kyori/adventure/text/TextComponent;");
        return null;
    }
    default java.lang.String serialize(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.serialize(Lnet/kyori/adventure/text/Component;)Ljava/lang/String;");
        return null;
    }
    void serialize(java.lang.StringBuilder arg0, net.kyori.adventure.text.Component arg1);
    default net.kyori.adventure.text.Component deserialize(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.deserialize(Ljava/lang/Object;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
}

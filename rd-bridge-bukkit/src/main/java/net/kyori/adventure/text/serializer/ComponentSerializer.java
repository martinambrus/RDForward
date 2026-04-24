package net.kyori.adventure.text.serializer;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ComponentSerializer extends net.kyori.adventure.text.serializer.ComponentEncoder, net.kyori.adventure.text.serializer.ComponentDecoder {
    net.kyori.adventure.text.Component deserialize(java.lang.Object arg0);
    default net.kyori.adventure.text.Component deseializeOrNull(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.serializer.ComponentSerializer.deseializeOrNull(Ljava/lang/Object;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component deserializeOrNull(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.serializer.ComponentSerializer.deserializeOrNull(Ljava/lang/Object;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component deserializeOr(java.lang.Object arg0, net.kyori.adventure.text.Component arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.serializer.ComponentSerializer.deserializeOr(Ljava/lang/Object;Lnet/kyori/adventure/text/Component;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    java.lang.Object serialize(net.kyori.adventure.text.Component arg0);
    default java.lang.Object serializeOrNull(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.serializer.ComponentSerializer.serializeOrNull(Lnet/kyori/adventure/text/Component;)Ljava/lang/Object;");
        return null;
    }
    default java.lang.Object serializeOr(net.kyori.adventure.text.Component arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.serializer.ComponentSerializer.serializeOr(Lnet/kyori/adventure/text/Component;Ljava/lang/Object;)Ljava/lang/Object;");
        return null;
    }
}

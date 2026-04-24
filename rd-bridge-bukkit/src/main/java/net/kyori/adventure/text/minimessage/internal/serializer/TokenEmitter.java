package net.kyori.adventure.text.minimessage.internal.serializer;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TokenEmitter {
    net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter tag(java.lang.String arg0);
    net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter selfClosingTag(java.lang.String arg0);
    default net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter arguments(java.lang.String[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter.arguments([Ljava/lang/String;)Lnet/kyori/adventure/text/minimessage/internal/serializer/TokenEmitter;");
        return this;
    }
    net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter argument(java.lang.String arg0);
    net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter argument(java.lang.String arg0, net.kyori.adventure.text.minimessage.internal.serializer.QuotingOverride arg1);
    net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter argument(net.kyori.adventure.text.Component arg0);
    net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter text(java.lang.String arg0);
    net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter pop();
}

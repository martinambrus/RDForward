package net.kyori.adventure.text.minimessage;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
class MiniMessageSerializer$Collector implements net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter, net.kyori.adventure.text.minimessage.internal.serializer.ClaimConsumer {
    public MiniMessageSerializer$Collector() {}
    public net.kyori.adventure.text.minimessage.MiniMessageSerializer$Collector tag(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessageSerializer$Collector.tag(Ljava/lang/String;)Lnet/kyori/adventure/text/minimessage/MiniMessageSerializer$Collector;");
        return this;
    }
    public net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter selfClosingTag(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessageSerializer$Collector.selfClosingTag(Ljava/lang/String;)Lnet/kyori/adventure/text/minimessage/internal/serializer/TokenEmitter;");
        return null;
    }
    public net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter argument(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessageSerializer$Collector.argument(Ljava/lang/String;)Lnet/kyori/adventure/text/minimessage/internal/serializer/TokenEmitter;");
        return null;
    }
    public net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter argument(java.lang.String arg0, net.kyori.adventure.text.minimessage.internal.serializer.QuotingOverride arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessageSerializer$Collector.argument(Ljava/lang/String;Lnet/kyori/adventure/text/minimessage/internal/serializer/QuotingOverride;)Lnet/kyori/adventure/text/minimessage/internal/serializer/TokenEmitter;");
        return null;
    }
    public net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter argument(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessageSerializer$Collector.argument(Lnet/kyori/adventure/text/Component;)Lnet/kyori/adventure/text/minimessage/internal/serializer/TokenEmitter;");
        return null;
    }
    public net.kyori.adventure.text.minimessage.MiniMessageSerializer$Collector text(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessageSerializer$Collector.text(Ljava/lang/String;)Lnet/kyori/adventure/text/minimessage/MiniMessageSerializer$Collector;");
        return this;
    }
    public net.kyori.adventure.text.minimessage.MiniMessageSerializer$Collector pop() {
        return null;
    }
    public void style(java.lang.String arg0, net.kyori.adventure.text.minimessage.internal.serializer.Emitable arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessageSerializer$Collector.style(Ljava/lang/String;Lnet/kyori/adventure/text/minimessage/internal/serializer/Emitable;)V");
    }
    public boolean component(net.kyori.adventure.text.minimessage.internal.serializer.Emitable arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessageSerializer$Collector.component(Lnet/kyori/adventure/text/minimessage/internal/serializer/Emitable;)Z");
        return false;
    }
    public boolean componentClaimed() {
        return false;
    }
    public boolean styleClaimed(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessageSerializer$Collector.styleClaimed(Ljava/lang/String;)Z");
        return false;
    }
}

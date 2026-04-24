package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TextReplacementConfig$Builder extends net.kyori.adventure.builder.AbstractBuilder, net.kyori.adventure.util.Buildable$Builder {
    default net.kyori.adventure.text.TextReplacementConfig$Builder matchLiteral(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TextReplacementConfig$Builder.matchLiteral(Ljava/lang/String;)Lnet/kyori/adventure/text/TextReplacementConfig$Builder;");
        return this;
    }
    default net.kyori.adventure.text.TextReplacementConfig$Builder match(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TextReplacementConfig$Builder.match(Ljava/lang/String;)Lnet/kyori/adventure/text/TextReplacementConfig$Builder;");
        return this;
    }
    net.kyori.adventure.text.TextReplacementConfig$Builder match(java.util.regex.Pattern arg0);
    default net.kyori.adventure.text.TextReplacementConfig$Builder once() {
        return null;
    }
    default net.kyori.adventure.text.TextReplacementConfig$Builder times(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TextReplacementConfig$Builder.times(I)Lnet/kyori/adventure/text/TextReplacementConfig$Builder;");
        return this;
    }
    default net.kyori.adventure.text.TextReplacementConfig$Builder condition(net.kyori.adventure.util.IntFunction2 arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TextReplacementConfig$Builder.condition(Lnet/kyori/adventure/util/IntFunction2;)Lnet/kyori/adventure/text/TextReplacementConfig$Builder;");
        return this;
    }
    net.kyori.adventure.text.TextReplacementConfig$Builder condition(net.kyori.adventure.text.TextReplacementConfig$Condition arg0);
    default net.kyori.adventure.text.TextReplacementConfig$Builder replacement(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TextReplacementConfig$Builder.replacement(Ljava/lang/String;)Lnet/kyori/adventure/text/TextReplacementConfig$Builder;");
        return this;
    }
    default net.kyori.adventure.text.TextReplacementConfig$Builder replacement(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TextReplacementConfig$Builder.replacement(Lnet/kyori/adventure/text/ComponentLike;)Lnet/kyori/adventure/text/TextReplacementConfig$Builder;");
        return this;
    }
    default net.kyori.adventure.text.TextReplacementConfig$Builder replacement(java.util.function.Function arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TextReplacementConfig$Builder.replacement(Ljava/util/function/Function;)Lnet/kyori/adventure/text/TextReplacementConfig$Builder;");
        return this;
    }
    net.kyori.adventure.text.TextReplacementConfig$Builder replacement(java.util.function.BiFunction arg0);
    net.kyori.adventure.text.TextReplacementConfig$Builder replaceInsideHoverEvents(boolean arg0);
}

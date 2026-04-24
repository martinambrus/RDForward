package net.kyori.adventure.text.minimessage.tag.resolver;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TagResolver$Builder {
    net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$Builder tag(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.Tag arg1);
    default net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$Builder tag(java.lang.String arg0, java.util.function.BiFunction arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$Builder.tag(Ljava/lang/String;Ljava/util/function/BiFunction;)Lnet/kyori/adventure/text/minimessage/tag/resolver/TagResolver$Builder;");
        return this;
    }
    default net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$Builder tag(java.util.Set arg0, java.util.function.BiFunction arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$Builder.tag(Ljava/util/Set;Ljava/util/function/BiFunction;)Lnet/kyori/adventure/text/minimessage/tag/resolver/TagResolver$Builder;");
        return this;
    }
    net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$Builder resolver(net.kyori.adventure.text.minimessage.tag.resolver.TagResolver arg0);
    net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$Builder resolvers(net.kyori.adventure.text.minimessage.tag.resolver.TagResolver[] arg0);
    net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$Builder resolvers(java.lang.Iterable arg0);
    default net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$Builder caching(net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$WithoutArguments arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$Builder.caching(Lnet/kyori/adventure/text/minimessage/tag/resolver/TagResolver$WithoutArguments;)Lnet/kyori/adventure/text/minimessage/tag/resolver/TagResolver$Builder;");
        return this;
    }
    net.kyori.adventure.text.minimessage.tag.resolver.TagResolver build();
}

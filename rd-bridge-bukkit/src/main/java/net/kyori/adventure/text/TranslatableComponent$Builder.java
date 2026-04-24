package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TranslatableComponent$Builder extends net.kyori.adventure.text.ComponentBuilder {
    default net.kyori.adventure.text.TranslatableComponent$Builder key(net.kyori.adventure.translation.Translatable arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TranslatableComponent$Builder.key(Lnet/kyori/adventure/translation/Translatable;)Lnet/kyori/adventure/text/TranslatableComponent$Builder;");
        return this;
    }
    net.kyori.adventure.text.TranslatableComponent$Builder key(java.lang.String arg0);
    default net.kyori.adventure.text.TranslatableComponent$Builder args(net.kyori.adventure.text.ComponentBuilder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TranslatableComponent$Builder.args(Lnet/kyori/adventure/text/ComponentBuilder;)Lnet/kyori/adventure/text/TranslatableComponent$Builder;");
        return this;
    }
    default net.kyori.adventure.text.TranslatableComponent$Builder args(net.kyori.adventure.text.ComponentBuilder[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TranslatableComponent$Builder.args([Lnet/kyori/adventure/text/ComponentBuilder;)Lnet/kyori/adventure/text/TranslatableComponent$Builder;");
        return this;
    }
    default net.kyori.adventure.text.TranslatableComponent$Builder args(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TranslatableComponent$Builder.args(Lnet/kyori/adventure/text/Component;)Lnet/kyori/adventure/text/TranslatableComponent$Builder;");
        return this;
    }
    default net.kyori.adventure.text.TranslatableComponent$Builder args(net.kyori.adventure.text.ComponentLike[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TranslatableComponent$Builder.args([Lnet/kyori/adventure/text/ComponentLike;)Lnet/kyori/adventure/text/TranslatableComponent$Builder;");
        return this;
    }
    default net.kyori.adventure.text.TranslatableComponent$Builder args(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TranslatableComponent$Builder.args(Ljava/util/List;)Lnet/kyori/adventure/text/TranslatableComponent$Builder;");
        return this;
    }
    net.kyori.adventure.text.TranslatableComponent$Builder arguments(net.kyori.adventure.text.ComponentLike[] arg0);
    net.kyori.adventure.text.TranslatableComponent$Builder arguments(java.util.List arg0);
    net.kyori.adventure.text.TranslatableComponent$Builder fallback(java.lang.String arg0);
}

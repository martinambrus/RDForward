package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TranslatableComponent extends net.kyori.adventure.text.BuildableComponent, net.kyori.adventure.text.ScopedComponent {
    java.lang.String key();
    default net.kyori.adventure.text.TranslatableComponent key(net.kyori.adventure.translation.Translatable arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TranslatableComponent.key(Lnet/kyori/adventure/translation/Translatable;)Lnet/kyori/adventure/text/TranslatableComponent;");
        return this;
    }
    net.kyori.adventure.text.TranslatableComponent key(java.lang.String arg0);
    java.util.List args();
    default net.kyori.adventure.text.TranslatableComponent args(net.kyori.adventure.text.ComponentLike[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TranslatableComponent.args([Lnet/kyori/adventure/text/ComponentLike;)Lnet/kyori/adventure/text/TranslatableComponent;");
        return this;
    }
    default net.kyori.adventure.text.TranslatableComponent args(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.TranslatableComponent.args(Ljava/util/List;)Lnet/kyori/adventure/text/TranslatableComponent;");
        return this;
    }
    java.util.List arguments();
    net.kyori.adventure.text.TranslatableComponent arguments(net.kyori.adventure.text.ComponentLike[] arg0);
    net.kyori.adventure.text.TranslatableComponent arguments(java.util.List arg0);
    java.lang.String fallback();
    net.kyori.adventure.text.TranslatableComponent fallback(java.lang.String arg0);
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
}

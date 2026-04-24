package net.kyori.adventure.text.minimessage;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MiniMessage extends net.kyori.adventure.text.serializer.ComponentSerializer {
    static net.kyori.adventure.text.minimessage.MiniMessage miniMessage() {
        return null;
    }
    java.lang.String escapeTags(java.lang.String arg0);
    java.lang.String escapeTags(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver arg1);
    default java.lang.String escapeTags(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessage.escapeTags(Ljava/lang/String;[Lnet/kyori/adventure/text/minimessage/tag/resolver/TagResolver;)Ljava/lang/String;");
        return null;
    }
    java.lang.String stripTags(java.lang.String arg0);
    java.lang.String stripTags(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver arg1);
    default java.lang.String stripTags(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessage.stripTags(Ljava/lang/String;[Lnet/kyori/adventure/text/minimessage/tag/resolver/TagResolver;)Ljava/lang/String;");
        return null;
    }
    net.kyori.adventure.text.Component deserialize(java.lang.String arg0, net.kyori.adventure.pointer.Pointered arg1);
    net.kyori.adventure.text.Component deserialize(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver arg1);
    net.kyori.adventure.text.Component deserialize(java.lang.String arg0, net.kyori.adventure.pointer.Pointered arg1, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver arg2);
    default net.kyori.adventure.text.Component deserialize(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessage.deserialize(Ljava/lang/String;[Lnet/kyori/adventure/text/minimessage/tag/resolver/TagResolver;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component deserialize(java.lang.String arg0, net.kyori.adventure.pointer.Pointered arg1, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver[] arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessage.deserialize(Ljava/lang/String;Lnet/kyori/adventure/pointer/Pointered;[Lnet/kyori/adventure/text/minimessage/tag/resolver/TagResolver;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    net.kyori.adventure.text.minimessage.tree.Node$Root deserializeToTree(java.lang.String arg0);
    net.kyori.adventure.text.minimessage.tree.Node$Root deserializeToTree(java.lang.String arg0, net.kyori.adventure.pointer.Pointered arg1);
    net.kyori.adventure.text.minimessage.tree.Node$Root deserializeToTree(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver arg1);
    net.kyori.adventure.text.minimessage.tree.Node$Root deserializeToTree(java.lang.String arg0, net.kyori.adventure.pointer.Pointered arg1, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver arg2);
    default net.kyori.adventure.text.minimessage.tree.Node$Root deserializeToTree(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessage.deserializeToTree(Ljava/lang/String;[Lnet/kyori/adventure/text/minimessage/tag/resolver/TagResolver;)Lnet/kyori/adventure/text/minimessage/tree/Node$Root;");
        return null;
    }
    default net.kyori.adventure.text.minimessage.tree.Node$Root deserializeToTree(java.lang.String arg0, net.kyori.adventure.pointer.Pointered arg1, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver[] arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.MiniMessage.deserializeToTree(Ljava/lang/String;Lnet/kyori/adventure/pointer/Pointered;[Lnet/kyori/adventure/text/minimessage/tag/resolver/TagResolver;)Lnet/kyori/adventure/text/minimessage/tree/Node$Root;");
        return null;
    }
    boolean strict();
    net.kyori.adventure.text.minimessage.tag.resolver.TagResolver tags();
    static net.kyori.adventure.text.minimessage.MiniMessage$Builder builder() {
        return null;
    }
}

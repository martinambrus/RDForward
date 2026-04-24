package net.kyori.adventure.text.minimessage.internal.parser;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TokenParser$TagProvider {
    net.kyori.adventure.text.minimessage.tag.Tag resolve(java.lang.String arg0, java.util.List arg1, net.kyori.adventure.text.minimessage.internal.parser.Token arg2);
    default net.kyori.adventure.text.minimessage.tag.Tag resolve(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.internal.parser.TokenParser$TagProvider.resolve(Ljava/lang/String;)Lnet/kyori/adventure/text/minimessage/tag/Tag;");
        return null;
    }
    default net.kyori.adventure.text.minimessage.tag.Tag resolve(net.kyori.adventure.text.minimessage.internal.parser.node.TagNode arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.internal.parser.TokenParser$TagProvider.resolve(Lnet/kyori/adventure/text/minimessage/internal/parser/node/TagNode;)Lnet/kyori/adventure/text/minimessage/tag/Tag;");
        return null;
    }
    static java.lang.String sanitizePlaceholderName(java.lang.String arg0) {
        return null;
    }
}

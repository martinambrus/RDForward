package net.kyori.adventure.text.minimessage.tag.resolver;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ArgumentQueue {
    net.kyori.adventure.text.minimessage.tag.Tag$Argument pop();
    net.kyori.adventure.text.minimessage.tag.Tag$Argument popOr(java.lang.String arg0);
    net.kyori.adventure.text.minimessage.tag.Tag$Argument popOr(java.util.function.Supplier arg0);
    net.kyori.adventure.text.minimessage.tag.Tag$Argument peek();
    boolean hasNext();
    void reset();
}

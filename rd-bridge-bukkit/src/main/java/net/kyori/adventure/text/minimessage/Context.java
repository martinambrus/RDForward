package net.kyori.adventure.text.minimessage;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Context {
    net.kyori.adventure.pointer.Pointered target();
    net.kyori.adventure.pointer.Pointered targetOrThrow();
    net.kyori.adventure.pointer.Pointered targetAsType(java.lang.Class arg0);
    net.kyori.adventure.text.Component deserialize(java.lang.String arg0);
    net.kyori.adventure.text.Component deserialize(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver arg1);
    net.kyori.adventure.text.Component deserialize(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver[] arg1);
    net.kyori.adventure.text.minimessage.ParsingException newException(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue arg1);
    net.kyori.adventure.text.minimessage.ParsingException newException(java.lang.String arg0);
    net.kyori.adventure.text.minimessage.ParsingException newException(java.lang.String arg0, java.lang.Throwable arg1, net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue arg2);
    boolean emitVirtuals();
}

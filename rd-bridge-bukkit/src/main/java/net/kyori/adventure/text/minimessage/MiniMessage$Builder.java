package net.kyori.adventure.text.minimessage;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MiniMessage$Builder extends net.kyori.adventure.builder.AbstractBuilder {
    net.kyori.adventure.text.minimessage.MiniMessage$Builder tags(net.kyori.adventure.text.minimessage.tag.resolver.TagResolver arg0);
    net.kyori.adventure.text.minimessage.MiniMessage$Builder editTags(java.util.function.Consumer arg0);
    net.kyori.adventure.text.minimessage.MiniMessage$Builder strict(boolean arg0);
    net.kyori.adventure.text.minimessage.MiniMessage$Builder emitVirtuals(boolean arg0);
    net.kyori.adventure.text.minimessage.MiniMessage$Builder debug(java.util.function.Consumer arg0);
    net.kyori.adventure.text.minimessage.MiniMessage$Builder postProcessor(java.util.function.UnaryOperator arg0);
    net.kyori.adventure.text.minimessage.MiniMessage$Builder preProcessor(java.util.function.UnaryOperator arg0);
    net.kyori.adventure.text.minimessage.MiniMessage build();
}

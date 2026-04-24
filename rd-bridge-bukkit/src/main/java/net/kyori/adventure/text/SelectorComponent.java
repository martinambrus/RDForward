package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SelectorComponent extends net.kyori.adventure.text.BuildableComponent, net.kyori.adventure.text.ScopedComponent {
    java.lang.String pattern();
    net.kyori.adventure.text.SelectorComponent pattern(java.lang.String arg0);
    net.kyori.adventure.text.Component separator();
    net.kyori.adventure.text.SelectorComponent separator(net.kyori.adventure.text.ComponentLike arg0);
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
}

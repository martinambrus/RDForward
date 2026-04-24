package net.kyori.adventure.text.minimessage.tag.resolver;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TagResolver {
    static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$Builder builder() {
        return null;
    }
    static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver standard() {
        return null;
    }
    static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver empty() {
        return null;
    }
    static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$Single resolver(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.Tag arg1) {
        return null;
    }
    static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver resolver(java.lang.String arg0, java.util.function.BiFunction arg1) {
        return null;
    }
    static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver resolver(java.util.Set arg0, java.util.function.BiFunction arg1) {
        return null;
    }
    static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver resolver(net.kyori.adventure.text.minimessage.tag.resolver.TagResolver[] arg0) {
        return null;
    }
    static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver resolver(java.lang.Iterable arg0) {
        return null;
    }
    static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver caching(net.kyori.adventure.text.minimessage.tag.resolver.TagResolver$WithoutArguments arg0) {
        return null;
    }
    static java.util.stream.Collector toTagResolver() {
        return null;
    }
    net.kyori.adventure.text.minimessage.tag.Tag resolve(java.lang.String arg0, net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue arg1, net.kyori.adventure.text.minimessage.Context arg2) throws net.kyori.adventure.text.minimessage.ParsingException;
    boolean has(java.lang.String arg0);
}

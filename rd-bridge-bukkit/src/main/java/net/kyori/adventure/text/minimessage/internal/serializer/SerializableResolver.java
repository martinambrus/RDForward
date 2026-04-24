package net.kyori.adventure.text.minimessage.internal.serializer;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SerializableResolver {
    static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver claimingComponent(java.lang.String arg0, java.util.function.BiFunction arg1, java.util.function.Function arg2) {
        return null;
    }
    static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver claimingComponent(java.util.Set arg0, java.util.function.BiFunction arg1, java.util.function.Function arg2) {
        return null;
    }
    static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver claimingStyle(java.lang.String arg0, java.util.function.BiFunction arg1, net.kyori.adventure.text.minimessage.internal.serializer.StyleClaim arg2) {
        return null;
    }
    static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver claimingStyle(java.util.Set arg0, java.util.function.BiFunction arg1, net.kyori.adventure.text.minimessage.internal.serializer.StyleClaim arg2) {
        return null;
    }
    void handle(net.kyori.adventure.text.Component arg0, net.kyori.adventure.text.minimessage.internal.serializer.ClaimConsumer arg1);
}

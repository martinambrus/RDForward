package net.kyori.adventure.text.serializer.json;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface LegacyHoverEventSerializer {
    net.kyori.adventure.text.event.HoverEvent$ShowItem deserializeShowItem(net.kyori.adventure.text.Component arg0) throws java.io.IOException;
    net.kyori.adventure.text.Component serializeShowItem(net.kyori.adventure.text.event.HoverEvent$ShowItem arg0) throws java.io.IOException;
    net.kyori.adventure.text.event.HoverEvent$ShowEntity deserializeShowEntity(net.kyori.adventure.text.Component arg0, net.kyori.adventure.util.Codec$Decoder arg1) throws java.io.IOException;
    net.kyori.adventure.text.Component serializeShowEntity(net.kyori.adventure.text.event.HoverEvent$ShowEntity arg0, net.kyori.adventure.util.Codec$Encoder arg1) throws java.io.IOException;
}

package net.kyori.adventure.text.serializer.json;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface JSONComponentSerializer$Builder {
    net.kyori.adventure.text.serializer.json.JSONComponentSerializer$Builder options(net.kyori.option.OptionState arg0);
    net.kyori.adventure.text.serializer.json.JSONComponentSerializer$Builder editOptions(java.util.function.Consumer arg0);
    net.kyori.adventure.text.serializer.json.JSONComponentSerializer$Builder downsampleColors();
    net.kyori.adventure.text.serializer.json.JSONComponentSerializer$Builder legacyHoverEventSerializer(net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer arg0);
    net.kyori.adventure.text.serializer.json.JSONComponentSerializer$Builder emitLegacyHoverEvent();
    net.kyori.adventure.text.serializer.json.JSONComponentSerializer build();
}

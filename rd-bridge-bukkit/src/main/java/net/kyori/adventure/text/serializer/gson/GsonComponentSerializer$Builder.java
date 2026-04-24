package net.kyori.adventure.text.serializer.gson;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface GsonComponentSerializer$Builder extends net.kyori.adventure.builder.AbstractBuilder, net.kyori.adventure.util.Buildable$Builder, net.kyori.adventure.text.serializer.json.JSONComponentSerializer$Builder {
    net.kyori.adventure.text.serializer.gson.GsonComponentSerializer$Builder options(net.kyori.option.OptionState arg0);
    net.kyori.adventure.text.serializer.gson.GsonComponentSerializer$Builder editOptions(java.util.function.Consumer arg0);
    default net.kyori.adventure.text.serializer.gson.GsonComponentSerializer$Builder downsampleColors() {
        return null;
    }
    default net.kyori.adventure.text.serializer.gson.GsonComponentSerializer$Builder legacyHoverEventSerializer(net.kyori.adventure.text.serializer.gson.LegacyHoverEventSerializer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.serializer.gson.GsonComponentSerializer$Builder.legacyHoverEventSerializer(Lnet/kyori/adventure/text/serializer/gson/LegacyHoverEventSerializer;)Lnet/kyori/adventure/text/serializer/gson/GsonComponentSerializer$Builder;");
        return this;
    }
    net.kyori.adventure.text.serializer.gson.GsonComponentSerializer$Builder legacyHoverEventSerializer(net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer arg0);
    default net.kyori.adventure.text.serializer.gson.GsonComponentSerializer$Builder emitLegacyHoverEvent() {
        return null;
    }
    net.kyori.adventure.text.serializer.gson.GsonComponentSerializer build();
}

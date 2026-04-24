package net.kyori.adventure.text.serializer.gson;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface GsonComponentSerializer extends net.kyori.adventure.text.serializer.json.JSONComponentSerializer, net.kyori.adventure.util.Buildable {
    static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer gson() {
        return null;
    }
    static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer colorDownsamplingGson() {
        return null;
    }
    static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer$Builder builder() {
        return null;
    }
    com.google.gson.Gson serializer();
    java.util.function.UnaryOperator populator();
    net.kyori.adventure.text.Component deserializeFromTree(com.google.gson.JsonElement arg0);
    com.google.gson.JsonElement serializeToTree(net.kyori.adventure.text.Component arg0);
}

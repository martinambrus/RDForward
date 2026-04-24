package com.github.martinambrus.rdforward.bridge.fabric;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses {@code fabric.mod.json} files. Entrypoint entries may be either a
 * plain string (FQCN) or an object {@code {"value": "FQCN", "adapter": "..."}} —
 * only the {@code value} is extracted; custom language adapters are not
 * supported by the bridge.
 */
public final class FabricModJsonParser {

    private FabricModJsonParser() {}

    public static FabricModDescriptor parse(InputStream in) {
        JsonElement root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        if (!root.isJsonObject()) throw new IllegalArgumentException("fabric.mod.json root must be an object");
        JsonObject obj = root.getAsJsonObject();

        String id = requireString(obj, "id");
        String version = requireString(obj, "version");
        String name = optString(obj, "name", id);
        String description = optString(obj, "description", "");
        String environment = optString(obj, "environment", "*");

        List<String> authors = new ArrayList<>();
        if (obj.has("authors") && obj.get("authors").isJsonArray()) {
            for (JsonElement a : obj.getAsJsonArray("authors")) {
                if (a.isJsonPrimitive()) authors.add(a.getAsString());
                else if (a.isJsonObject() && a.getAsJsonObject().has("name")) {
                    authors.add(a.getAsJsonObject().get("name").getAsString());
                }
            }
        }

        List<String> mainEntrypoints = new ArrayList<>();
        List<String> serverEntrypoints = new ArrayList<>();
        List<String> clientEntrypoints = new ArrayList<>();
        if (obj.has("entrypoints") && obj.get("entrypoints").isJsonObject()) {
            JsonObject ep = obj.getAsJsonObject("entrypoints");
            collectEntrypoints(ep, "main", mainEntrypoints);
            collectEntrypoints(ep, "server", serverEntrypoints);
            collectEntrypoints(ep, "client", clientEntrypoints);
        }

        Map<String, String> deps = new HashMap<>();
        if (obj.has("depends") && obj.get("depends").isJsonObject()) {
            for (Map.Entry<String, JsonElement> e : obj.getAsJsonObject("depends").entrySet()) {
                deps.put(e.getKey(), e.getValue().isJsonPrimitive() ? e.getValue().getAsString() : "*");
            }
        }

        return new FabricModDescriptor(id, version, name, description, authors,
                mainEntrypoints, serverEntrypoints, clientEntrypoints, deps, environment);
    }

    private static void collectEntrypoints(JsonObject entrypoints, String key, List<String> out) {
        if (!entrypoints.has(key) || !entrypoints.get(key).isJsonArray()) return;
        JsonArray arr = entrypoints.getAsJsonArray(key);
        for (JsonElement e : arr) {
            if (e.isJsonPrimitive()) {
                out.add(e.getAsString());
            } else if (e.isJsonObject() && e.getAsJsonObject().has("value")) {
                out.add(e.getAsJsonObject().get("value").getAsString());
            }
        }
    }

    private static String requireString(JsonObject obj, String key) {
        JsonElement v = obj.get(key);
        if (v == null || !v.isJsonPrimitive() || v.getAsString().isBlank()) {
            throw new IllegalArgumentException("fabric.mod.json missing required field: " + key);
        }
        return v.getAsString();
    }

    private static String optString(JsonObject obj, String key, String fallback) {
        JsonElement v = obj.get(key);
        if (v == null || !v.isJsonPrimitive()) return fallback;
        return v.getAsString();
    }
}

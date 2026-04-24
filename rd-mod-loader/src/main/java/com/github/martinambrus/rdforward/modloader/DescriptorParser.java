package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses mod descriptors into {@link ModDescriptor}. Supports three formats
 * per plan §1: {@code rdmod.json}, {@code rdmod.yaml}/{@code rdmod.yml},
 * and {@code rdmod.toml}. All three formats converge on a shared
 * {@code Map<String, Object>} intermediate representation and build the
 * descriptor through a single code path, so validation + error messages are
 * identical regardless of input format.
 */
public final class DescriptorParser {

    private DescriptorParser() {}

    // -------- JSON --------

    /** Parse an {@code rdmod.json} from the given input stream. */
    public static ModDescriptor parseJson(InputStream input) throws ModDescriptorException {
        return parseJson(readFully(input, "rdmod.json"));
    }

    /** Parse an {@code rdmod.json} from a string. */
    public static ModDescriptor parseJson(String text) throws ModDescriptorException {
        JsonElement root;
        try {
            root = JsonParser.parseString(text);
        } catch (JsonSyntaxException e) {
            throw new ModDescriptorException("rdmod.json is not valid JSON: " + e.getMessage(), e);
        }
        if (!root.isJsonObject()) {
            throw new ModDescriptorException("rdmod.json must be a JSON object at the top level");
        }
        return fromMap(jsonToMap(root.getAsJsonObject()));
    }

    // -------- YAML --------

    /** Parse an {@code rdmod.yaml} (or {@code .yml}) from the given input stream. */
    public static ModDescriptor parseYaml(InputStream input) throws ModDescriptorException {
        return parseYaml(readFully(input, "rdmod.yaml"));
    }

    /** Parse an {@code rdmod.yaml} from a string. */
    @SuppressWarnings("unchecked")
    public static ModDescriptor parseYaml(String text) throws ModDescriptorException {
        Yaml yaml = new Yaml(new SafeConstructor(new org.yaml.snakeyaml.LoaderOptions()));
        Object root;
        try {
            root = yaml.load(text);
        } catch (YAMLException e) {
            throw new ModDescriptorException("rdmod.yaml is not valid YAML: " + e.getMessage(), e);
        }
        if (!(root instanceof Map)) {
            throw new ModDescriptorException("rdmod.yaml must be a mapping at the top level");
        }
        return fromMap((Map<String, Object>) root);
    }

    // -------- TOML --------

    /** Parse an {@code rdmod.toml} from the given input stream. */
    public static ModDescriptor parseToml(InputStream input) throws ModDescriptorException {
        TomlParseResult result;
        try {
            result = Toml.parse(input);
        } catch (IOException e) {
            throw new ModDescriptorException("failed to read rdmod.toml: " + e.getMessage(), e);
        }
        return parseTomlResult(result);
    }

    /** Parse an {@code rdmod.toml} from a string. */
    public static ModDescriptor parseToml(String text) throws ModDescriptorException {
        try {
            return parseToml(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
        } catch (ModDescriptorException e) {
            throw e;
        }
    }

    private static ModDescriptor parseTomlResult(TomlParseResult result) throws ModDescriptorException {
        if (result.hasErrors()) {
            StringBuilder msg = new StringBuilder("rdmod.toml is not valid TOML: ");
            boolean first = true;
            for (org.tomlj.TomlParseError err : result.errors()) {
                if (!first) msg.append("; ");
                msg.append(err.getMessage());
                first = false;
            }
            throw new ModDescriptorException(msg.toString());
        }
        return fromMap(tomlTableToMap(result));
    }

    private static Map<String, Object> tomlTableToMap(TomlTable table) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (String key : table.keySet()) {
            Object raw = table.get(key);
            out.put(key, tomlConvert(raw));
        }
        return out;
    }

    private static Object tomlConvert(Object raw) {
        if (raw instanceof TomlTable) return tomlTableToMap((TomlTable) raw);
        if (raw instanceof TomlArray arr) {
            List<Object> list = new ArrayList<>(arr.size());
            for (int i = 0; i < arr.size(); i++) list.add(tomlConvert(arr.get(i)));
            return list;
        }
        return raw;
    }

    // -------- shared builder --------

    private static ModDescriptor fromMap(Map<String, Object> obj) throws ModDescriptorException {
        String id = requireString(obj, "id");
        String name = optionalString(obj, "name", id);
        String version = requireString(obj, "version");
        String description = optionalString(obj, "description", "");
        String apiVersion = optionalString(obj, "api_version", "");
        List<String> authors = stringArray(obj, "authors");
        Map<String, String> entrypoints = stringMap(obj, "entrypoints");
        Map<String, String> dependencies = stringMap(obj, "dependencies");
        Map<String, String> softDeps = stringMap(obj, "soft_dependencies");
        List<String> permissions = stringArray(obj, "permissions");
        boolean reloadable = obj.containsKey("reloadable")
                && Boolean.parseBoolean(String.valueOf(obj.get("reloadable")));
        String minProtocol = optionalString(obj, "min_protocol", null);
        String maxProtocol = optionalString(obj, "max_protocol", null);

        try {
            return new ModDescriptor(
                    id, name, version, description, authors, apiVersion,
                    entrypoints, dependencies, softDeps, permissions,
                    reloadable, minProtocol, maxProtocol
            );
        } catch (IllegalArgumentException e) {
            throw new ModDescriptorException("invalid descriptor: " + e.getMessage(), e);
        }
    }

    // -------- field access helpers (Map<String, Object>) --------

    private static String requireString(Map<String, Object> obj, String key) throws ModDescriptorException {
        Object v = obj.get(key);
        if (v == null) {
            throw new ModDescriptorException("descriptor missing required field '" + key + "'");
        }
        return String.valueOf(v);
    }

    private static String optionalString(Map<String, Object> obj, String key, String fallback) {
        Object v = obj.get(key);
        if (v == null) return fallback;
        return String.valueOf(v);
    }

    @SuppressWarnings("unchecked")
    private static List<String> stringArray(Map<String, Object> obj, String key) {
        Object v = obj.get(key);
        if (!(v instanceof List)) return List.of();
        List<Object> raw = (List<Object>) v;
        List<String> out = new ArrayList<>(raw.size());
        for (Object el : raw) {
            if (el != null) out.add(String.valueOf(el));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> stringMap(Map<String, Object> obj, String key) {
        Object v = obj.get(key);
        if (!(v instanceof Map)) return Map.of();
        Map<String, Object> raw = (Map<String, Object>) v;
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            if (e.getValue() != null) out.put(e.getKey(), String.valueOf(e.getValue()));
        }
        return out;
    }

    // -------- json -> Map<String, Object> --------

    private static Map<String, Object> jsonToMap(JsonObject obj) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
            out.put(e.getKey(), jsonToValue(e.getValue()));
        }
        return out;
    }

    private static Object jsonToValue(JsonElement el) {
        if (el.isJsonNull()) return null;
        if (el.isJsonPrimitive()) {
            com.google.gson.JsonPrimitive p = el.getAsJsonPrimitive();
            if (p.isBoolean()) return p.getAsBoolean();
            if (p.isNumber()) return p.getAsNumber();
            return p.getAsString();
        }
        if (el.isJsonArray()) {
            JsonArray arr = el.getAsJsonArray();
            List<Object> out = new ArrayList<>(arr.size());
            for (JsonElement x : arr) out.add(jsonToValue(x));
            return out;
        }
        if (el.isJsonObject()) return jsonToMap(el.getAsJsonObject());
        return null;
    }

    private static String readFully(InputStream input, String label) throws ModDescriptorException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) sb.append(buf, 0, n);
            return sb.toString();
        } catch (IOException e) {
            throw new ModDescriptorException("failed to read " + label + ": " + e.getMessage(), e);
        }
    }

    /** Thrown when a descriptor is missing, malformed or inconsistent. */
    public static final class ModDescriptorException extends Exception {
        public ModDescriptorException(String message) { super(message); }
        public ModDescriptorException(String message, Throwable cause) { super(message, cause); }
    }
}

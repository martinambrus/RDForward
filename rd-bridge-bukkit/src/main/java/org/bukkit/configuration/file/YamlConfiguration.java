// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.configuration.file;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.MemorySection;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bukkit-shaped {@code YamlConfiguration} with real load + save round
 * trip. Parsed content lands in the inherited
 * {@link MemorySection#values} map (dot-flattened) so subsequent
 * {@code getString} / {@code getInt} reads return real plugin
 * configuration data; {@link #saveToString} re-nests the flat map and
 * dumps via SnakeYAML so plugins (SimpleLogin, LoginSecurity) can
 * persist their settings back to disk.
 *
 * <p>The {@link #loadConfiguration(File)} / {@link #loadConfiguration(Reader)}
 * static factories MUST NOT return {@code null}: real plugin code (e.g.
 * LuckPerms's {@code BukkitConfigAdapter}) stores the result in a field
 * and later dereferences it without a null-check.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class YamlConfiguration extends org.bukkit.configuration.file.FileConfiguration {

    public YamlConfiguration() {}

    @Override
    public String saveToString() {
        Map<String, Object> nested = unflatten(values);
        if (options().copyDefaults() && !defaults.isEmpty()) {
            Map<String, Object> defaultsNested = unflatten(defaults);
            mergeMissing(defaultsNested, nested);
        }
        Object serialized = deepSerialize(nested);
        DumperOptions dumper = new DumperOptions();
        dumper.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumper.setIndent(Math.max(2, options().indent()));
        dumper.setWidth(options().width() > 0 ? options().width() : 80);
        dumper.setAllowUnicode(true);
        Yaml yaml = new Yaml(dumper);
        String body = (serialized instanceof Map && ((Map) serialized).isEmpty()) ? "" : yaml.dump(serialized);
        String header = options().copyHeader() ? options().header() : null;
        if (header == null || header.isEmpty()) return body;
        StringBuilder out = new StringBuilder();
        for (String line : header.split("\\r?\\n", -1)) {
            out.append('#');
            if (!line.isEmpty()) out.append(' ').append(line);
            out.append('\n');
        }
        out.append('\n').append(body);
        return out.toString();
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        if (contents == null || contents.isEmpty()) return;
        Object loaded;
        try {
            // SnakeYAML Constructor that resolves classes through plugin classloaders.
            // For types with !! tags that have a (Map) constructor (Bukkit's
            // ConfigurationSerializable pattern), construct via that constructor
            // instead of requiring a no-arg bean constructor.
            org.yaml.snakeyaml.constructor.Constructor ctor =
                    new org.yaml.snakeyaml.constructor.Constructor(new org.yaml.snakeyaml.LoaderOptions()) {
                        @Override
                        protected Class<?> getClassForName(String name) throws ClassNotFoundException {
                            try {
                                return Class.forName(name);
                            } catch (ClassNotFoundException e) {
                                for (java.net.URLClassLoader cl : pluginClassLoaders()) {
                                    try { return cl.loadClass(name); } catch (ClassNotFoundException ignored) {}
                                }
                                throw new ClassNotFoundException(name);
                            }
                        }

                        private final org.yaml.snakeyaml.constructor.Construct mapCtorConstruct =
                                new org.yaml.snakeyaml.constructor.Construct() {
                            @Override
                            public Object construct(org.yaml.snakeyaml.nodes.Node node) {
                                org.yaml.snakeyaml.nodes.MappingNode mnode =
                                        (org.yaml.snakeyaml.nodes.MappingNode) node;
                                java.util.Map<Object, Object> rawMap = constructMapping(mnode);
                                @SuppressWarnings("unchecked")
                                java.util.Map<String, Object> map = (java.util.Map<String, Object>) (java.util.Map<?, ?>) rawMap;
                                String tag = node.getTag().getValue();
                                String className = tag.substring(
                                        org.yaml.snakeyaml.nodes.Tag.PREFIX.length());
                                try {
                                    Class<?> cls = getClassForName(className);
                                    java.lang.reflect.Constructor<?> ctor =
                                            cls.getConstructor(java.util.Map.class);
                                    return ctor.newInstance(map);
                                } catch (Exception e) {
                                    return map;
                                }
                            }
                            @Override
                            public void construct2ndStep(org.yaml.snakeyaml.nodes.Node node, Object object) {}
                        };

                        @Override
                        protected org.yaml.snakeyaml.constructor.Construct getConstructor(
                                org.yaml.snakeyaml.nodes.Node node) {
                            org.yaml.snakeyaml.nodes.Tag tag = node.getTag();
                            if (tag.getValue().startsWith(org.yaml.snakeyaml.nodes.Tag.PREFIX)
                                    && node.getNodeId() == org.yaml.snakeyaml.nodes.NodeId.mapping) {
                                String className = tag.getValue().substring(
                                        org.yaml.snakeyaml.nodes.Tag.PREFIX.length());
                                try {
                                    Class<?> cls = getClassForName(className);
                                    if (hasMapConstructor(cls)) {
                                        return mapCtorConstruct;
                                    }
                                } catch (ClassNotFoundException ignored) {}
                            }
                            return super.getConstructor(node);
                        }

                        private boolean hasMapConstructor(Class<?> cls) {
                            try { cls.getConstructor(java.util.Map.class); return true; }
                            catch (NoSuchMethodException e) { return false; }
                        }
                    };
            loaded = new Yaml(ctor).load(contents);
        } catch (Exception e) {
            throw new InvalidConfigurationException("YAML parse failed: " + e.getMessage());
        }
        if (loaded instanceof Map) {
            values.clear();
            MemorySection.flattenInto((Map<String, Object>) loaded, "", values);
        }
    }

    /** Collect all registered plugin classloaders so SnakeYAML can resolve
     *  {@code !!} class tags from plugin YAML data files. */
    private static java.util.List<java.net.URLClassLoader> pluginClassLoaders() {
        return com.github.martinambrus.rdforward.bridge.bukkit.compat.LegacyPluginClassLoader.allLoaders();
    }

    @Override
    public YamlConfigurationOptions options() {
        return (YamlConfigurationOptions) super.options();
    }

    public static YamlConfiguration loadConfiguration(File file) {
        YamlConfiguration cfg = new YamlConfiguration();
        try { cfg.load(file); } catch (IOException | InvalidConfigurationException ignored) {}
        return cfg;
    }

    public static YamlConfiguration loadConfiguration(Reader reader) {
        YamlConfiguration cfg = new YamlConfiguration();
        try { cfg.load(reader); } catch (IOException | InvalidConfigurationException ignored) {}
        return cfg;
    }

    /** Pre-Bukkit-1.7 form. Modern Bukkit dropped the InputStream variant
     *  (replaced by {@link #loadConfiguration(Reader)} so plugins control
     *  the charset), but Jail 3.x's {@code JailIO.loadLanguage} and other
     *  legacy plugins still call this with the raw stream from
     *  {@code Plugin.getResource(...)}. Wrap as UTF-8 and delegate. */
    public static YamlConfiguration loadConfiguration(java.io.InputStream stream) {
        if (stream == null) return new YamlConfiguration();
        return loadConfiguration(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
    }

    /** Recursively convert {@link ConfigurationSerializable} objects to
     *  their serialized Map form (with a {@code "=="} key) so SnakeYAML
     *  can dump them as proper YAML instead of emitting empty {@code {}}. */
    @SuppressWarnings("unchecked")
    private static Object deepSerialize(Object o) {
        if (o instanceof ConfigurationSerializable cs) {
            Map<String, Object> map = new LinkedHashMap<>();
            Map<String, Object> serialized = cs.serialize();
            if (serialized != null) map.putAll(serialized);
            String alias = ConfigurationSerialization.getAlias(cs.getClass());
            map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, alias);
            return deepSerialize(map);
        }
        if (o instanceof Map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
                out.put(String.valueOf(e.getKey()), deepSerialize(e.getValue()));
            }
            return out;
        }
        if (o instanceof List) {
            java.util.List<Object> out = new java.util.ArrayList<>();
            for (Object item : (List<?>) o) {
                out.add(deepSerialize(item));
            }
            return out;
        }
        return o;
    }

    /** Reverse of {@link MemorySection#flattenInto}: walks dot-separated
     *  paths and rebuilds nested maps so SnakeYAML emits proper indented
     *  sections. Keys without dots stay at the top level. List/scalar
     *  values are inserted unchanged. */
    private static Map<String, Object> unflatten(Map<String, Object> flat) {
        Map<String, Object> root = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : flat.entrySet()) {
            String key = e.getKey();
            String[] parts = key.split("\\.");
            Map<String, Object> cursor = root;
            for (int i = 0; i < parts.length - 1; i++) {
                Object next = cursor.get(parts[i]);
                if (!(next instanceof Map)) {
                    Map<String, Object> child = new LinkedHashMap<>();
                    cursor.put(parts[i], child);
                    cursor = child;
                } else {
                    cursor = (Map<String, Object>) next;
                }
            }
            cursor.put(parts[parts.length - 1], e.getValue());
        }
        return root;
    }

    /** Recursively copy {@code defaults} entries into {@code target}
     *  where the target lacks them, mirroring real Bukkit's
     *  {@code copyDefaults} semantics. Existing values win. */
    private static void mergeMissing(Map<String, Object> defaults, Map<String, Object> target) {
        for (Map.Entry<String, Object> e : defaults.entrySet()) {
            Object existing = target.get(e.getKey());
            if (existing == null) {
                target.put(e.getKey(), e.getValue());
            } else if (existing instanceof Map && e.getValue() instanceof Map) {
                mergeMissing((Map<String, Object>) e.getValue(), (Map<String, Object>) existing);
            }
        }
    }
}

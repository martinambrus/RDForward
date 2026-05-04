package org.bukkit.configuration.serialization;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps aliases to {@link ConfigurationSerializable} classes so that
 * deserialization can resolve a YAML {@code "=="} key back to the correct
 * class. Bukkit plugins (notably HomeSpawnPlus) call
 * {@link #registerClass} during {@code onLoad}/{@code onEnable} to make
 * their serializable types discoverable by
 * {@link #deserializeObject(Map)}.
 */
public class ConfigurationSerialization {

    public static final String SERIALIZED_TYPE_KEY = "==";

    private static final ConcurrentHashMap<String, Class<? extends ConfigurationSerializable>> ALIASES =
            new ConcurrentHashMap<>();

    protected ConfigurationSerialization(java.lang.Class arg0) {}
    public ConfigurationSerialization() {}

    protected java.lang.reflect.Method getMethod(java.lang.String name, boolean arg1) {
        return null;
    }

    protected java.lang.reflect.Constructor getConstructor() {
        return null;
    }

    protected ConfigurationSerializable deserializeViaMethod(java.lang.reflect.Method m, java.util.Map map) {
        return null;
    }

    protected ConfigurationSerializable deserializeViaCtor(java.lang.reflect.Constructor c, java.util.Map map) {
        return null;
    }

    public ConfigurationSerializable deserialize(java.util.Map map) {
        return null;
    }

    /** Register a class using its {@link SerializableAs} annotation alias,
     *  falling back to the simple class name. */
    public static void registerClass(Class<? extends ConfigurationSerializable> clazz) {
        String alias = getAlias(clazz);
        if (alias != null) {
            ALIASES.putIfAbsent(alias, clazz);
        }
    }

    /** Register a class under an explicit alias. */
    public static void registerClass(Class<? extends ConfigurationSerializable> clazz, String alias) {
        if (alias != null && clazz != null) {
            ALIASES.putIfAbsent(alias, clazz);
        }
    }

    public static void unregisterClass(String alias) {
        if (alias != null) ALIASES.remove(alias);
    }

    public static void unregisterClass(Class<? extends ConfigurationSerializable> clazz) {
        ALIASES.values().removeIf(c -> c.equals(clazz));
    }

    /** Look up a registered class by alias. */
    public static Class<? extends ConfigurationSerializable> getClassByAlias(String alias) {
        return alias == null ? null : ALIASES.get(alias);
    }

    /** Derive an alias from the class — checks for a {@link SerializableAs}
     *  annotation, then falls back to the simple class name. */
    public static String getAlias(Class<? extends ConfigurationSerializable> clazz) {
        if (clazz == null) return null;
        SerializableAs sa = clazz.getAnnotation(SerializableAs.class);
        if (sa != null && !sa.value().isEmpty()) return sa.value();
        return clazz.getSimpleName();
    }

    /** Deserialize a map that contains a {@code "=="} key back to the
     *  registered class. Tries a static {@code deserialize(Map)} method
     *  first, then a (Map) constructor. */
    @SuppressWarnings("unchecked")
    public static ConfigurationSerializable deserializeObject(java.util.Map<String, ?> map) {
        if (map == null) return null;
        Object typeKey = map.get(SERIALIZED_TYPE_KEY);
        if (typeKey == null) return null;
        Class<? extends ConfigurationSerializable> clazz = getClassByAlias(String.valueOf(typeKey));
        if (clazz == null) return null;
        // Try static deserialize(Map) method
        try {
            java.lang.reflect.Method m = clazz.getMethod("deserialize", java.util.Map.class);
            if (ConfigurationSerializable.class.isAssignableFrom(m.getReturnType())) {
                return (ConfigurationSerializable) m.invoke(null, map);
            }
        } catch (ReflectiveOperationException ignored) {}
        // Try (Map) constructor
        try {
            java.lang.reflect.Constructor<? extends ConfigurationSerializable> ctor =
                    clazz.getConstructor(java.util.Map.class);
            return ctor.newInstance(map);
        } catch (ReflectiveOperationException ignored) {}
        return null;
    }

    public static ConfigurationSerializable deserializeObject(java.util.Map map, java.lang.Class clazz) {
        return deserializeObject(map);
    }
}

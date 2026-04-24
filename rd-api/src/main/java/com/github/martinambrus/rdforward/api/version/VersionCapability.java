package com.github.martinambrus.rdforward.api.version;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Extensible capability registry — each capability is a named feature gated by
 * a protocol version predicate. Mods may register custom capabilities.
 *
 * <p>Not an enum: capabilities can be added at runtime (plan section 6.3).
 *
 * <p>Built-in capabilities use {@code __server__:<name>}. Mod-defined capabilities
 * should use {@code <modId>:<name>}.
 */
public final class VersionCapability {

    private static final Map<String, VersionCapability> REGISTRY = new ConcurrentHashMap<>();

    private final String key;
    private final Predicate<ProtocolVersion> supports;

    private VersionCapability(String key, Predicate<ProtocolVersion> supports) {
        this.key = key;
        this.supports = supports;
    }

    public String getKey() { return key; }

    public boolean isSupported(ProtocolVersion version) {
        return version != null && supports.test(version);
    }

    /** Register a new capability. Throws on duplicate key. */
    public static VersionCapability register(String key, Predicate<ProtocolVersion> supports) {
        if (key == null || key.isEmpty()) throw new IllegalArgumentException("key");
        if (supports == null) throw new IllegalArgumentException("supports");
        VersionCapability cap = new VersionCapability(key, supports);
        VersionCapability prior = REGISTRY.putIfAbsent(key, cap);
        if (prior != null) {
            throw new IllegalStateException("Capability already registered: " + key);
        }
        return cap;
    }

    /** Look up a capability by key, or null if unregistered. */
    public static VersionCapability get(String key) {
        return REGISTRY.get(key);
    }

    /** Unmodifiable snapshot of all registered capabilities. */
    public static Map<String, VersionCapability> all() {
        return Collections.unmodifiableMap(REGISTRY);
    }

    // --- Built-in capability keys (registered on first class-load) ----------

    public static final VersionCapability BLOCK_PLACEMENT =
            register("__server__:block_placement", v -> true);
    public static final VersionCapability CHAT =
            register("__server__:chat", v -> true);
    public static final VersionCapability WEATHER =
            register("__server__:weather", v -> v.getSortOrder() >= 11);  // Beta 1.5+
    public static final VersionCapability TIME_OF_DAY =
            register("__server__:time_of_day", v -> v.getSortOrder() >= 3); // Alpha 1.2.0+
    public static final VersionCapability TAB_LIST =
            register("__server__:tab_list", v -> v.getSortOrder() >= 17); // Beta 1.8+
    public static final VersionCapability CUSTOM_CHANNELS =
            register("__server__:custom_channels", v -> v.getSortOrder() >= 39); // 1.3.1+
    public static final VersionCapability ACTION_BAR =
            register("__server__:action_bar", v -> v.getSortOrder() >= 47); // 1.8+
    public static final VersionCapability TITLE_SCREEN =
            register("__server__:title_screen", v -> v.getSortOrder() >= 47); // 1.8+
    public static final VersionCapability BOSS_BAR =
            register("__server__:boss_bar", v -> v.getSortOrder() >= 107); // 1.9+
}

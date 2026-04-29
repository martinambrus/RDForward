// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit;

/**
 * Functional {@link net.kyori.adventure.key.Key}-shaped namespaced key.
 * The auto-generated stub returned null from every getter and dropped
 * ctor args on the floor; CoreProtect persists {@code Material.getKey()}
 * into its audit DB and the null-namespace round-trip corrupted records.
 * The real Bukkit shape is plain (namespace, key) immutable strings —
 * mirror that.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class NamespacedKey implements net.kyori.adventure.key.Key, com.destroystokyo.paper.Namespaced {
    public static final java.lang.String MINECRAFT = "minecraft";
    public static final java.lang.String BUKKIT = "bukkit";

    private final String namespace;
    private final String key;

    public NamespacedKey(java.lang.String namespace, java.lang.String key) {
        this.namespace = namespace == null ? MINECRAFT : namespace;
        this.key = key == null ? "" : key;
    }
    public NamespacedKey(org.bukkit.plugin.Plugin plugin, java.lang.String key) {
        this(plugin == null || plugin.getName() == null ? MINECRAFT : plugin.getName().toLowerCase(java.util.Locale.ROOT), key);
    }
    public NamespacedKey() {
        this(MINECRAFT, "");
    }

    public java.lang.String getNamespace() { return namespace; }
    public java.lang.String getKey() { return key; }

    @Override
    public int hashCode() { return namespace.hashCode() * 31 + key.hashCode(); }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (!(o instanceof NamespacedKey)) return false;
        NamespacedKey n = (NamespacedKey) o;
        return namespace.equals(n.namespace) && key.equals(n.key);
    }

    @Override
    public java.lang.String toString() { return namespace + ":" + key; }

    public static org.bukkit.NamespacedKey randomKey() {
        return new NamespacedKey(MINECRAFT, java.util.UUID.randomUUID().toString().replace('-', '_'));
    }
    public static org.bukkit.NamespacedKey minecraft(java.lang.String key) {
        return new NamespacedKey(MINECRAFT, key);
    }
    public static org.bukkit.NamespacedKey fromString(java.lang.String s, org.bukkit.plugin.Plugin defaultNs) {
        if (s == null || s.isEmpty()) return null;
        int colon = s.indexOf(':');
        if (colon < 0) {
            String ns = defaultNs == null || defaultNs.getName() == null
                    ? MINECRAFT : defaultNs.getName().toLowerCase(java.util.Locale.ROOT);
            return new NamespacedKey(ns, s);
        }
        return new NamespacedKey(s.substring(0, colon), s.substring(colon + 1));
    }
    public static org.bukkit.NamespacedKey fromString(java.lang.String s) {
        return fromString(s, null);
    }

    @Override public java.lang.String namespace() { return namespace; }
    @Override public java.lang.String value() { return key; }
    @Override public java.lang.String asString() { return toString(); }
}

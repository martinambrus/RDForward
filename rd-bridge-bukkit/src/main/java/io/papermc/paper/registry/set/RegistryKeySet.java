package io.papermc.paper.registry.set;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface RegistryKeySet extends java.lang.Iterable, io.papermc.paper.registry.set.RegistrySet {
    default int size() {
        return 0;
    }
    java.util.Collection values();
    java.util.Collection resolve(org.bukkit.Registry arg0);
    boolean contains(io.papermc.paper.registry.TypedKey arg0);
    default java.util.Iterator iterator() {
        return null;
    }
}

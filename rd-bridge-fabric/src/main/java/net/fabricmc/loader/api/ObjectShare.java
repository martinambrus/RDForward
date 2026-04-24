package net.fabricmc.loader.api;

/** Auto-generated stub from fabric-loader-0.18.4.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ObjectShare {
    java.lang.Object get(java.lang.String arg0);
    void whenAvailable(java.lang.String arg0, java.util.function.BiConsumer arg1);
    java.lang.Object put(java.lang.String arg0, java.lang.Object arg1);
    java.lang.Object putIfAbsent(java.lang.String arg0, java.lang.Object arg1);
    java.lang.Object remove(java.lang.String arg0);
}

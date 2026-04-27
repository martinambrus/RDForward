package com.github.martinambrus.rdforward.buildsrc.legacyapi;

/**
 * One legacy-API bridge to add to a compiled interface .class file.
 *
 * <p>The JVM allows multiple methods on the same interface with the same
 * name and parameter signature but different return-type descriptors;
 * the Java source language does not. Real Bukkit broke source/binary
 * compatibility several times by changing return types (e.g.
 * {@code Server.getOnlinePlayers()} from {@code Player[]} to
 * {@code Collection&lt;? extends Player&gt;}). Plugins compiled against
 * an old API embed the legacy descriptor in their constant pool and
 * crash with {@link NoSuchMethodError} when only the modern descriptor
 * is present.
 *
 * <p>This record describes one such bridge so the build can patch the
 * compiled interface to carry both descriptors.
 *
 * @param methodName        e.g. {@code "getOnlinePlayers"}
 * @param modernDescriptor  the descriptor written by the Java source —
 *                          e.g. {@code "()Ljava/util/Collection;"}
 * @param legacyDescriptor  the descriptor old plugins look up —
 *                          e.g. {@code "()[Lorg/bukkit/entity/Player;"}
 * @param kind              how to convert the modern return value into
 *                          the legacy return shape inside the bridge body
 * @param elementInternalName for {@link Kind#COLLECTION_TO_ARRAY} only —
 *                            internal name of the array element type
 *                            ({@code "org/bukkit/entity/Player"} for
 *                            {@code Player[]}). Ignored for other kinds.
 */
public record BridgeSpec(
        String methodName,
        String modernDescriptor,
        String legacyDescriptor,
        Kind kind,
        String elementInternalName) {

    public enum Kind {
        /** Modern returns {@code Collection<E>}, legacy returns {@code E[]}. */
        COLLECTION_TO_ARRAY
    }

    public static BridgeSpec collectionToArray(String methodName,
                                               String modernDescriptor,
                                               String legacyDescriptor,
                                               String elementInternalName) {
        return new BridgeSpec(methodName, modernDescriptor, legacyDescriptor,
                Kind.COLLECTION_TO_ARRAY, elementInternalName);
    }
}

package com.github.martinambrus.rdforward.modloader;

import org.objectweb.asm.commons.Remapper;

/**
 * ASM {@link Remapper} that rewrites references to Mojang's transient
 * 1.7.2-1.7.5 package shading {@code net.minecraft.util.com.google.*} back
 * to the canonical {@code com.google.*} packages bundled by the host
 * server. Plugins compiled against that era still carry the legacy
 * references in their constant pools, but Guava and Gson live at their
 * regular FQCN on the runtime classpath, so the bytecode is patched at
 * class-load time inside {@link ModClassLoader#findClass(String)}.
 *
 * <p>Only the prefix is rewritten; sub-package structure under each root
 * is preserved verbatim. The mapping is one-way (legacy to canonical) —
 * no real-world bytecode references the canonical FQCN expecting the
 * legacy class, so no reverse direction is necessary.
 *
 * <p>Internal name format follows ASM convention: slashes between package
 * segments, no leading {@code L} or trailing {@code ;}. The remapper is
 * stateless and thread-safe.
 */
public final class LegacyGoogleRemapper extends Remapper {

    private static final String LEGACY_COMMON_PREFIX = "net/minecraft/util/com/google/common/";
    private static final String LEGACY_GSON_PREFIX = "net/minecraft/util/com/google/gson/";
    private static final String CANONICAL_COMMON_PREFIX = "com/google/common/";
    private static final String CANONICAL_GSON_PREFIX = "com/google/gson/";

    /** Package-binary form (dots, not slashes) used for fast string scans
     *  in {@link ModClassLoader} before invoking the full ASM rewrite. */
    public static final String LEGACY_BINARY_PREFIX = "net.minecraft.util.com.google.";

    /** Internal-name form scanned against raw class bytes to decide
     *  whether the slow path is needed. The bytes appear unmodified inside
     *  the constant pool's UTF-8 entries. */
    public static final byte[] LEGACY_BYTES = "net/minecraft/util/com/google".getBytes();

    @Override
    public String map(String internalName) {
        if (internalName == null) return null;
        if (internalName.startsWith(LEGACY_COMMON_PREFIX)) {
            return CANONICAL_COMMON_PREFIX + internalName.substring(LEGACY_COMMON_PREFIX.length());
        }
        if (internalName.startsWith(LEGACY_GSON_PREFIX)) {
            return CANONICAL_GSON_PREFIX + internalName.substring(LEGACY_GSON_PREFIX.length());
        }
        return internalName;
    }

    /** Quick byte-level scan to decide whether {@code classBytes} contains
     *  any legacy reference. Returning {@code false} lets the caller skip
     *  the full ASM read/visit/write cycle. */
    public static boolean classBytesContainLegacy(byte[] classBytes) {
        if (classBytes == null || classBytes.length < LEGACY_BYTES.length) return false;
        outer:
        for (int i = 0; i <= classBytes.length - LEGACY_BYTES.length; i++) {
            for (int j = 0; j < LEGACY_BYTES.length; j++) {
                if (classBytes[i + j] != LEGACY_BYTES[j]) continue outer;
            }
            return true;
        }
        return false;
    }
}

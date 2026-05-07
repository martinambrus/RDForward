// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.compat;

/**
 * Runtime helpers used by {@link LegacyEnumArrayTransformer} when it
 * rewrites a plugin's {@code Enum[]} parameters to {@code Object[]}.
 *
 * <p>Old plugins compiled against pre-modernization Bukkit pass arrays
 * of types that were once Java enums (e.g. {@code Sound}) to utility
 * methods declared as {@code Enum[]}. In modern Bukkit those types are
 * interfaces extending {@code OldEnum}, so the JVM verifier rejects
 * {@code [LSound;} as {@code [LEnum;}. The transformer changes the
 * method signature and call sites to {@code Object[]}; this class
 * provides {@code Enum#name()} / {@code Enum#ordinal()} replacements
 * that work on the resulting {@code Object} elements regardless of
 * whether the runtime value is a real Enum or an OldEnum-implementing
 * interface instance.
 */
public final class LegacyEnumCompat {

    private LegacyEnumCompat() {}

    /** Equivalent of {@code ((Enum) o).name()} that also handles
     *  OldEnum-implementing interface instances. Falls back to
     *  {@link String#valueOf(Object)} for anything else so a stray
     *  call site does not NPE. */
    public static String name(Object o) {
        if (o instanceof org.bukkit.util.OldEnum) return ((org.bukkit.util.OldEnum) o).name();
        if (o instanceof Enum<?>) return ((Enum<?>) o).name();
        return String.valueOf(o);
    }

    /** Equivalent of {@code ((Enum) o).ordinal()} for OldEnum / Enum;
     *  returns {@code -1} for anything else so a stray call site
     *  does not NPE. */
    public static int ordinal(Object o) {
        if (o instanceof org.bukkit.util.OldEnum) return ((org.bukkit.util.OldEnum) o).ordinal();
        if (o instanceof Enum<?>) return ((Enum<?>) o).ordinal();
        return -1;
    }
}

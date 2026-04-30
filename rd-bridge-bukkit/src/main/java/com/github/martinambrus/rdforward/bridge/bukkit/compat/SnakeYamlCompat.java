package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import java.lang.reflect.Field;

/**
 * Static shim invoked by {@link LegacySnakeYamlTransformer}-rewritten
 * plugin bytecode. EssentialsX Pre-2.14's {@code BukkitConstructor
 * .ConstructBukkitMapping.constructJavaBean2ndStep} hard-codes
 * {@code Constructor.class.getDeclaredField("typeDefinitions")} to
 * reach into SnakeYAML's internals. The {@code typeDefinitions} field
 * was promoted from {@code Constructor} to {@code BaseConstructor} in
 * SnakeYAML 1.13; the bundled SnakeYAML 1.33 still carries it on
 * {@code BaseConstructor}, two levels above {@code Constructor}. The
 * direct {@link Class#getDeclaredField(String)} call therefore throws
 * {@code NoSuchFieldException} — and Essentials surfaces it as
 * "Can't construct a java object for tag ... typeDefinitions" on
 * every YAML load.
 *
 * <p>The transformer rewrites the call site to {@link
 * #findDeclaredFieldRecursive(Class, String)}, which walks the class
 * hierarchy until it finds the named field (or exhausts the chain
 * and rethrows). Targeted scope: only call sites where the preceding
 * LDC is a known SnakeYAML field name are rewritten, so unrelated
 * {@code getDeclaredField} reflection retains its strict semantics.
 */
public final class SnakeYamlCompat {

    private SnakeYamlCompat() {}

    /**
     * Walk {@code start} and its superclasses looking for a declared
     * field named {@code name}. Mirrors the behaviour Essentials's
     * old call site assumed when {@code typeDefinitions} lived on
     * {@code Constructor}.
     *
     * @throws NoSuchFieldException if no class in the chain declares
     *         the field; preserves the original's checked exception
     *         contract so plugin try/catch blocks still match.
     */
    public static Field findDeclaredFieldRecursive(Class<?> start, String name) throws NoSuchFieldException {
        NoSuchFieldException last = null;
        Class<?> c = start;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                last = e;
                c = c.getSuperclass();
            }
        }
        throw last == null ? new NoSuchFieldException(name) : last;
    }
}

package com.github.martinambrus.rdforward.codegen;

import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Produces the Java source expression the generator emits as the
 * default return value for a stub method per the Hybrid Semantics
 * Contract (see {@code PLAN-FULL-STUBS.md} §2).
 *
 * <p>Resolution order:
 *
 * <ol>
 *   <li>Primitive type -&gt; typed zero literal ({@code false}, {@code 0},
 *       {@code 0L}, {@code 0.0f}, etc.).</li>
 *   <li>Array type -&gt; {@code new <elem>[0]} for 1D arrays, otherwise
 *       {@code null}.</li>
 *   <li>Reference type listed in {@code empty-sentinels.properties}
 *       (bundled or caller-supplied) -&gt; the configured expression.</li>
 *   <li>Common collection interfaces ({@code List}, {@code Set},
 *       {@code Map}, {@code Collection}, {@code Iterable}) -&gt;
 *       {@code Collections.emptyX()}.</li>
 *   <li>{@link java.util.Optional} -&gt; {@code Optional.empty()}.</li>
 *   <li>Otherwise -&gt; {@code null}.</li>
 * </ol>
 *
 * <p>The bundled sentinels map starts empty; bridge phases populate it
 * with ecosystem-specific types (e.g. {@code ItemStack.EMPTY},
 * {@code Component.empty()}) as those stubs land.
 */
public final class DefaultValueResolver {

    private final Map<String, String> sentinels;

    public DefaultValueResolver() {
        this(loadBundledSentinels());
    }

    public DefaultValueResolver(Map<String, String> sentinels) {
        this.sentinels = Map.copyOf(sentinels);
    }

    public String defaultExpression(Type type) {
        return switch (type.getSort()) {
            case Type.VOID -> "";
            case Type.BOOLEAN -> "false";
            case Type.CHAR -> "(char) 0";
            case Type.BYTE -> "(byte) 0";
            case Type.SHORT -> "(short) 0";
            case Type.INT -> "0";
            case Type.LONG -> "0L";
            case Type.FLOAT -> "0.0f";
            case Type.DOUBLE -> "0.0";
            case Type.ARRAY -> defaultForArray(type);
            case Type.OBJECT -> defaultForObject(type);
            default -> "null";
        };
    }

    private String defaultForArray(Type type) {
        if (type.getDimensions() != 1) return "null";
        Type elem = type.getElementType();
        String elemSource = switch (elem.getSort()) {
            case Type.BOOLEAN -> "boolean";
            case Type.CHAR -> "char";
            case Type.BYTE -> "byte";
            case Type.SHORT -> "short";
            case Type.INT -> "int";
            case Type.LONG -> "long";
            case Type.FLOAT -> "float";
            case Type.DOUBLE -> "double";
            case Type.OBJECT -> elem.getInternalName().replace('/', '.');
            default -> null;
        };
        if (elemSource == null) return "null";
        return "new " + elemSource + "[0]";
    }

    private String defaultForObject(Type type) {
        String fqcn = type.getInternalName().replace('/', '.');
        String sentinel = sentinels.get(fqcn);
        if (sentinel != null) return sentinel;
        return switch (fqcn) {
            case "java.util.Optional" -> "java.util.Optional.empty()";
            case "java.util.List", "java.util.Collection", "java.lang.Iterable"
                    -> "java.util.Collections.emptyList()";
            case "java.util.Set" -> "java.util.Collections.emptySet()";
            case "java.util.Map" -> "java.util.Collections.emptyMap()";
            default -> "null";
        };
    }

    private static Map<String, String> loadBundledSentinels() {
        Properties p = new Properties();
        try (InputStream in = DefaultValueResolver.class.getResourceAsStream(
                "/empty-sentinels.properties")) {
            if (in != null) p.load(in);
        } catch (IOException e) {
            // fall through with empty map
        }
        Map<String, String> out = new HashMap<>();
        for (String k : p.stringPropertyNames()) out.put(k, p.getProperty(k));
        return Map.copyOf(out);
    }
}

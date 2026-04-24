package com.github.martinambrus.rdforward.codegen;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Drift detector for generated bridge stubs.
 *
 * <p>Compares every public method signature declared in the pinned
 * upstream API jar against the method signatures present on the bridge's
 * compiled stub classes. Reports two flavours of drift:
 *
 * <ul>
 *   <li><b>MISSING_CLASS</b> — an upstream class is in scope for the
 *       bridge but no corresponding stub class exists.</li>
 *   <li><b>MISSING_METHOD</b> — an upstream class is stubbed but a
 *       public method from upstream is absent from the stub.</li>
 * </ul>
 *
 * <p>Preserved stub files (those carrying
 * {@link PreserveMarker#MARKER}) are exempt from
 * {@code MISSING_METHOD}: hand-tuned facades intentionally diverge from
 * the upstream shape. They are still checked for {@code MISSING_CLASS}
 * so that a preserved file deletion is surfaced.
 *
 * <p>Extra methods in stubs (present in stub but not in upstream) are
 * not reported — generated bridges may legitimately add convenience
 * methods beyond the upstream contract.
 *
 * <p>Invocation:
 *
 * <pre>
 *   StubReporter &lt;bridge&gt; &lt;stubClassesDir&gt; &lt;stubSourceDir&gt; &lt;jar1&gt; [jar2 ...]
 * </pre>
 *
 * <p>Exit code {@code 0} on no drift, {@code 1} on drift. A summary is
 * printed to {@code System.out} so CI logs show the drift rows.
 */
public final class StubReporter {

    /** Bridge internal-name prefixes in scope — must mirror {@link StubGenerator}. */
    private static final List<String> BUKKIT_PACKAGES = List.of(
            "org/bukkit/",
            "io/papermc/paper/",
            "com/destroystokyo/paper/",
            "net/kyori/adventure/",
            "net/kyori/examination/",
            "net/kyori/option/",
            "net/md_5/bungee/api/chat/",
            "com/mojang/brigadier/",
            "org/joml/",
            "co/aikar/",
            "org/spigotmc/");

    /** Fabric scope — must mirror {@link StubGenerator#FABRIC_PACKAGES}. */
    private static final List<String> FABRIC_PACKAGES = List.of(
            "net/fabricmc/");

    /** Forge scope — must mirror {@link StubGenerator#FORGE_PACKAGES}. */
    private static final List<String> FORGE_PACKAGES = List.of(
            "net/minecraftforge/eventbus/api/");

    /** NeoForge scope — must mirror {@link StubGenerator#NEOFORGE_PACKAGES}. */
    private static final List<String> NEOFORGE_PACKAGES = List.of(
            "net/neoforged/bus/api/");

    private static final Map<String, List<String>> BRIDGE_PACKAGES = Map.of(
            "bukkit", BUKKIT_PACKAGES,
            "paper", List.of(),
            "fabric", FABRIC_PACKAGES,
            "forge", FORGE_PACKAGES,
            "neoforge", NEOFORGE_PACKAGES);

    /** Must mirror {@link StubGenerator#REFUSED_PACKAGES}. */
    private static final List<String> REFUSED_PACKAGES = List.of(
            "net/minecraft/",
            "net/fabricmc/loader/impl/",
            "net/fabricmc/loader/language/",
            "net/fabricmc/loader/launch/",
            "net/fabricmc/loader/metadata/",
            "net/fabricmc/loader/util/",
            "net/fabricmc/accesswidener/");

    /** Must mirror {@link StubGenerator#SKIP_CLASSES}. */
    private static final Set<String> SKIP_CLASSES = Set.of(
            "co/aikar/util/LoadingIntMap",
            "org/bukkit/configuration/file/YamlConstructor$ConstructCustomObject",
            "org/bukkit/event/entity/PlayerDeathEvent",
            "net/fabricmc/loader/FabricLoader",
            "net/fabricmc/loader/ModContainer",
            "net/fabricmc/loader/DependencyException",
            "net/minecraftforge/eventbus/api/EventListenerHelper");

    /**
     * Methods that cannot be represented in a single Java source file
     * even though their bytecode exists in upstream. These are usually
     * generic covariant bridges (two methods sharing name+args, differing
     * only by erasure of an intersection-bound type parameter). Our
     * emitter picks the most-specific overload; the other is knowingly
     * dropped. Key format: {@code internalName#methodName+desc}.
     */
    private static final Set<String> KNOWN_METHOD_DRIFT = Set.of(
            // Registry.SimpleRegistry.get is declared with a T extends Keyed
            // bound in one overload and T extends Enum & Keyed in another,
            // giving two bytecode methods (get(NamespacedKey): Keyed and
            // get(NamespacedKey): Enum) with the same source signature.
            // Java source cannot declare both; we emit the Keyed form and
            // accept the Enum-returning bridge as dropped.
            "org/bukkit/Registry$SimpleRegistry#get(Lorg/bukkit/NamespacedKey;)Ljava/lang/Enum;");

    private StubReporter() {}

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("usage: StubReporter <bridge> <stubClassesDir> <stubSourceDir> <jar1> [jar2 ...]");
            System.exit(2);
        }
        String bridge = args[0];
        Path stubClassesDir = Path.of(args[1]);
        Path stubSourceDir = Path.of(args[2]);
        List<Path> jars = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            jars.add(Path.of(args[i]));
        }

        List<String> allowedPrefixes = BRIDGE_PACKAGES.getOrDefault(bridge, List.of());
        Set<String> preservedNames = collectPreservedClassNames(stubSourceDir);

        Map<String, ClassSig> upstream = collectUpstream(jars, allowedPrefixes);
        Map<String, ClassSig> emitted = collectEmitted(stubClassesDir, allowedPrefixes);

        List<String> missingClasses = new ArrayList<>();
        Map<String, List<String>> missingMethods = new TreeMap<>();

        for (Map.Entry<String, ClassSig> e : new TreeMap<>(upstream).entrySet()) {
            String name = e.getKey();
            if (SKIP_CLASSES.contains(name)) continue;
            ClassSig up = e.getValue();
            ClassSig st = emitted.get(name);
            if (st == null) {
                missingClasses.add(name);
                continue;
            }
            if (preservedNames.contains(name)) continue;
            Set<String> diff = new TreeSet<>(up.methods);
            diff.removeAll(st.methods);
            diff.removeIf(sig -> KNOWN_METHOD_DRIFT.contains(name + "#" + sig));
            if (!diff.isEmpty()) {
                missingMethods.put(name, new ArrayList<>(diff));
            }
        }

        int drift = missingClasses.size() + missingMethods.size();
        System.out.println("[stubReport] bridge=" + bridge
                + " upstreamClasses=" + upstream.size()
                + " stubbedClasses=" + emitted.size()
                + " preservedClasses=" + preservedNames.size()
                + " skipped=" + SKIP_CLASSES.size());
        if (drift == 0) {
            System.out.println("[stubReport] OK: no drift");
            System.exit(0);
        }

        if (!missingClasses.isEmpty()) {
            System.out.println("[stubReport] MISSING_CLASS (" + missingClasses.size() + "):");
            for (String cn : missingClasses) {
                System.out.println("  - " + cn.replace('/', '.'));
            }
        }
        if (!missingMethods.isEmpty()) {
            int methodCount = missingMethods.values().stream().mapToInt(List::size).sum();
            System.out.println("[stubReport] MISSING_METHOD (" + methodCount + " across "
                    + missingMethods.size() + " classes):");
            for (Map.Entry<String, List<String>> row : missingMethods.entrySet()) {
                System.out.println("  " + row.getKey().replace('/', '.') + ":");
                for (String m : row.getValue()) {
                    System.out.println("    - " + m);
                }
            }
        }
        System.exit(1);
    }

    private static Set<String> collectPreservedClassNames(Path stubSourceDir) throws IOException {
        if (!Files.isDirectory(stubSourceDir)) {
            return Set.of();
        }
        Set<String> out = new LinkedHashSet<>();
        try (Stream<Path> walk = Files.walk(stubSourceDir)) {
            List<Path> javaFiles = walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList();
            for (Path p : javaFiles) {
                if (PreserveMarker.isPreserved(p)) {
                    out.add(internalNameFrom(stubSourceDir, p));
                }
            }
        }
        return out;
    }

    private static String internalNameFrom(Path root, Path file) {
        Path rel = root.relativize(file);
        String s = rel.toString().replace(java.io.File.separatorChar, '/');
        if (s.endsWith(".java")) s = s.substring(0, s.length() - 5);
        return s;
    }

    private static Map<String, ClassSig> collectUpstream(List<Path> jars, List<String> allowedPrefixes)
            throws IOException {
        Map<String, ClassSig> out = new HashMap<>();
        for (Path jar : jars) {
            for (ClassNode node : JarScanner.scan(jar)) {
                if (!isPublicApiClass(node)) continue;
                if (!matchesPrefix(node.name, allowedPrefixes)) continue;
                if (matchesPrefix(node.name, REFUSED_PACKAGES)) continue;
                out.put(node.name, ClassSig.from(node));
            }
        }
        return out;
    }

    private static Map<String, ClassSig> collectEmitted(Path classesDir, List<String> allowedPrefixes)
            throws IOException {
        Map<String, ClassSig> out = new HashMap<>();
        if (!Files.isDirectory(classesDir)) return out;
        try (Stream<Path> walk = Files.walk(classesDir)) {
            List<Path> classFiles = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".class"))
                    .toList();
            for (Path file : classFiles) {
                try (InputStream in = Files.newInputStream(file)) {
                    ClassReader reader = new ClassReader(in);
                    ClassNode node = new ClassNode();
                    reader.accept(node, ClassReader.SKIP_CODE);
                    if (!isPublicApiClass(node)) continue;
                    if (!matchesPrefix(node.name, allowedPrefixes)) continue;
                    out.put(node.name, ClassSig.from(node));
                }
            }
        }
        return out;
    }

    private static boolean isPublicApiClass(ClassNode node) {
        return (node.access & Opcodes.ACC_PUBLIC) != 0
                && (node.access & Opcodes.ACC_SYNTHETIC) == 0;
    }

    private static boolean matchesPrefix(String internalName, List<String> prefixes) {
        if (prefixes.isEmpty()) return true;
        for (String p : prefixes) {
            if (internalName.startsWith(p)) return true;
        }
        return false;
    }

    /** Public method signatures {@code name + desc} for a class. */
    private record ClassSig(String name, Set<String> methods) {
        static ClassSig from(ClassNode node) {
            Set<String> out = new LinkedHashSet<>();
            if (node.methods != null) {
                for (MethodNode m : node.methods) {
                    if ((m.access & Opcodes.ACC_PUBLIC) == 0) continue;
                    if ((m.access & Opcodes.ACC_SYNTHETIC) != 0) continue;
                    if ("<clinit>".equals(m.name)) continue;
                    out.add(m.name + m.desc);
                }
            }
            return new ClassSig(node.name, Collections.unmodifiableSet(out));
        }
    }
}

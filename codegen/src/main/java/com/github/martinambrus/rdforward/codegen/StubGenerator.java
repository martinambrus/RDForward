package com.github.martinambrus.rdforward.codegen;

import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Entry point for RDForward's bridge stub generator.
 *
 * <p>Reads upstream API JAR(s), converts each class to a Java stub via
 * {@link StubEmitter}, and writes results under a bridge module's
 * source tree. Files carrying the {@code @rdforward:preserve} header
 * are left untouched (see {@link PreserveMarker}).
 *
 * <p>Invocation:
 *
 * <pre>
 *   StubGenerator &lt;bridge&gt; &lt;outputRoot&gt; &lt;jar1&gt; [jar2 ...]
 * </pre>
 *
 * <p>Where {@code bridge} is one of {@code bukkit | paper | fabric |
 * forge | neoforge}, {@code outputRoot} is the absolute path of the
 * bridge module's {@code src/main/java}, and the remaining args are
 * the resolved upstream jar paths. The Gradle wrapper task
 * {@code :codegen:generateStubs} fills these in from the project's
 * configuration.
 */
public final class StubGenerator {

    private static final Set<String> KNOWN_BRIDGES =
            Set.of("bukkit", "paper", "fabric", "forge", "neoforge");

    /**
     * Internal-name prefixes (no trailing slash) that each bridge emits.
     *
     * <p>Paper-API is the source jar. Its {@code org.bukkit.*} classes
     * already reference Paper-only types ({@code io.papermc.paper.*},
     * {@code net.kyori.adventure.*}, {@code com.mojang.brigadier.*},
     * {@code org.joml.*}). Splitting the two into separate modules
     * produces mutual cycles: the Bukkit stubs fail to compile without
     * Paper types on the classpath, and the hand-tuned Paper adapter
     * references {@code org.bukkit.*}. We therefore emit the entire
     * paper-api surface into {@code rd-bridge-bukkit} and keep
     * {@code rd-bridge-paper} for the hand-tuned Paper plugin loader
     * (paper-plugin.yml, Brigadier lifecycle, bootstrap).
     */
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

    /**
     * Fabric emits into {@code net/fabricmc/*}. The upstream
     * {@code fabric-loader} jar also ships {@code net/minecraft/*}
     * game-provider shims, Mixin scaffolding ({@code org/objectweb/asm/*},
     * {@code org/spongepowered/asm/*}), and zip/log utilities
     * ({@code net/fabricmc/loader/impl/lib/*}) — none of these belong in a
     * bridge surface. The prefix allowlist plus the
     * {@link #REFUSED_PACKAGES} denylist keep the emitted set to the mod-
     * facing {@code net/fabricmc/api}, {@code net/fabricmc/loader/api}
     * and {@code net/fabricmc/fabric/api} namespaces.
     */
    private static final List<String> FABRIC_PACKAGES = List.of(
            "net/fabricmc/");

    /**
     * Forge emits into {@code net/minecraftforge/eventbus/api/} — the
     * one Forge surface shipped as a stand-alone artifact
     * ({@code net.minecraftforge:eventbus}). The transitive closure of
     * that jar drags in {@code modlauncher}, {@code securemodules},
     * {@code unsafe}, and {@code asm}; their {@code net.minecraftforge.*}
     * classes ({@code eventbus.service}, {@code securemodules},
     * {@code unsafe}, {@code fml.unsafe}) reference {@code cpw.mods.*}
     * and raw ASM types that our bridge does not carry. The rest of the
     * Forge surface ({@code @Mod}, FML lifecycle events, per-event
     * subclasses under {@code net.minecraftforge.event}) remains
     * hand-tuned because upstream bundles it with the Minecraft runtime.
     */
    private static final List<String> FORGE_PACKAGES = List.of(
            "net/minecraftforge/eventbus/api/");

    /**
     * NeoForge emits into {@code net/neoforged/bus/api/} — the public
     * event-bus contract from {@code net.neoforged:bus}. The rest of
     * NeoForge's public surface ({@code @Mod}, {@code FMLCommonSetupEvent},
     * {@code ServerChatEvent}, {@code NeoForge}, {@code Dist}) is shipped
     * bundled with the Minecraft runtime and remains hand-tuned.
     */
    private static final List<String> NEOFORGE_PACKAGES = List.of(
            "net/neoforged/bus/api/");

    private static final Map<String, List<String>> BRIDGE_PACKAGES = Map.of(
            "bukkit", BUKKIT_PACKAGES,
            "paper", List.of(),
            "fabric", FABRIC_PACKAGES,
            "forge", FORGE_PACKAGES,
            "neoforge", NEOFORGE_PACKAGES);

    /**
     * Packages we refuse to emit stubs for even when they would otherwise
     * match the bridge's scope. {@code net/minecraft/} is Minecraft itself
     * — a Fabric mod that references {@code MinecraftServer} is talking
     * to the game, not the modding framework, and our bridge has no
     * runtime to back those calls. Leaving the type unresolved produces
     * a clear {@code NoClassDefFoundError} at load time rather than a
     * silent no-op backed by an empty stub.
     *
     * <p>Internal Fabric implementation packages are also refused: they
     * are not part of the public mod-facing API and would require huge,
     * churn-prone emission. Mods should not depend on them; if one does,
     * the load-time error is the correct signal.
     */
    private static final List<String> REFUSED_PACKAGES = List.of(
            "net/minecraft/",
            "net/fabricmc/loader/impl/",
            "net/fabricmc/loader/language/",
            "net/fabricmc/loader/launch/",
            "net/fabricmc/loader/metadata/",
            "net/fabricmc/loader/util/",
            "net/fabricmc/accesswidener/");

    /**
     * Internal classes whose raw-types stub form cannot be made to
     * compile cleanly because upstream generics declare multiple
     * conflicting supertypes (for example {@code co.aikar.util.LoadingIntMap}
     * ultimately inheriting both {@code Function<K,V>} and
     * {@code Map<K,V>} with incompatible parameter erasures, or covariant
     * builder/serializer chains whose return types cannot be reconciled
     * after erasure). These are rarely-referenced implementation details
     * so the pragmatic fix is to drop them from the emitted bridge;
     * plugin code that actually touches one will fail with
     * {@code NoClassDefFoundError} at link time with a clear pointer.
     *
     * <p>{@code PlayerDeathEvent} is here because its real {@code
     * Player getEntity()} override cannot be emitted: our bridge keeps a
     * hand-tuned {@code Player} class that does not extend {@code
     * LivingEntity}, so javac rejects the covariant return. Plugins that
     * use {@code PlayerDeathEvent} will surface during the Phase A.4
     * smoke test and we will revisit Player's facade shape then.
     */
    private static final Set<String> SKIP_CLASSES = Set.of(
            "co/aikar/util/LoadingIntMap",
            "org/bukkit/configuration/file/YamlConstructor$ConstructCustomObject",
            "org/bukkit/event/entity/PlayerDeathEvent",
            // Fabric pre-0.4 legacy re-exports that abstract-extend the
            // newer net.fabricmc.loader.api/* types. Modern Fabric mods
            // target net.fabricmc.loader.api.FabricLoader#getInstance()
            // instead. Emitting naive stubs here produces type errors
            // because our hand-tuned api.FabricLoader/ModContainer are
            // concrete classes rather than the upstream interfaces, and
            // ModContainer#getInfo returns the refused
            // net.fabricmc.loader.metadata.LoaderModMetadata.
            "net/fabricmc/loader/FabricLoader",
            "net/fabricmc/loader/ModContainer",
            "net/fabricmc/loader/DependencyException",
            // Forge eventbus helper exposes the internal ListenerList
            // impl; plugins don't reach for it, and scoping in the impl
            // class drags in ASM type references we can't resolve.
            "net/minecraftforge/eventbus/api/EventListenerHelper");

    private StubGenerator() {}

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            usage();
            System.exit(2);
        }
        String bridge = args[0];
        if (!KNOWN_BRIDGES.contains(bridge)) {
            System.err.println("[codegen] unknown bridge: " + bridge);
            usage();
            System.exit(2);
        }
        Path outputRoot = Path.of(args[1]);
        if (!Files.isDirectory(outputRoot)) {
            System.err.println("[codegen] outputRoot is not a directory: " + outputRoot);
            System.exit(2);
        }
        List<Path> jars = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            Path p = Path.of(args[i]);
            if (!Files.isRegularFile(p)) {
                System.err.println("[codegen] jar not found: " + p);
                System.exit(2);
            }
            jars.add(p);
        }

        String artifact = jars.isEmpty() ? bridge : jars.get(0).getFileName().toString();
        DefaultValueResolver resolver = new DefaultValueResolver();
        List<String> allowedPrefixes = BRIDGE_PACKAGES.getOrDefault(bridge, List.of());

        // First pass: scan all jars, build name -> ClassNode index. Needed
        // so the emitter can look up a parent class's constructor signature
        // when synthesizing explicit super(...) calls in child stubs.
        Map<String, ClassNode> classIndex = new HashMap<>();
        List<ClassNode> inScope = new ArrayList<>();
        int totalClasses = 0;
        int outOfScope = 0;
        for (Path jar : jars) {
            System.out.println("[codegen] scanning " + jar.getFileName());
            List<ClassNode> nodes = JarScanner.scan(jar);
            totalClasses += nodes.size();
            for (ClassNode node : nodes) {
                classIndex.put(node.name, node);
                if (!matchesAllowedPrefix(node.name, allowedPrefixes)) {
                    outOfScope++;
                    continue;
                }
                if (matchesAllowedPrefix(node.name, REFUSED_PACKAGES)) {
                    outOfScope++;
                    continue;
                }
                if (SKIP_CLASSES.contains(node.name)) continue;
                inScope.add(node);
            }
        }

        StubEmitter emitter = new StubEmitter(resolver, artifact, classIndex);
        StubWriter writer = new StubWriter(outputRoot);
        int skipped = 0;
        for (ClassNode node : inScope) {
            Optional<String> src = emitter.emit(node);
            if (src.isEmpty()) { skipped++; continue; }
            writer.write(node, src.get());
        }

        System.out.println("[codegen] bridge=" + bridge
                + " classes=" + totalClasses
                + " outOfScope=" + outOfScope
                + " skipped=" + skipped
                + " written=" + writer.stats().written
                + " preserved=" + writer.stats().preserved);
    }

    private static boolean matchesAllowedPrefix(String internalName, List<String> prefixes) {
        if (prefixes.isEmpty()) return true;
        for (String p : prefixes) {
            if (internalName.startsWith(p)) return true;
        }
        return false;
    }

    private static void usage() {
        System.err.println("usage: StubGenerator <bridge> <outputRoot> <jar1> [jar2 ...]");
        System.err.println("  bridge: bukkit | paper | fabric | forge | neoforge");
        System.err.println("  outputRoot: absolute path, e.g. rd-bridge-bukkit/src/main/java");
    }
}

package com.github.martinambrus.rdforward.codegen;

import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes emitted stub sources under a target source tree, honoring the
 * {@code @rdforward:preserve} marker on existing files. Target paths
 * are derived from the ASM {@linkplain ClassNode#name internal name}:
 * {@code org/bukkit/Material} -&gt; {@code org/bukkit/Material.java}.
 *
 * <p>Inner class files are written with the {@code $} separator
 * preserved, e.g. {@code org/bukkit/Outer$Inner.java}, to match the
 * binary layout third-party code references at link time.
 */
public final class StubWriter {

    private final Path outputRoot;
    private final Stats stats = new Stats();

    public StubWriter(Path outputRoot) {
        this.outputRoot = outputRoot;
    }

    /**
     * Write {@code source} for {@code node} under the output root.
     * Returns {@code true} if a file was written, {@code false} if the
     * existing target was preserved or the node is outside the scope.
     */
    public boolean write(ClassNode node, String source) throws IOException {
        Path target = targetPath(node.name);
        if (Files.exists(target) && PreserveMarker.isPreserved(target)) {
            stats.preserved++;
            return false;
        }
        Files.createDirectories(target.getParent());
        Files.writeString(target, source, StandardCharsets.UTF_8);
        stats.written++;
        return true;
    }

    public Path targetPath(String internalName) {
        String pkg = StubEmitter.packageOf(internalName);
        String simple = StubEmitter.simpleNameOf(internalName);
        Path pkgDir = pkg.isEmpty() ? outputRoot : outputRoot.resolve(pkg.replace('.', '/'));
        return pkgDir.resolve(simple + ".java");
    }

    public Stats stats() { return stats; }

    public static final class Stats {
        public int written;
        public int preserved;
    }
}

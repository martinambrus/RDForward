package com.github.martinambrus.rdforward.codegen;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Reads all non-module, non-META-INF {@code .class} entries from a JAR
 * and returns them as ASM {@link ClassNode} instances.
 *
 * <p>The reader uses {@link ClassReader#SKIP_CODE} because the stub
 * generator only needs the class signature, field set, method
 * signatures, and annotations — bytecode bodies are discarded for
 * speed and memory.
 */
public final class JarScanner {

    private JarScanner() {}

    public static List<ClassNode> scan(Path jarPath) throws IOException {
        List<ClassNode> out = new ArrayList<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!isClassFile(entry)) continue;
                try (InputStream in = jar.getInputStream(entry)) {
                    ClassReader reader = new ClassReader(in);
                    ClassNode node = new ClassNode();
                    reader.accept(node, ClassReader.SKIP_CODE);
                    out.add(node);
                }
            }
        }
        return out;
    }

    static boolean isClassFile(JarEntry entry) {
        if (entry.isDirectory()) return false;
        String name = entry.getName();
        if (!name.endsWith(".class")) return false;
        if (name.startsWith("META-INF/")) return false;
        if ("module-info.class".equals(name)) return false;
        if (name.endsWith("/module-info.class")) return false;
        return true;
    }
}

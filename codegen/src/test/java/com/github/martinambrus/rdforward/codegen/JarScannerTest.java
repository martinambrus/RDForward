package com.github.martinambrus.rdforward.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JarScannerTest {

    private static byte[] classBytes(String internalName) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, internalName,
                null, "java/lang/Object", null);
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static Path writeJar(Path dir, String fileName,
                                 java.util.Map<String, byte[]> entries) throws IOException {
        Path jar = dir.resolve(fileName);
        try (OutputStream fos = Files.newOutputStream(jar);
             JarOutputStream jos = new JarOutputStream(fos)) {
            for (var entry : entries.entrySet()) {
                jos.putNextEntry(new JarEntry(entry.getKey()));
                jos.write(entry.getValue());
                jos.closeEntry();
            }
        }
        return jar;
    }

    @Test
    void scansAllClassEntries(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir, "api.jar", java.util.Map.of(
                "pkg/Alpha.class", classBytes("pkg/Alpha"),
                "pkg/sub/Beta.class", classBytes("pkg/sub/Beta")));
        List<ClassNode> nodes = JarScanner.scan(jar);
        Set<String> names = nodes.stream().map(n -> n.name).collect(Collectors.toSet());
        assertEquals(Set.of("pkg/Alpha", "pkg/sub/Beta"), names);
    }

    @Test
    void ignoresNonClassEntries(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir, "api.jar", java.util.Map.of(
                "pkg/Alpha.class", classBytes("pkg/Alpha"),
                "resources/data.txt", "hi".getBytes(),
                "LICENSE", "MIT".getBytes()));
        List<ClassNode> nodes = JarScanner.scan(jar);
        assertEquals(1, nodes.size());
        assertEquals("pkg/Alpha", nodes.get(0).name);
    }

    @Test
    void ignoresMetaInf(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir, "api.jar", java.util.Map.of(
                "pkg/Alpha.class", classBytes("pkg/Alpha"),
                "META-INF/MANIFEST.MF", "hi".getBytes(),
                "META-INF/versions/9/Meta.class", classBytes("pkg/Meta")));
        List<ClassNode> nodes = JarScanner.scan(jar);
        assertEquals(1, nodes.size());
        assertEquals("pkg/Alpha", nodes.get(0).name);
    }

    @Test
    void ignoresModuleInfo(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir, "api.jar", java.util.Map.of(
                "pkg/Alpha.class", classBytes("pkg/Alpha"),
                "module-info.class", classBytes("module-info")));
        List<ClassNode> nodes = JarScanner.scan(jar);
        assertEquals(1, nodes.size());
        assertEquals("pkg/Alpha", nodes.get(0).name);
    }

    @Test
    void emptyJarReturnsEmptyList(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir, "empty.jar", java.util.Map.of());
        assertTrue(JarScanner.scan(jar).isEmpty());
    }

    @Test
    void nestedDirectoriesScanned(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir, "api.jar", java.util.Map.of(
                "a/b/c/Deep.class", classBytes("a/b/c/Deep")));
        List<ClassNode> nodes = JarScanner.scan(jar);
        assertEquals(1, nodes.size());
        assertEquals("a/b/c/Deep", nodes.get(0).name);
    }
}

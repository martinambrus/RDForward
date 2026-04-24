package com.github.martinambrus.rdforward.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StubWriterTest {

    private static ClassNode nodeFor(String internalName) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, internalName,
                null, "java/lang/Object", null);
        cw.visitEnd();
        ClassNode cn = new ClassNode();
        new org.objectweb.asm.ClassReader(cw.toByteArray()).accept(cn, 0);
        return cn;
    }

    @Test
    void writesNewFileAndUpdatesStats(@TempDir Path root) throws IOException {
        StubWriter writer = new StubWriter(root);
        ClassNode cn = nodeFor("org/bukkit/Material");
        assertTrue(writer.write(cn, "/* stub */\n"));
        Path target = root.resolve("org/bukkit/Material.java");
        assertTrue(Files.exists(target));
        assertEquals("/* stub */\n", Files.readString(target, StandardCharsets.UTF_8));
        assertEquals(1, writer.stats().written);
        assertEquals(0, writer.stats().preserved);
    }

    @Test
    void skipsPreservedFile(@TempDir Path root) throws IOException {
        Path target = root.resolve("org/bukkit/Material.java");
        Files.createDirectories(target.getParent());
        Files.writeString(target,
                "// @rdforward:preserve\npackage org.bukkit;\nclass Material {}\n",
                StandardCharsets.UTF_8);

        StubWriter writer = new StubWriter(root);
        ClassNode cn = nodeFor("org/bukkit/Material");
        assertFalse(writer.write(cn, "/* should not overwrite */\n"));
        assertTrue(Files.readString(target).contains("// @rdforward:preserve"));
        assertEquals(0, writer.stats().written);
        assertEquals(1, writer.stats().preserved);
    }

    @Test
    void overwritesNonPreservedFile(@TempDir Path root) throws IOException {
        Path target = root.resolve("org/bukkit/Material.java");
        Files.createDirectories(target.getParent());
        Files.writeString(target, "// old content\n", StandardCharsets.UTF_8);

        StubWriter writer = new StubWriter(root);
        ClassNode cn = nodeFor("org/bukkit/Material");
        assertTrue(writer.write(cn, "// new content\n"));
        assertEquals("// new content\n", Files.readString(target, StandardCharsets.UTF_8));
        assertEquals(1, writer.stats().written);
    }

    @Test
    void innerClassNameUsesDollar(@TempDir Path root) throws IOException {
        StubWriter writer = new StubWriter(root);
        ClassNode cn = nodeFor("org/bukkit/Outer$Inner");
        assertTrue(writer.write(cn, "/* inner */\n"));
        assertTrue(Files.exists(root.resolve("org/bukkit/Outer$Inner.java")));
    }

    @Test
    void defaultPackageWritesAtRoot(@TempDir Path root) throws IOException {
        StubWriter writer = new StubWriter(root);
        ClassNode cn = nodeFor("Top");
        assertTrue(writer.write(cn, "/* top */\n"));
        assertTrue(Files.exists(root.resolve("Top.java")));
    }

    @Test
    void targetPathDerivesFromInternalName(@TempDir Path root) {
        StubWriter writer = new StubWriter(root);
        assertEquals(root.resolve("a/b/c/X.java"),
                writer.targetPath("a/b/c/X"));
        assertEquals(root.resolve("Top.java"),
                writer.targetPath("Top"));
        assertEquals(root.resolve("a/b/Outer$Inner.java"),
                writer.targetPath("a/b/Outer$Inner"));
    }
}

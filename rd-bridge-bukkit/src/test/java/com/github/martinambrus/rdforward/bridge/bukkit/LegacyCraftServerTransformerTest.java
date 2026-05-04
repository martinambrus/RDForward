package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.bridge.bukkit.compat.LegacyCraftServerTransformer;
import com.github.martinambrus.rdforward.bridge.bukkit.compat.LegacyPluginClassLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the ASM bytecode rewriter that replaces
 * {@code org.bukkit.craftbukkit.v1_4_R1.CraftServer} with
 * {@code org.bukkit.craftbukkit.v1_21_R1.CraftServer} in plugin classes.
 * HomeSpawnPlus 1.7.4 hard-casts Bukkit.getServer() to the v1_4_R1 type.
 */
class LegacyCraftServerTransformerTest {

    private static final String OLD = "org/bukkit/craftbukkit/v1_4_R1/CraftServer";
    private static final String NEW = "org/bukkit/craftbukkit/v1_21_R1/CraftServer";

    @Test
    void transformRewritesSuperclass() {
        byte[] before = emitClassExtendingOld();
        byte[] after = LegacyCraftServerTransformer.transform(before);
        assertNotSame(before, after, "transform must produce new byte array");
        org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(after);
        assertEquals(NEW, reader.getSuperName(), "superclass must be v1_21_R1.CraftServer");
    }

    @Test
    void transformRewritesCheckcastInMethodBody() {
        byte[] before = emitWithCheckcast();
        byte[] after = LegacyCraftServerTransformer.transform(before);
        assertNotSame(before, after);
        // Verify by scanning for rewritten checkcast in method bodies using ASM
        org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(after);
        String[] methodResult = {null};
        reader.accept(new org.objectweb.asm.ClassVisitor(Opcodes.ASM9) {
            @Override
            public org.objectweb.asm.MethodVisitor visitMethod(int access, String name, String desc,
                    String sig, String[] exceptions) {
                return new org.objectweb.asm.MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitTypeInsn(int opcode, String type) {
                        if (opcode == Opcodes.CHECKCAST) methodResult[0] = type;
                        super.visitTypeInsn(opcode, type);
                    }
                };
            }
        }, 0);
        assertEquals(NEW, methodResult[0], "checkcast must target v1_21_R1.CraftServer");
    }

    @Test
    void transformRewritesMethodInvocation() {
        byte[] before = emitWithMethodCall();
        byte[] after = LegacyCraftServerTransformer.transform(before);
        assertNotSame(before, after);
        org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(after);
        String[] invokeOwner = {null};
        reader.accept(new org.objectweb.asm.ClassVisitor(Opcodes.ASM9) {
            @Override
            public org.objectweb.asm.MethodVisitor visitMethod(int access, String name, String desc,
                    String sig, String[] exceptions) {
                return new org.objectweb.asm.MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String mname,
                            String mdesc, boolean itf) {
                        if ("getCommandMap".equals(mname)) invokeOwner[0] = owner;
                        super.visitMethodInsn(opcode, owner, mname, mdesc, itf);
                    }
                };
            }
        }, 0);
        assertEquals(NEW, invokeOwner[0], "invokevirtual owner must be v1_21_R1.CraftServer");
    }

    @Test
    void transformSkipsClassWithoutV1_4_R1Reference() {
        byte[] clean = emitCleanClass();
        byte[] result = LegacyCraftServerTransformer.transform(clean);
        assertSame(clean, result,
                "bytecode without v1_4_R1 must be returned as-is (fast path)");
    }

    @Test
    void transformEndToEndThroughPluginClassLoader(@TempDir Path dir) throws Exception {
        // Emit a class that calls getCommandMap() on v1_4_R1.CraftServer.
        // After transformation through LegacyPluginClassLoader, the class
        // should link against v1_21_R1.CraftServer successfully.
        byte[] probe = emitWithMethodCall();
        Path jar = writeJar(dir, "transform/Probe", probe);
        try (LegacyPluginClassLoader loader = new LegacyPluginClassLoader(
                new URL[] { jar.toUri().toURL() }, getClass().getClassLoader())) {
            Class<?> cls = loader.loadClass("transform.Probe");
            // If transformation worked, the class loaded without LinkageError
            assertNotNull(cls);
        }
    }

    /* ---------- emit helpers ---------- */

    /** Emits a class that extends v1_4_R1.CraftServer. */
    private static byte[] emitClassExtendingOld() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "transform/Subclass", null, OLD, null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                "(Lcom/github/martinambrus/rdforward/api/server/Server;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, OLD, "<init>",
                "(Lcom/github/martinambrus/rdforward/api/server/Server;)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    /** Emits a class with a method that checkcasts to v1_4_R1.CraftServer. */
    private static byte[] emitWithCheckcast() {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "transform/CheckcastProbe", null,
                "java/lang/Object", null);
        defaultObjectCtor(cw);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "cast", "(Lorg/bukkit/Server;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, OLD);
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    /** Emits a class with a method that calls getCommandMap() on v1_4_R1.CraftServer. */
    private static byte[] emitWithMethodCall() {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "transform/Probe", null,
                "java/lang/Object", null);
        defaultObjectCtor(cw);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "getMap", "(Lorg/bukkit/Server;)Lorg/bukkit/command/SimpleCommandMap;", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, OLD);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, OLD,
                "getCommandMap", "()Lorg/bukkit/command/SimpleCommandMap;", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    /** Emits a clean class with no v1_4_R1 reference. */
    private static byte[] emitCleanClass() {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "transform/Clean", null,
                "java/lang/Object", null);
        defaultObjectCtor(cw);
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void defaultCtor(ClassWriter cw, String superName) {
        // CraftServer(Server) constructor descriptor
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                "(Lcom/github/martinambrus/rdforward/api/server/Server;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>",
                "(Lcom/github/martinambrus/rdforward/api/server/Server;)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    private static void defaultObjectCtor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private static Path writeJar(Path dir, String className, byte[] classBytes) throws IOException {
        Path jar = dir.resolve("probe.jar");
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jar))) {
            jos.putNextEntry(new JarEntry(className.replace('.', '/') + ".class"));
            jos.write(classBytes);
            jos.closeEntry();
        }
        return jar;
    }
}

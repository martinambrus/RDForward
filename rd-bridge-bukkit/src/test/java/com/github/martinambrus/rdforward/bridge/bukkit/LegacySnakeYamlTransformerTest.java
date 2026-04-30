package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.bridge.bukkit.compat.LegacyPluginClassLoader;
import com.github.martinambrus.rdforward.bridge.bukkit.compat.LegacySnakeYamlTransformer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Verifies the two ABI-bridge rules that
 * {@link LegacySnakeYamlTransformer} applies to legacy plugin bytecode.
 * The unit assertions run on the rewritten {@code byte[]} directly
 * (cheap, deterministic). The integration assertion drives the
 * {@link LegacyPluginClassLoader} load path so the rewritten class
 * actually links and executes against the bundled SnakeYAML 1.33 —
 * the same end-to-end path EssentialsX takes during YAML decode.
 */
class LegacySnakeYamlTransformerTest {

    /* ---------- typeDefinitions field rewrite ---------- */

    @Test
    void typeDefinitionsLookupRewriteRedirectsToCompatHelper() {
        byte[] before = emitFieldLookup("typeDefinitions");
        byte[] after = LegacySnakeYamlTransformer.transform(before);

        FieldCallScan scan = scan(after, "lookup");
        assertFalse(scan.containsClassGetDeclaredField,
                "transformed bytecode must not retain INVOKEVIRTUAL Class.getDeclaredField for typeDefinitions");
        assertTrue(scan.containsCompatHelper,
                "transformed bytecode must call SnakeYamlCompat.findDeclaredFieldRecursive");
    }

    @Test
    void unknownFieldNameLookupIsLeftUntouched() {
        byte[] before = emitFieldLookup("someOtherField");
        byte[] after = LegacySnakeYamlTransformer.transform(before);

        FieldCallScan scan = scan(after, "lookup");
        assertTrue(scan.containsClassGetDeclaredField,
                "non-whitelisted field names keep strict declared-only semantics");
        assertFalse(scan.containsCompatHelper,
                "compat helper must only fire for whitelisted SnakeYAML fields");
    }

    @Test
    void fieldLookupExecutionWalksHierarchyEndToEnd(@TempDir Path dir) throws Exception {
        // End-to-end: emit a class that does the same lookup
        // EssentialsX does, drop it into a real LegacyPluginClassLoader
        // (so all transform passes run), and invoke the method. Without
        // the rewrite this throws NoSuchFieldException; with the
        // rewrite it returns the field declared on BaseConstructor.
        Path jar = writeProbe(dir, "yaml/FieldProbe", emitFieldLookup("typeDefinitions"));
        try (LegacyPluginClassLoader loader = new LegacyPluginClassLoader(
                new URL[] { jar.toUri().toURL() }, getClass().getClassLoader())) {
            Class<?> probe = loader.loadClass("yaml.FieldProbe");
            Method m = probe.getDeclaredMethod("lookup");
            Object result = m.invoke(null);
            assertNotNull(result, "rewritten lookup must return a Field");
            assertEquals("typeDefinitions", ((Field) result).getName());
        }
    }

    /* ---------- constructScalar return-type widening ---------- */

    @Test
    void constructScalarObjectDescriptorRewrittenToString() {
        byte[] before = emitConstructScalarCaller();
        byte[] after = LegacySnakeYamlTransformer.transform(before);

        ConstructScalarScan scan = scanConstructScalar(after);
        assertFalse(scan.objectDescriptorPresent,
                "legacy (ScalarNode)Object descriptor must be rewritten");
        assertTrue(scan.stringDescriptorPresent,
                "transformer must emit (ScalarNode)String descriptor matching modern SnakeYAML");
    }

    @Test
    void constructScalarCallSucceedsAfterRewrite(@TempDir Path dir) throws Exception {
        // Drop the synthetic class through the real classloader so all
        // transformer passes run. The probe extends SnakeYAML's
        // Constructor (mirroring EssentialsX's BukkitConstructor) so the
        // protected {@code constructScalar} method is accessible from
        // its bytecode — same access rule the JVM enforces in production.
        // Pre-rewrite this throws NoSuchMethodError; post-rewrite the
        // call resolves to {@code BaseConstructor.constructScalar(ScalarNode)String}.
        Path jar = writeProbe(dir, "yaml/ScalarProbe", emitConstructScalarCaller());
        try (LegacyPluginClassLoader loader = new LegacyPluginClassLoader(
                new URL[] { jar.toUri().toURL() }, getClass().getClassLoader())) {
            Class<?> probe = loader.loadClass("yaml.ScalarProbe");
            Object instance = probe.getDeclaredConstructor().newInstance();
            Method m = probe.getDeclaredMethod("callConstructScalar", ScalarNode.class);
            ScalarNode node = new ScalarNode(Tag.STR, "hello", null, null,
                    org.yaml.snakeyaml.DumperOptions.ScalarStyle.PLAIN);
            Object result = m.invoke(instance, node);
            assertEquals("hello", result,
                    "rewritten call must return the scalar value via the modern String-typed method");
        }
    }

    /* ---------- emit / scan helpers ---------- */

    /** Emit a class with a single static {@code lookup()} method whose
     *  body matches EssentialsX's exact shape:
     *  <pre>
     *  return Constructor.class.getDeclaredField(name);
     *  </pre>
     *  parameterised on the field name so the negation case can drive
     *  the same emitter. */
    private static byte[] emitFieldLookup(String fieldName) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "yaml/FieldProbe", null, "java/lang/Object", null);
        defaultCtor(cw);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "lookup", "()Ljava/lang/reflect/Field;",
                null, new String[] { "java/lang/NoSuchFieldException" });
        mv.visitCode();
        mv.visitLdcInsn(org.objectweb.asm.Type.getType(
                "Lorg/yaml/snakeyaml/constructor/Constructor;"));
        mv.visitLdcInsn(fieldName);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
                "getDeclaredField",
                "(Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    /** Emit a class extending SnakeYAML's {@code Constructor} (mirroring
     *  EssentialsX's {@code BukkitConstructor}) with an instance method
     *  {@code callConstructScalar(ScalarNode) -> Object} that compiles to
     *  the legacy
     *  {@code INVOKEVIRTUAL Constructor.constructScalar(ScalarNode)Object}
     *  call site. The subclass relationship is what permits the
     *  protected-method access at JVM verification time — exactly the
     *  same shape EssentialsX Pre-2.14's {@code access$NXX} bridges use. */
    private static byte[] emitConstructScalarCaller() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "yaml/ScalarProbe", null,
                "org/yaml/snakeyaml/constructor/Constructor", null);

        // No-arg ctor that chains super() — Constructor() is public.
        MethodVisitor c = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        c.visitCode();
        c.visitVarInsn(Opcodes.ALOAD, 0);
        c.visitMethodInsn(Opcodes.INVOKESPECIAL,
                "org/yaml/snakeyaml/constructor/Constructor",
                "<init>", "()V", false);
        c.visitInsn(Opcodes.RETURN);
        c.visitMaxs(0, 0);
        c.visitEnd();

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC,
                "callConstructScalar",
                "(Lorg/yaml/snakeyaml/nodes/ScalarNode;)Ljava/lang/Object;",
                null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "yaml/ScalarProbe",
                "constructScalar",
                "(Lorg/yaml/snakeyaml/nodes/ScalarNode;)Ljava/lang/Object;",
                false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static FieldCallScan scan(byte[] classBytes, String methodName) {
        FieldCallScan scan = new FieldCallScan();
        new ClassReader(classBytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc,
                                             String sig, String[] exceptions) {
                if (!methodName.equals(name)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int op, String owner, String n, String d, boolean iface) {
                        if (op == Opcodes.INVOKEVIRTUAL && "java/lang/Class".equals(owner)
                                && "getDeclaredField".equals(n)) {
                            scan.containsClassGetDeclaredField = true;
                        }
                        if (op == Opcodes.INVOKESTATIC
                                && owner.endsWith("compat/SnakeYamlCompat")
                                && "findDeclaredFieldRecursive".equals(n)) {
                            scan.containsCompatHelper = true;
                        }
                    }
                };
            }
        }, 0);
        return scan;
    }

    private static ConstructScalarScan scanConstructScalar(byte[] classBytes) {
        ConstructScalarScan scan = new ConstructScalarScan();
        new ClassReader(classBytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc,
                                             String sig, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int op, String owner, String n, String d, boolean iface) {
                        if (op == Opcodes.INVOKEVIRTUAL && "constructScalar".equals(n)) {
                            if ("(Lorg/yaml/snakeyaml/nodes/ScalarNode;)Ljava/lang/Object;".equals(d)) {
                                scan.objectDescriptorPresent = true;
                            }
                            if ("(Lorg/yaml/snakeyaml/nodes/ScalarNode;)Ljava/lang/String;".equals(d)) {
                                scan.stringDescriptorPresent = true;
                            }
                        }
                    }
                };
            }
        }, 0);
        return scan;
    }

    private static Path writeProbe(Path dir, String resourceBase, byte[] classBytes) throws IOException {
        Path jar = dir.resolve(resourceBase.replace('/', '_') + ".jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry(resourceBase + ".class"));
            out.write(classBytes);
            out.closeEntry();
        }
        return jar;
    }

    private static void defaultCtor(ClassWriter cw) {
        MethodVisitor c = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        c.visitCode();
        c.visitVarInsn(Opcodes.ALOAD, 0);
        c.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        c.visitInsn(Opcodes.RETURN);
        c.visitMaxs(0, 0);
        c.visitEnd();
    }

    private static final class FieldCallScan {
        boolean containsClassGetDeclaredField;
        boolean containsCompatHelper;
    }

    private static final class ConstructScalarScan {
        boolean objectDescriptorPresent;
        boolean stringDescriptorPresent;
    }
}

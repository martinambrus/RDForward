package com.github.martinambrus.rdforward.codegen;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StubEmitterTest {

    private static final String ARTIFACT = "paper-api:1.0.0";

    private StubEmitter emitter() {
        return new StubEmitter(new DefaultValueResolver(), ARTIFACT);
    }

    private static ClassNode build(int classAccess, String internalName, String superName,
                                   String[] interfaces, Consumer<ClassWriter> body) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V21, classAccess, internalName, null,
                superName == null ? "java/lang/Object" : superName, interfaces);
        body.accept(cw);
        cw.visitEnd();
        ClassNode cn = new ClassNode();
        new org.objectweb.asm.ClassReader(cw.toByteArray()).accept(cn, 0);
        return cn;
    }

    private static ClassNode buildClass(String internalName, Consumer<ClassWriter> body) {
        return build(Opcodes.ACC_PUBLIC, internalName, null, null, body);
    }

    private static ClassNode buildInterface(String internalName, Consumer<ClassWriter> body) {
        return build(Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT,
                internalName, null, null, body);
    }

    @Test
    void skipsSyntheticClass() {
        ClassNode cn = build(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
                "pkg/Syn", null, null, cw -> {});
        assertTrue(emitter().emit(cn).isEmpty());
    }

    @Test
    void emitsEnumDeclarationWithEmptyConstants() {
        ClassNode cn = build(Opcodes.ACC_PUBLIC | Opcodes.ACC_ENUM,
                "pkg/Color", "java/lang/Enum", null, cw -> {});
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public enum Color {"));
        assertTrue(src.contains("    ;\n"));
    }

    @Test
    void emitsEnumConstants() {
        ClassNode cn = build(Opcodes.ACC_PUBLIC | Opcodes.ACC_ENUM,
                "pkg/Color", "java/lang/Enum", null, cw -> {
                    cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC
                            | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
                            "RED", "Lpkg/Color;", null, null).visitEnd();
                    cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC
                            | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
                            "GREEN", "Lpkg/Color;", null, null).visitEnd();
                    cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC
                            | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
                            "BLUE", "Lpkg/Color;", null, null).visitEnd();
                });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public enum Color {"));
        assertTrue(src.contains("    RED, GREEN, BLUE;\n"));
    }

    @Test
    void enumSkipsValuesAndValueOf() {
        ClassNode cn = build(Opcodes.ACC_PUBLIC | Opcodes.ACC_ENUM,
                "pkg/Color", "java/lang/Enum", null, cw -> {
                    MethodVisitor mv = cw.visitMethod(
                            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                            "values", "()[Lpkg/Color;", null, null);
                    mv.visitCode();
                    mv.visitInsn(Opcodes.ACONST_NULL);
                    mv.visitInsn(Opcodes.ARETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();

                    MethodVisitor mv2 = cw.visitMethod(
                            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                            "valueOf", "(Ljava/lang/String;)Lpkg/Color;", null, null);
                    mv2.visitCode();
                    mv2.visitInsn(Opcodes.ACONST_NULL);
                    mv2.visitInsn(Opcodes.ARETURN);
                    mv2.visitMaxs(0, 0);
                    mv2.visitEnd();
                });
        String src = emitter().emit(cn).orElseThrow();
        assertFalse(src.contains("values()"));
        assertFalse(src.contains("valueOf("));
    }

    @Test
    void enumSkipsConstructors() {
        ClassNode cn = build(Opcodes.ACC_PUBLIC | Opcodes.ACC_ENUM,
                "pkg/Color", "java/lang/Enum", null, cw -> {
                    MethodVisitor mv = cw.visitMethod(
                            Opcodes.ACC_PRIVATE, "<init>", "()V", null, null);
                    mv.visitCode();
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "java/lang/Enum", "<init>", "()V", false);
                    mv.visitInsn(Opcodes.RETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                });
        String src = emitter().emit(cn).orElseThrow();
        assertFalse(src.contains("Color("));
    }

    @Test
    void emitsAnnotationAsAtInterface() {
        ClassNode cn = build(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_ANNOTATION | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT,
                "pkg/Marker", null, new String[] { "java/lang/annotation/Annotation" }, cw -> {});
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public @interface Marker"));
    }

    @Test
    void skipsAnonymousInnerClass() {
        ClassNode cn = build(Opcodes.ACC_PUBLIC, "pkg/Outer$1",
                null, null, cw -> {});
        assertTrue(emitter().emit(cn).isEmpty());
    }

    @Test
    void emitsRecordWithComponents() {
        org.objectweb.asm.ClassWriter cw = new org.objectweb.asm.ClassWriter(0);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                "pkg/Point", null, "java/lang/Record", null);
        cw.visitRecordComponent("x", "I", null).visitEnd();
        cw.visitRecordComponent("y", "I", null).visitEnd();
        cw.visitEnd();
        ClassNode cn = new ClassNode();
        new org.objectweb.asm.ClassReader(cw.toByteArray()).accept(cn, 0);
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public record Point(int x, int y)"));
    }

    @Test
    void emitsPackageAndHeader() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {});
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.startsWith("package pkg;\n\n"));
        assertTrue(src.contains("/** Auto-generated stub from paper-api:1.0.0. See PLAN-FULL-STUBS.md. */"));
        assertTrue(src.contains("@SuppressWarnings({\"unchecked\", \"rawtypes\", \"unused\"})"));
    }

    @Test
    void emitsNoPackageWhenDefault() {
        ClassNode cn = buildClass("Top", cw -> {});
        String src = emitter().emit(cn).orElseThrow();
        assertFalse(src.contains("package"));
    }

    @Test
    void emitsPublicClassDeclaration() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {});
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public class Sample {"));
    }

    @Test
    void dropsFinalModifierOnClass() {
        ClassNode cn = build(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                "pkg/Sealed", null, null, cw -> {});
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public class Sealed"));
        assertFalse(src.contains("final"));
    }

    @Test
    void emitsExtendsClause() {
        ClassNode cn = build(Opcodes.ACC_PUBLIC, "pkg/Dog",
                "pkg/Animal", null, cw -> {});
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public class Dog extends pkg.Animal"));
    }

    @Test
    void omitsExtendsObject() {
        ClassNode cn = buildClass("pkg/Plain", cw -> {});
        String src = emitter().emit(cn).orElseThrow();
        assertFalse(src.contains("extends"));
    }

    @Test
    void emitsImplementsClause() {
        ClassNode cn = build(Opcodes.ACC_PUBLIC, "pkg/Impl", null,
                new String[] { "pkg/Alpha", "pkg/Beta" }, cw -> {});
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public class Impl implements pkg.Alpha, pkg.Beta"));
    }

    @Test
    void interfaceUsesExtendsForSuperInterfaces() {
        ClassNode cn = build(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT,
                "pkg/Child", null, new String[] { "pkg/Parent" }, cw -> {});
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public interface Child extends pkg.Parent"));
    }

    @Test
    void emitsPublicFieldWithStringLiteral() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            FieldVisitor fv = cw.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                    "GREETING", "Ljava/lang/String;", null, "hi");
            fv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public static final java.lang.String GREETING = \"hi\";"));
    }

    @Test
    void emitsPublicFieldWithIntLiteral() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                    "MAX", "I", null, 42).visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public static final int MAX = 42;"));
    }

    @Test
    void emitsBooleanFieldFromIntValue() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                    "FLAG", "Z", null, 1).visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public static final boolean FLAG = true;"));
    }

    @Test
    void emitsLongFieldLiteral() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                    "BIG", "J", null, 1234567890123L).visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public static final long BIG = 1234567890123L;"));
    }

    @Test
    void emitsFloatFieldLiteral() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                    "RATIO", "F", null, 1.5f).visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public static final float RATIO = 1.5f;"));
    }

    @Test
    void emitsPublicFieldWithoutValueUsesResolver() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            cw.visitField(Opcodes.ACC_PUBLIC, "id", "I", null, null).visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public int id = 0;"));
    }

    @Test
    void skipsPrivateField() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            cw.visitField(Opcodes.ACC_PRIVATE, "secret", "I", null, null).visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertFalse(src.contains("secret"));
    }

    @Test
    void skipsPackagePrivateField() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            cw.visitField(0, "pkgField", "I", null, null).visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertFalse(src.contains("pkgField"));
    }

    @Test
    void skipsSyntheticField() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
                    "synth", "I", null, null).visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertFalse(src.contains("synth"));
    }

    @Test
    void emitsPublicConstructor() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "<init>", "(ILjava/lang/String;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public Sample(int arg0, java.lang.String arg1) {}"));
    }

    @Test
    void emitsProtectedConstructor() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PROTECTED, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("protected Sample() {}"));
    }

    @Test
    void skipsPrivateConstructorButSynthesizesNoArg() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PRIVATE, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertFalse(src.contains("private Sample("));
        assertTrue(src.contains("public Sample()"));
    }

    @Test
    void interfaceHasNoConstructors() {
        ClassNode cn = buildInterface("pkg/Iface", cw -> {});
        String src = emitter().emit(cn).orElseThrow();
        assertFalse(src.contains("Iface("));
    }

    @Test
    void constructorWithThrows() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V",
                    null, new String[] { "java/io/IOException" });
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public Sample() throws java.io.IOException {}"));
    }

    @Test
    void lifecycleMethodHasEmptyBody() {
        ClassNode cn = buildClass("pkg/Plug", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "onEnable", "()V", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public void onEnable() {\n    }\n"));
        assertFalse(src.contains("StubCallLog"));
    }

    @Test
    void getterReturnsDefault() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "getCount", "()I", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public int getCount() {\n        return 0;\n    }"));
    }

    @Test
    void voidSetterLogsAndReturnsNothing() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "setFoo", "(I)V", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public void setFoo(int arg0) {"));
        assertTrue(src.contains(
                "com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, \"pkg.Sample.setFoo(I)V\");"));
        assertFalse(src.contains("return 0;"));
    }

    @Test
    void nonVoidArgMethodLogsAndReturnsDefault() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "transform", "(I)Ljava/lang/String;", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("StubCallLog.logOnce(null, \"pkg.Sample.transform(I)Ljava/lang/String;\");"));
        assertTrue(src.contains("return null;"));
    }

    @Test
    void builderChainLogsAndReturnsThis() {
        ClassNode cn = buildClass("pkg/Builder", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "withFoo", "(I)Lpkg/Builder;", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("StubCallLog.logOnce(null, \"pkg.Builder.withFoo(I)Lpkg/Builder;\");"));
        assertTrue(src.contains("return this;"));
    }

    @Test
    void staticFactoryReturnsDefault() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "of", "()Lpkg/Sample;", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public static pkg.Sample of() {\n        return null;\n    }"));
        assertFalse(src.contains("StubCallLog"));
    }

    @Test
    void keepsForRemovalDeprecated() {
        // Methods marked @Deprecated(forRemoval=true) are still part of
        // the upstream API; dropping them would also strip overrides
        // required by interfaces the class implements.
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "gone", "()V", null, null);
            AnnotationVisitor av = mv.visitAnnotation("Ljava/lang/Deprecated;", true);
            av.visit("forRemoval", Boolean.TRUE);
            av.visitEnd();
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("gone"));
    }

    @Test
    void keepsDeprecatedNotForRemoval() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "oldSet", "(I)V", null, null);
            AnnotationVisitor av = mv.visitAnnotation("Ljava/lang/Deprecated;", true);
            av.visit("forRemoval", Boolean.FALSE);
            av.visitEnd();
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public void oldSet(int arg0)"));
    }

    @Test
    void keepsDeprecatedWithNoValues() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "aged", "(I)V", null, null);
            mv.visitAnnotation("Ljava/lang/Deprecated;", true).visitEnd();
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public void aged(int arg0)"));
    }

    @Test
    void skipsSyntheticMethod() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
                    "synMethod", "()V", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertFalse(src.contains("synMethod"));
    }

    @Test
    void keepsBridgeMethod() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE,
                    "bridgeMethod", "()V", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("bridgeMethod"));
    }

    @Test
    void skipsPackagePrivateMethod() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(0, "pkgOnly", "()V", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertFalse(src.contains("pkgOnly"));
    }

    @Test
    void interfaceAbstractMethodHasNoBody() {
        ClassNode cn = buildInterface("pkg/Iface", cw -> {
            cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
                    "getFoo", "()I", null, null).visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("int getFoo();"));
        assertFalse(src.contains("default"));
    }

    @Test
    void interfaceDefaultMethodKeepsBody() {
        ClassNode cn = buildInterface("pkg/Iface", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "getFoo", "()I", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("default int getFoo()"));
        assertTrue(src.contains("return 0;"));
    }

    @Test
    void interfaceStaticMethodKeepsStatic() {
        ClassNode cn = buildInterface("pkg/Iface", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "of",
                    "()Lpkg/Iface;", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("static pkg.Iface of()"));
        assertFalse(src.contains("default"));
    }

    @Test
    void classStaticMethodKeepsStatic() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "broadcast", "(Ljava/lang/String;)V", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public static void broadcast(java.lang.String arg0)"));
    }

    @Test
    void classFinalMethodKeepsFinal() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                    "lock", "()V", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public final void lock()"));
    }

    @Test
    void methodThrowsClauseRendered() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "read", "()I",
                    null, new String[] { "java/io/IOException" });
            mv.visitCode();
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public int read() throws java.io.IOException"));
    }

    @Test
    void arrayAndPrimitiveParamTypes() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "doit",
                    "([I[[Ljava/lang/String;)V", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public void doit(int[] arg0, java.lang.String[][] arg1)"));
    }

    @Test
    void innerClassDollarPreservedInName() {
        ClassNode cn = build(Opcodes.ACC_PUBLIC, "pkg/Outer$Inner",
                null, null, cw -> {});
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("public class Outer$Inner {"));
    }

    @Test
    void escapeHandlesQuotesAndBackslashes() {
        ClassNode cn = buildClass("pkg/Sample", cw -> {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                    "MSG", "Ljava/lang/String;", null, "a\"b\\c\n").visitEnd();
        });
        String src = emitter().emit(cn).orElseThrow();
        assertTrue(src.contains("\"a\\\"b\\\\c\\n\""));
    }
}

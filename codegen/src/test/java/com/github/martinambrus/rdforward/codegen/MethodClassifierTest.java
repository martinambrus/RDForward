package com.github.martinambrus.rdforward.codegen;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MethodClassifierTest {

    private static ClassNode build(String owner, Consumer<ClassWriter> body) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, owner, null, "java/lang/Object", null);
        body.accept(cw);
        cw.visitEnd();
        ClassNode cn = new ClassNode();
        new org.objectweb.asm.ClassReader(cw.toByteArray()).accept(cn, 0);
        return cn;
    }

    private static Map<String, MethodNode> methodsByName(ClassNode cn) {
        Map<String, MethodNode> out = new HashMap<>();
        for (MethodNode m : cn.methods) out.put(m.name, m);
        return out;
    }

    private static void emptyBody(MethodVisitor mv, int returnOpcode) {
        mv.visitCode();
        if (returnOpcode == Opcodes.IRETURN) mv.visitInsn(Opcodes.ICONST_0);
        else if (returnOpcode == Opcodes.LRETURN) mv.visitInsn(Opcodes.LCONST_0);
        else if (returnOpcode == Opcodes.FRETURN) mv.visitInsn(Opcodes.FCONST_0);
        else if (returnOpcode == Opcodes.DRETURN) mv.visitInsn(Opcodes.DCONST_0);
        else if (returnOpcode == Opcodes.ARETURN) mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(returnOpcode);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    @Test
    void constructorIsConstructor() {
        ClassNode cn = build("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });
        MethodNode m = methodsByName(cn).get("<init>");
        assertEquals(StubSemantics.CONSTRUCTOR, MethodClassifier.classify(cn, m));
    }

    @Test
    void staticInitializerIsStaticInitializer() {
        ClassNode cn = build("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            emptyBody(mv, Opcodes.RETURN);
        });
        MethodNode m = methodsByName(cn).get("<clinit>");
        assertEquals(StubSemantics.STATIC_INITIALIZER, MethodClassifier.classify(cn, m));
    }

    @Test
    void onEnableIsLifecycle() {
        ClassNode cn = build("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "onEnable", "()V", null, null);
            emptyBody(mv, Opcodes.RETURN);
        });
        MethodNode m = methodsByName(cn).get("onEnable");
        assertEquals(StubSemantics.LIFECYCLE, MethodClassifier.classify(cn, m));
    }

    @Test
    void onDisableIsLifecycle() {
        ClassNode cn = build("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "onDisable", "()V", null, null);
            emptyBody(mv, Opcodes.RETURN);
        });
        MethodNode m = methodsByName(cn).get("onDisable");
        assertEquals(StubSemantics.LIFECYCLE, MethodClassifier.classify(cn, m));
    }

    @Test
    void onInitializeIsLifecycle() {
        ClassNode cn = build("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "onInitialize", "()V", null, null);
            emptyBody(mv, Opcodes.RETURN);
        });
        MethodNode m = methodsByName(cn).get("onInitialize");
        assertEquals(StubSemantics.LIFECYCLE, MethodClassifier.classify(cn, m));
    }

    @Test
    void voidInstanceMethodIsSetterWarnNoop() {
        ClassNode cn = build("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "setFoo", "(I)V", null, null);
            emptyBody(mv, Opcodes.RETURN);
        });
        MethodNode m = methodsByName(cn).get("setFoo");
        assertEquals(StubSemantics.SETTER_WARN_NOOP, MethodClassifier.classify(cn, m));
    }

    @Test
    void zeroArgNonVoidIsGetterDefault() {
        ClassNode cn = build("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "getFoo", "()I", null, null);
            emptyBody(mv, Opcodes.IRETURN);
        });
        MethodNode m = methodsByName(cn).get("getFoo");
        assertEquals(StubSemantics.GETTER_DEFAULT, MethodClassifier.classify(cn, m));
    }

    @Test
    void zeroArgNonVoidNoPrefixIsGetterDefault() {
        ClassNode cn = build("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "inventory", "()Ljava/lang/Object;", null, null);
            emptyBody(mv, Opcodes.ARETURN);
        });
        MethodNode m = methodsByName(cn).get("inventory");
        assertEquals(StubSemantics.GETTER_DEFAULT, MethodClassifier.classify(cn, m));
    }

    @Test
    void builderChainWhenReturnTypeEqualsOwner() {
        ClassNode cn = build("pkg/Builder", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "withFoo", "(I)Lpkg/Builder;", null, null);
            emptyBody(mv, Opcodes.ARETURN);
        });
        MethodNode m = methodsByName(cn).get("withFoo");
        assertEquals(StubSemantics.BUILDER_CHAIN, MethodClassifier.classify(cn, m));
    }

    @Test
    void nonVoidArgMethodReturningOtherClassIsSetterWarnNoop() {
        ClassNode cn = build("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "transform", "(I)Ljava/lang/String;", null, null);
            emptyBody(mv, Opcodes.ARETURN);
        });
        MethodNode m = methodsByName(cn).get("transform");
        assertEquals(StubSemantics.SETTER_WARN_NOOP, MethodClassifier.classify(cn, m));
    }

    @Test
    void staticNonVoidIsStaticFactory() {
        ClassNode cn = build("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "of", "()Lpkg/Sample;", null, null);
            emptyBody(mv, Opcodes.ARETURN);
        });
        MethodNode m = methodsByName(cn).get("of");
        assertEquals(StubSemantics.STATIC_FACTORY, MethodClassifier.classify(cn, m));
    }

    @Test
    void staticVoidIsSetterWarnNoop() {
        ClassNode cn = build("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "broadcast", "(Ljava/lang/String;)V", null, null);
            emptyBody(mv, Opcodes.RETURN);
        });
        MethodNode m = methodsByName(cn).get("broadcast");
        assertEquals(StubSemantics.SETTER_WARN_NOOP, MethodClassifier.classify(cn, m));
    }

    @Test
    void lifecycleNameWithArgsIsNotLifecycle() {
        ClassNode cn = build("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC, "onEnable", "(Z)V", null, null);
            emptyBody(mv, Opcodes.RETURN);
        });
        MethodNode m = methodsByName(cn).get("onEnable");
        assertEquals(StubSemantics.SETTER_WARN_NOOP, MethodClassifier.classify(cn, m),
                "onEnable(boolean) is not the lifecycle hook — should be warn+noop");
    }

    @Test
    void lifecycleNameAsStaticIsNotLifecycle() {
        ClassNode cn = build("pkg/Sample", cw -> {
            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "onEnable", "()V", null, null);
            emptyBody(mv, Opcodes.RETURN);
        });
        MethodNode m = methodsByName(cn).get("onEnable");
        assertEquals(StubSemantics.SETTER_WARN_NOOP, MethodClassifier.classify(cn, m),
                "static onEnable is not a plugin lifecycle override — warn+noop");
    }
}

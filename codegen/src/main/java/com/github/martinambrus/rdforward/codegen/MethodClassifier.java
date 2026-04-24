package com.github.martinambrus.rdforward.codegen;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Set;

/**
 * Maps an ASM {@link MethodNode} to a {@link StubSemantics} value per
 * the Hybrid Semantics Contract (see {@code PLAN-FULL-STUBS.md} §2).
 *
 * <p>Classification rules, in order:
 *
 * <ol>
 *   <li>{@code <init>} -&gt; {@link StubSemantics#CONSTRUCTOR}</li>
 *   <li>{@code <clinit>} -&gt; {@link StubSemantics#STATIC_INITIALIZER}</li>
 *   <li>Lifecycle name (onLoad, onEnable, onDisable, onInitialize,
 *       onInitializeServer, onInitializeClient) and instance method
 *       -&gt; {@link StubSemantics#LIFECYCLE}</li>
 *   <li>Static non-void -&gt; {@link StubSemantics#STATIC_FACTORY}</li>
 *   <li>Static void -&gt; {@link StubSemantics#SETTER_WARN_NOOP}</li>
 *   <li>Instance void -&gt; {@link StubSemantics#SETTER_WARN_NOOP}</li>
 *   <li>Instance, zero-arg, non-void -&gt; {@link StubSemantics#GETTER_DEFAULT}</li>
 *   <li>Instance, has args, returns declaring class -&gt; {@link StubSemantics#BUILDER_CHAIN}</li>
 *   <li>Anything else -&gt; {@link StubSemantics#SETTER_WARN_NOOP}</li>
 * </ol>
 */
public final class MethodClassifier {

    private static final Set<String> LIFECYCLE_NAMES = Set.of(
            "onLoad",
            "onEnable",
            "onDisable",
            "onInitialize",
            "onInitializeServer",
            "onInitializeClient"
    );

    private MethodClassifier() {}

    public static StubSemantics classify(ClassNode owner, MethodNode method) {
        if ("<init>".equals(method.name)) return StubSemantics.CONSTRUCTOR;
        if ("<clinit>".equals(method.name)) return StubSemantics.STATIC_INITIALIZER;

        Type[] argTypes = Type.getArgumentTypes(method.desc);
        Type retType = Type.getReturnType(method.desc);
        boolean isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
        boolean isVoid = retType.getSort() == Type.VOID;

        if (!isStatic
                && argTypes.length == 0
                && isVoid
                && LIFECYCLE_NAMES.contains(method.name)) {
            return StubSemantics.LIFECYCLE;
        }

        if (isStatic) {
            return isVoid ? StubSemantics.SETTER_WARN_NOOP : StubSemantics.STATIC_FACTORY;
        }

        if (isVoid) return StubSemantics.SETTER_WARN_NOOP;

        if (argTypes.length == 0) return StubSemantics.GETTER_DEFAULT;

        if (retType.getSort() == Type.OBJECT
                && retType.getInternalName().equals(owner.name)) {
            return StubSemantics.BUILDER_CHAIN;
        }

        return StubSemantics.SETTER_WARN_NOOP;
    }
}

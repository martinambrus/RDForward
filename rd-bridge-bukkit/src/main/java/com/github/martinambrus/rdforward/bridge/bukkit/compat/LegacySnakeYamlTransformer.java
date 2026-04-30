// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Bytecode rewriter for plugin classes that hit two ABI breaks in the
 * bundled SnakeYAML 1.33:
 *
 * <ol>
 *   <li><b>typeDefinitions field moved.</b> SnakeYAML 1.13 promoted
 *       this field from {@code Constructor} to {@code BaseConstructor},
 *       so the strict declared-only lookup
 *       <pre>
 *       LDC "typeDefinitions"
 *       INVOKEVIRTUAL java/lang/Class.getDeclaredField(String)Field
 *       </pre>
 *       now throws {@code NoSuchFieldException}. The transformer
 *       replaces those call sites with
 *       {@link SnakeYamlCompat#findDeclaredFieldRecursive(Class, String)},
 *       which walks the class hierarchy.</li>
 *
 *   <li><b>constructScalar return type tightened.</b> Pre-1.13
 *       SnakeYAML declared {@code Object constructScalar(ScalarNode)};
 *       1.13+ tightened the return type to {@code String}. Plugins
 *       (EssentialsX Pre-2.14's {@code BukkitConstructor.access$NXX}
 *       bridges) still emit
 *       {@code INVOKEVIRTUAL X.constructScalar(ScalarNode)Object},
 *       which doesn't match the live signature and throws
 *       {@code NoSuchMethodError} on every YAML load. The transformer
 *       rewrites the descriptor to {@code (ScalarNode)String}; the
 *       returned {@code String} reference is assignable to {@code Object}
 *       so any subsequent {@code areturn} or {@code astore} into an
 *       Object-typed slot still verifies.</li>
 * </ol>
 *
 * <p>Other {@code getDeclaredField} reflection (different field names,
 * or names not loaded as a constant immediately before the call) is
 * left untouched, so plugins that genuinely want strict declared-only
 * semantics keep them. The {@code constructScalar} rewrite is keyed
 * off the SnakeYAML-typed {@code ScalarNode} parameter, so unrelated
 * methods named {@code constructScalar} on non-SnakeYAML types are
 * not affected.
 */
public final class LegacySnakeYamlTransformer {

    private static final String SNAKE_COMPAT =
            "com/github/martinambrus/rdforward/bridge/bukkit/compat/SnakeYamlCompat";

    private LegacySnakeYamlTransformer() {}

    public static byte[] transform(byte[] original) {
        ClassReader reader = new ClassReader(original);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new RewriterAdapter(writer), 0);
        return writer.toByteArray();
    }

    /** Field names that legacy plugins reach via the cross-version
     *  reflection idiom above. Conservative whitelist — only names
     *  we have confirmed crash on the modern hierarchy are rewritten. */
    private static boolean isKnownSnakeYamlField(String name) {
        return "typeDefinitions".equals(name);
    }

    private static final class RewriterAdapter extends ClassVisitor {
        RewriterAdapter(ClassVisitor delegate) { super(Opcodes.ASM9, delegate); }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            return mv == null ? null : new MethodRewriter(mv);
        }
    }

    /** Tracks the most recent string LDC and rewrites the immediately
     *  following matching {@code Class.getDeclaredField} call. The
     *  state is cleared by every other instruction visitor so that
     *  intervening bytecode invalidates the pattern match. */
    private static final class MethodRewriter extends MethodVisitor {

        private String lastLdcString;

        MethodRewriter(MethodVisitor mv) { super(Opcodes.ASM9, mv); }

        @Override
        public void visitLdcInsn(Object value) {
            lastLdcString = (value instanceof String s) ? s : null;
            super.visitLdcInsn(value);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name,
                                    String descriptor, boolean isInterface) {
            if (opcode == Opcodes.INVOKEVIRTUAL
                    && "java/lang/Class".equals(owner)
                    && "getDeclaredField".equals(name)
                    && "(Ljava/lang/String;)Ljava/lang/reflect/Field;".equals(descriptor)
                    && lastLdcString != null
                    && isKnownSnakeYamlField(lastLdcString)) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, SNAKE_COMPAT,
                        "findDeclaredFieldRecursive",
                        "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;",
                        false);
                lastLdcString = null;
                return;
            }
            if (opcode == Opcodes.INVOKEVIRTUAL
                    && "constructScalar".equals(name)
                    && "(Lorg/yaml/snakeyaml/nodes/ScalarNode;)Ljava/lang/Object;".equals(descriptor)) {
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, "constructScalar",
                        "(Lorg/yaml/snakeyaml/nodes/ScalarNode;)Ljava/lang/String;",
                        isInterface);
                lastLdcString = null;
                return;
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            lastLdcString = null;
        }

        @Override public void visitInsn(int op) { super.visitInsn(op); lastLdcString = null; }
        @Override public void visitVarInsn(int op, int v) { super.visitVarInsn(op, v); lastLdcString = null; }
        @Override public void visitTypeInsn(int op, String t) { super.visitTypeInsn(op, t); lastLdcString = null; }
        @Override public void visitFieldInsn(int op, String o, String n, String d) { super.visitFieldInsn(op, o, n, d); lastLdcString = null; }
        @Override public void visitIntInsn(int op, int o) { super.visitIntInsn(op, o); lastLdcString = null; }
        @Override public void visitJumpInsn(int op, org.objectweb.asm.Label l) { super.visitJumpInsn(op, l); lastLdcString = null; }
        @Override public void visitIincInsn(int v, int i) { super.visitIincInsn(v, i); lastLdcString = null; }
        @Override public void visitInvokeDynamicInsn(String n, String d, org.objectweb.asm.Handle h, Object... a) { super.visitInvokeDynamicInsn(n, d, h, a); lastLdcString = null; }
        @Override public void visitMultiANewArrayInsn(String d, int n) { super.visitMultiANewArrayInsn(d, n); lastLdcString = null; }
    }
}

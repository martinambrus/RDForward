// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Bytecode rewriter for plugin classes compiled against pre-1.6 Bukkit's
 * {@code int}-typed health API. Real Bukkit changed {@code getHealth} /
 * {@code setHealth} / {@code getMaxHealth} / {@code setMaxHealth} from
 * {@code int} to {@code double} in 1.6 (Damageable). Old plugins (e.g.
 * Essentials 2.9.x's {@code PlayerExtension}) emit {@code INVOKEINTERFACE
 * org/bukkit/entity/Player.getHealth()I} which doesn't resolve against
 * the modern interface, throwing
 * {@code NoSuchMethodError: 'int org.bukkit.entity.Player.getHealth()'}
 * when {@code /whois} (or any other code path that touches health) runs.
 *
 * <p>This transformer rewrites every {@code INVOKEINTERFACE owner.<name>(<int-shape>)}
 * call where {@code owner} is a Bukkit entity interface ({@code Player},
 * {@code HumanEntity}, {@code LivingEntity}, {@code Damageable}) into an
 * {@code INVOKESTATIC} call to {@link HealthCompat}, which round-trips
 * through the modern double-typed method. Stack shape is preserved.
 *
 * <p>Conservative scope: only the four health-related signatures are
 * touched. Any other ABI mismatch surfaces normally so we know to add a
 * shim.
 */
public final class LegacyHealthTransformer {

    private static final String HEALTH_COMPAT =
            "com/github/martinambrus/rdforward/bridge/bukkit/compat/HealthCompat";
    private static final String DAMAGEABLE = "org/bukkit/entity/Damageable";

    private LegacyHealthTransformer() {}

    public static byte[] transform(byte[] original) {
        ClassReader reader = new ClassReader(original);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new RewriterAdapter(writer), 0);
        return writer.toByteArray();
    }

    /** True if {@code owner} is a Bukkit entity interface that pre-1.6
     *  declared int-shaped health methods. Damageable is the modern home
     *  for them; the rest are subinterfaces that inherit the legacy
     *  signatures from old plugin compile-time API jars. */
    private static boolean isBukkitEntityOwner(String owner) {
        return "org/bukkit/entity/Player".equals(owner)
                || "org/bukkit/entity/HumanEntity".equals(owner)
                || "org/bukkit/entity/LivingEntity".equals(owner)
                || DAMAGEABLE.equals(owner);
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

    private static final class MethodRewriter extends MethodVisitor {
        MethodRewriter(MethodVisitor mv) { super(Opcodes.ASM9, mv); }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (opcode == Opcodes.INVOKEINTERFACE && isBukkitEntityOwner(owner)) {
                if ("getHealth".equals(name) && "()I".equals(descriptor)) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, HEALTH_COMPAT, "getHealth",
                            "(L" + DAMAGEABLE + ";)I", false);
                    return;
                }
                if ("getMaxHealth".equals(name) && "()I".equals(descriptor)) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, HEALTH_COMPAT, "getMaxHealth",
                            "(L" + DAMAGEABLE + ";)I", false);
                    return;
                }
                if ("setHealth".equals(name) && "(I)V".equals(descriptor)) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, HEALTH_COMPAT, "setHealth",
                            "(L" + DAMAGEABLE + ";I)V", false);
                    return;
                }
                if ("setMaxHealth".equals(name) && "(I)V".equals(descriptor)) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, HEALTH_COMPAT, "setMaxHealth",
                            "(L" + DAMAGEABLE + ";I)V", false);
                    return;
                }
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }
}

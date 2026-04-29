// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Bytecode rewriter for pre-Guava-11 plugin classes. Rewrites two
 * call-site shapes that broke when CraftBukkit's bundled Guava jumped
 * from v10 to v11:
 *
 * <ul>
 *   <li>{@code CacheBuilder.build(CacheLoader) -> Cache} (gone in modern
 *       Guava; the only build(CacheLoader) overload returns
 *       {@code LoadingCache}). Rewrites the descriptor's return type to
 *       {@code LoadingCache}; the JVM accepts the subtype where
 *       {@code Cache} is expected (field assignment, locals).</li>
 *   <li>{@code Cache.get(Object)} (moved to {@code LoadingCache}). Rewritten
 *       to {@code INVOKESTATIC GuavaCompat.cacheGet(Cache, Object)} —
 *       receiver becomes first arg, no stack-shape change, stack maps stay
 *       valid.</li>
 *   <li>{@code CacheBuilder.maximumSize(int) -> CacheBuilder} (only
 *       {@code maximumSize(long)} survives in modern Guava). Rewritten
 *       to {@code INVOKESTATIC GuavaCompat.maximumSize(CacheBuilder, int)}
 *       so the int argument flows through unchanged — receiver becomes
 *       first arg, stack shape stays (CacheBuilder + int → CacheBuilder),
 *       no I2L injection or frame fixup needed.</li>
 * </ul>
 *
 * <p>Conservative scope: only the two signatures above are touched. Any
 * other Guava ABI mismatch surfaces as a normal {@code NoSuchMethodError}
 * so we know to add a shim.
 */
public final class LegacyGuavaTransformer {

    private static final String CACHE_BUILDER = "com/google/common/cache/CacheBuilder";
    private static final String CACHE = "com/google/common/cache/Cache";
    private static final String LOADING_CACHE = "com/google/common/cache/LoadingCache";
    private static final String CACHE_LOADER = "com/google/common/cache/CacheLoader";

    private static final String GUAVA_COMPAT = "com/github/martinambrus/rdforward/bridge/bukkit/compat/GuavaCompat";

    private LegacyGuavaTransformer() {}

    /** Rewrite {@code original} class bytes. The {@link MethodRewriter}
     *  filter is a no-op on calls outside the small whitelist, so every
     *  class pays only the visitor walk — no constant-pool sniff is
     *  needed. Returns a possibly-rewritten byte[]; ASM may copy the
     *  buffer even when no instructions changed, which is acceptable
     *  for the per-plugin-jar load path (one-time cost). */
    public static byte[] transform(byte[] original) {
        ClassReader reader = new ClassReader(original);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new RewriterAdapter(writer), 0);
        return writer.toByteArray();
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
            if (opcode == Opcodes.INVOKEVIRTUAL
                    && CACHE_BUILDER.equals(owner)
                    && "build".equals(name)
                    && ("(L" + CACHE_LOADER + ";)L" + CACHE + ";").equals(descriptor)) {
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CACHE_BUILDER, "build",
                        "(L" + CACHE_LOADER + ";)L" + LOADING_CACHE + ";", false);
                return;
            }
            if (opcode == Opcodes.INVOKEINTERFACE
                    && CACHE.equals(owner)
                    && "get".equals(name)
                    && "(Ljava/lang/Object;)Ljava/lang/Object;".equals(descriptor)) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, GUAVA_COMPAT, "cacheGet",
                        "(L" + CACHE + ";Ljava/lang/Object;)Ljava/lang/Object;", false);
                return;
            }
            if (opcode == Opcodes.INVOKEVIRTUAL
                    && CACHE_BUILDER.equals(owner)
                    && "maximumSize".equals(name)
                    && ("(I)L" + CACHE_BUILDER + ";").equals(descriptor)) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, GUAVA_COMPAT, "maximumSize",
                        "(L" + CACHE_BUILDER + ";I)L" + CACHE_BUILDER + ";", false);
                return;
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }
}

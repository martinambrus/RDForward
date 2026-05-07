// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Bytecode rewriter for plugins compiled against pre-modernization
 * Bukkit, where types like {@code Sound}, {@code Material},
 * {@code EntityType}, {@code Particle}, {@code GameMode} were Java
 * enums. Modern Paper makes them interfaces extending
 * {@link org.bukkit.util.OldEnum}.
 *
 * <p>The breakage: a plugin's bytecode says
 * {@code Util.listValuesPretty([Ljava/lang/Enum;)Ljava/lang/String;}
 * and passes {@code Sound[]}. The JVM verifier rejects
 * {@code [Lorg/bukkit/Sound;} as {@code [Ljava/lang/Enum;} because
 * {@code Sound} is no longer an Enum subtype.
 *
 * <p>The fix: rewrite every occurrence of {@code [Ljava/lang/Enum;}
 * inside the plugin's bytecode (method descriptors, callsite
 * methodrefs) to {@code [Ljava/lang/Object;}. Plugin's own
 * {@code Enum[]}-shaped utilities thus accept the interface array
 * directly (verifier accepts because {@code [Lorg/bukkit/Sound;}
 * &lt;: {@code [Ljava/lang/Object;}).
 *
 * <p>Inside the rewritten methods, calls to {@code Enum#name()}
 * and {@code Enum#ordinal()} on now-Object elements are forwarded
 * to {@link LegacyEnumCompat} so they still resolve at runtime
 * (OldEnum types provide the same shimmed methods).
 *
 * <p>Conservative scope: only the {@code [Ljava/lang/Enum;} array
 * shape is touched. Single {@code Ljava/lang/Enum;} parameters are
 * unaffected — a plugin passing one real Enum still works because
 * the runtime carries the Enum subtype and the verifier accepts
 * subtype assignment for non-array references. Array contravariance
 * is the only verifier rule that bites.
 */
public final class LegacyEnumArrayTransformer {

    private static final String ENUM_ARR = "[Ljava/lang/Enum;";
    private static final String OBJ_ARR = "[Ljava/lang/Object;";
    private static final String ENUM_DESC = "Ljava/lang/Enum;";
    private static final String OBJ_DESC = "Ljava/lang/Object;";
    private static final String ENUM_INTERNAL = "java/lang/Enum";
    private static final String COMPAT = "com/github/martinambrus/rdforward/bridge/bukkit/compat/LegacyEnumCompat";

    private LegacyEnumArrayTransformer() {}

    /** Rewrite {@code original}. No-op for classes whose constant
     *  pool does not mention {@code Ljava/lang/Enum;} (which covers
     *  both scalar and array references) so the per-class cost on
     *  plugin jars is one constant-pool scan. */
    public static byte[] transform(byte[] original) {
        if (!constantPoolMentions(original, ENUM_DESC)) return original;
        ClassReader reader = new ClassReader(original);
        // COMPUTE_FRAMES is needed because changing parameter types
        // can shift local-variable typing inside rewritten methods.
        // The override on getCommonSuperClass tolerates plugin
        // classes that the system classloader can't see — without
        // it, ClassWriter would NoClassDefFoundError on every
        // plugin-private type when computing merge points.
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                try {
                    return super.getCommonSuperClass(type1, type2);
                } catch (Throwable ignored) {
                    return "java/lang/Object";
                }
            }
        };
        reader.accept(new RewriterAdapter(writer), 0);
        return writer.toByteArray();
    }

    /** Cheap pre-check: skip classes whose constant pool does not
     *  contain the {@code [Ljava/lang/Enum;} string. ASM has no
     *  built-in for this so we walk the raw cp. */
    private static boolean constantPoolMentions(byte[] bytes, String needle) {
        try {
            ClassReader cr = new ClassReader(bytes);
            int cpSize = cr.getItemCount();
            char[] buf = new char[cr.getMaxStringLength()];
            for (int i = 1; i < cpSize; i++) {
                int offset = cr.getItem(i);
                if (offset == 0) continue;
                int tag = bytes[offset - 1] & 0xff;
                // CONSTANT_Utf8 = 1; methodref / nameandtype etc reference utf8s,
                // so checking utf8 entries alone catches descriptor mentions.
                if (tag == 1) {
                    String s = cr.readUTF8(offset - 1, buf);
                    if (s != null && s.contains(needle)) return true;
                }
            }
        } catch (Throwable ignored) {
            // Defensive: if the cp scan blows up for any reason, fall
            // through to the full transform — correctness over speed.
            return true;
        }
        return false;
    }

    private static final class RewriterAdapter extends ClassVisitor {
        RewriterAdapter(ClassVisitor delegate) { super(Opcodes.ASM9, delegate); }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            String rewritten = rewriteDescriptor(descriptor);
            MethodVisitor mv = super.visitMethod(access, name, rewritten, signature, exceptions);
            return mv == null ? null : new MethodRewriter(mv);
        }
    }

    private static final class MethodRewriter extends MethodVisitor {
        MethodRewriter(MethodVisitor mv) { super(Opcodes.ASM9, mv); }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            // Replace java/lang/Enum.name() / .ordinal() virtual calls
            // with static helpers operating on Object — the receiver
            // is now Object after the array element type changes.
            if (opcode == Opcodes.INVOKEVIRTUAL && ENUM_INTERNAL.equals(owner)) {
                if ("name".equals(name) && "()Ljava/lang/String;".equals(descriptor)) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, COMPAT, "name",
                            "(Ljava/lang/Object;)Ljava/lang/String;", false);
                    return;
                }
                if ("ordinal".equals(name) && "()I".equals(descriptor)) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, COMPAT, "ordinal",
                            "(Ljava/lang/Object;)I", false);
                    return;
                }
            }
            super.visitMethodInsn(opcode, owner, name, rewriteDescriptor(descriptor), isInterface);
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bsm, Object... bsmArgs) {
            super.visitInvokeDynamicInsn(name, rewriteDescriptor(descriptor), bsm, bsmArgs);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            // ANEWARRAY uses the bare element type ("java/lang/Enum");
            // CHECKCAST / INSTANCEOF on an array uses the descriptor
            // form ("[Ljava/lang/Enum;"). Rewrite both shapes so the
            // verifier sees Object / Object[] uniformly. Scalar
            // CHECKCAST/INSTANCEOF on Enum becomes Object too — the
            // method may now produce Object on stack and the cast
            // would otherwise fail.
            if (opcode == Opcodes.ANEWARRAY && ENUM_INTERNAL.equals(type)) {
                super.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
                return;
            }
            if (opcode == Opcodes.CHECKCAST || opcode == Opcodes.INSTANCEOF) {
                if (ENUM_ARR.equals(type)) {
                    super.visitTypeInsn(opcode, OBJ_ARR);
                    return;
                }
                if (ENUM_INTERNAL.equals(type)) {
                    super.visitTypeInsn(opcode, "java/lang/Object");
                    return;
                }
            }
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            super.visitFieldInsn(opcode, owner, name, rewriteFieldDescriptor(descriptor));
        }

        @Override
        public void visitLocalVariable(String name, String descriptor, String signature,
                                       org.objectweb.asm.Label start, org.objectweb.asm.Label end, int index) {
            super.visitLocalVariable(name, rewriteFieldDescriptor(descriptor), signature, start, end, index);
        }
    }

    /** Rewrite a method descriptor: every {@code [Ljava/lang/Enum;}
     *  becomes {@code [Ljava/lang/Object;} and every scalar
     *  {@code Ljava/lang/Enum;} becomes {@code Ljava/lang/Object;}.
     *
     *  <p>Both must be rewritten together: a method declared
     *  {@code (Enum[]) -> Enum} pulls an element via {@code aaload}
     *  (now {@code Object[]} → produces {@code Object}) and tries to
     *  {@code areturn} as Enum — verifier rejects unless the return
     *  is also Object. Callers' methodref descriptors get the same
     *  rewrite so the link still resolves. */
    static String rewriteDescriptor(String descriptor) {
        if (descriptor == null || descriptor.indexOf(ENUM_DESC) < 0) return descriptor;
        Type method = Type.getMethodType(descriptor);
        Type[] params = method.getArgumentTypes();
        boolean changed = false;
        for (int i = 0; i < params.length; i++) {
            String d = params[i].getDescriptor();
            if (ENUM_ARR.equals(d)) { params[i] = Type.getType(OBJ_ARR); changed = true; }
            else if (ENUM_DESC.equals(d)) { params[i] = Type.getType(OBJ_DESC); changed = true; }
        }
        Type ret = method.getReturnType();
        String retDesc = ret.getDescriptor();
        if (ENUM_ARR.equals(retDesc)) { ret = Type.getType(OBJ_ARR); changed = true; }
        else if (ENUM_DESC.equals(retDesc)) { ret = Type.getType(OBJ_DESC); changed = true; }
        return changed ? Type.getMethodDescriptor(ret, params) : descriptor;
    }

    /** Same rewrite for a field / local-variable descriptor —
     *  collapses both array and scalar Enum references. */
    static String rewriteFieldDescriptor(String descriptor) {
        if (descriptor == null) return null;
        if (ENUM_ARR.equals(descriptor)) return OBJ_ARR;
        if (ENUM_DESC.equals(descriptor)) return OBJ_DESC;
        return descriptor;
    }
}

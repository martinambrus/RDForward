package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.compat.LegacyHealthTransformer;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the contract that {@link LegacyHealthTransformer} rewrites
 * legacy {@code int}-typed health call sites into INVOKESTATIC calls
 * to {@code HealthCompat}, so plugins compiled against pre-1.6 Bukkit
 * (Essentials 2.9.x's {@code PlayerExtension}) no longer
 * {@code NoSuchMethodError} on {@code /whois} (Player.getHealth():I)
 * or any other code path that touches int-typed health.
 */
class LegacyHealthTransformerTest {

    @Test
    void rewritesGetHealthCallSite() {
        byte[] cls = emitCallerClass("CallsGetHealthInt", "getHealth", "()I");
        List<MethodCall> calls = collectMethodCalls(LegacyHealthTransformer.transform(cls), "callIt");
        assertFalse(containsLegacyCall(calls, "org/bukkit/entity/Player", "getHealth", "()I"),
                "legacy INVOKEINTERFACE Player.getHealth()I must be rewritten");
        assertTrue(containsCompatCall(calls, "getHealth", "(Lorg/bukkit/entity/Damageable;)I"),
                "rewrite must target HealthCompat.getHealth(Damageable):int");
    }

    @Test
    void rewritesGetMaxHealthCallSite() {
        byte[] cls = emitCallerClass("CallsGetMaxHealthInt", "getMaxHealth", "()I");
        List<MethodCall> calls = collectMethodCalls(LegacyHealthTransformer.transform(cls), "callIt");
        assertFalse(containsLegacyCall(calls, "org/bukkit/entity/Player", "getMaxHealth", "()I"));
        assertTrue(containsCompatCall(calls, "getMaxHealth", "(Lorg/bukkit/entity/Damageable;)I"));
    }

    @Test
    void rewritesSetHealthCallSite() {
        byte[] cls = emitSetterCallerClass("CallsSetHealthInt", "setHealth", "(I)V");
        List<MethodCall> calls = collectMethodCalls(LegacyHealthTransformer.transform(cls), "callIt");
        assertFalse(containsLegacyCall(calls, "org/bukkit/entity/Player", "setHealth", "(I)V"));
        assertTrue(containsCompatCall(calls, "setHealth", "(Lorg/bukkit/entity/Damageable;I)V"));
    }

    @Test
    void rewritesSetMaxHealthCallSite() {
        byte[] cls = emitSetterCallerClass("CallsSetMaxHealthInt", "setMaxHealth", "(I)V");
        List<MethodCall> calls = collectMethodCalls(LegacyHealthTransformer.transform(cls), "callIt");
        assertFalse(containsLegacyCall(calls, "org/bukkit/entity/Player", "setMaxHealth", "(I)V"));
        assertTrue(containsCompatCall(calls, "setMaxHealth", "(Lorg/bukkit/entity/Damageable;I)V"));
    }

    @Test
    void leavesModernDoubleHealthCallsAlone() {
        byte[] cls = emitCallerClass("CallsGetHealthDouble", "getHealth", "()D");
        List<MethodCall> calls = collectMethodCalls(LegacyHealthTransformer.transform(cls), "callIt");
        assertTrue(containsLegacyCall(calls, "org/bukkit/entity/Player", "getHealth", "()D"),
                "modern double-typed call must be left untouched");
        assertFalse(containsCompatCall(calls, "getHealth", "(Lorg/bukkit/entity/Damageable;)I"));
    }

    @Test
    void leavesUnrelatedInvocationsAlone() {
        byte[] cls = emitCallerClass("CallsGetName", "getName", "()Ljava/lang/String;");
        List<MethodCall> calls = collectMethodCalls(LegacyHealthTransformer.transform(cls), "callIt");
        assertTrue(containsLegacyCall(calls, "org/bukkit/entity/Player", "getName", "()Ljava/lang/String;"),
                "unrelated Player call must remain INVOKEINTERFACE");
    }

    /** Build {@code class X { static R callIt(Player p) { return p.<name><desc>; } }}
     *  where {@code desc} is a getter shape ({@code ()T}). */
    private static byte[] emitCallerClass(String simpleName, String name, String descriptor) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, simpleName, null, "java/lang/Object", null);
        String returnType = descriptor.substring(descriptor.indexOf(')') + 1);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "callIt", "(Lorg/bukkit/entity/Player;)" + returnType, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/bukkit/entity/Player", name, descriptor, true);
        mv.visitInsn(switch (returnType) {
            case "I" -> Opcodes.IRETURN;
            case "D" -> Opcodes.DRETURN;
            default -> Opcodes.ARETURN;
        });
        mv.visitMaxs(2, 1);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    /** Build {@code class X { static void callIt(Player p, int v) { p.<name>(v); } }}. */
    private static byte[] emitSetterCallerClass(String simpleName, String name, String descriptor) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, simpleName, null, "java/lang/Object", null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "callIt", "(Lorg/bukkit/entity/Player;I)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/bukkit/entity/Player", name, descriptor, true);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    private record MethodCall(int opcode, String owner, String name, String descriptor) {}

    private static List<MethodCall> collectMethodCalls(byte[] cls, String inMethodNamed) {
        List<MethodCall> out = new ArrayList<>();
        new ClassReader(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int a, String n, String d, String s, String[] e) {
                if (!inMethodNamed.equals(n)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int op, String o, String mn, String md, boolean iface) {
                        out.add(new MethodCall(op, o, mn, md));
                    }
                };
            }
        }, 0);
        return out;
    }

    private static boolean containsLegacyCall(List<MethodCall> calls, String owner, String name, String desc) {
        for (MethodCall c : calls) {
            if (c.opcode == Opcodes.INVOKEINTERFACE
                    && c.owner.equals(owner)
                    && c.name.equals(name)
                    && c.descriptor.equals(desc)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsCompatCall(List<MethodCall> calls, String name, String desc) {
        for (MethodCall c : calls) {
            if (c.opcode == Opcodes.INVOKESTATIC
                    && c.owner.equals("com/github/martinambrus/rdforward/bridge/bukkit/compat/HealthCompat")
                    && c.name.equals(name)
                    && c.descriptor.equals(desc)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void shimRoundsHealthDoubleToInt() {
        // Sanity: HealthCompat round-trips via Math.round so a 17.6
        // double surfaces as 18 to legacy callers expecting int.
        org.bukkit.entity.Damageable d = (org.bukkit.entity.Damageable) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { org.bukkit.entity.Damageable.class },
                (proxy, method, args) -> {
                    if ("getHealth".equals(method.getName())) return 17.6;
                    return null;
                });
        assertEquals(18,
                com.github.martinambrus.rdforward.bridge.bukkit.compat.HealthCompat.getHealth(d));
    }
}

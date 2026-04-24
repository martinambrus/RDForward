package com.mojang.brigadier.arguments;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class FloatArgumentType implements com.mojang.brigadier.arguments.ArgumentType {
    public FloatArgumentType() {}
    public static com.mojang.brigadier.arguments.FloatArgumentType floatArg() {
        return null;
    }
    public static com.mojang.brigadier.arguments.FloatArgumentType floatArg(float arg0) {
        return null;
    }
    public static com.mojang.brigadier.arguments.FloatArgumentType floatArg(float arg0, float arg1) {
        return null;
    }
    public static float getFloat(com.mojang.brigadier.context.CommandContext arg0, java.lang.String arg1) {
        return 0.0f;
    }
    public float getMinimum() {
        return 0.0f;
    }
    public float getMaximum() {
        return 0.0f;
    }
    public java.lang.Float parse(com.mojang.brigadier.StringReader arg0) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.arguments.FloatArgumentType.parse(Lcom/mojang/brigadier/StringReader;)Ljava/lang/Float;");
        return null;
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.arguments.FloatArgumentType.equals(Ljava/lang/Object;)Z");
        return false;
    }
    public int hashCode() {
        return 0;
    }
    public java.lang.String toString() {
        return null;
    }
    public java.util.Collection getExamples() {
        return java.util.Collections.emptyList();
    }
}

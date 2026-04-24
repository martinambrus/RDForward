package com.mojang.brigadier.arguments;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class DoubleArgumentType implements com.mojang.brigadier.arguments.ArgumentType {
    public DoubleArgumentType() {}
    public static com.mojang.brigadier.arguments.DoubleArgumentType doubleArg() {
        return null;
    }
    public static com.mojang.brigadier.arguments.DoubleArgumentType doubleArg(double arg0) {
        return null;
    }
    public static com.mojang.brigadier.arguments.DoubleArgumentType doubleArg(double arg0, double arg1) {
        return null;
    }
    public static double getDouble(com.mojang.brigadier.context.CommandContext arg0, java.lang.String arg1) {
        return 0.0;
    }
    public double getMinimum() {
        return 0.0;
    }
    public double getMaximum() {
        return 0.0;
    }
    public java.lang.Double parse(com.mojang.brigadier.StringReader arg0) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.arguments.DoubleArgumentType.parse(Lcom/mojang/brigadier/StringReader;)Ljava/lang/Double;");
        return null;
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.arguments.DoubleArgumentType.equals(Ljava/lang/Object;)Z");
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

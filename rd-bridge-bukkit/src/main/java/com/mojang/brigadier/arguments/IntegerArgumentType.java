package com.mojang.brigadier.arguments;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class IntegerArgumentType implements com.mojang.brigadier.arguments.ArgumentType {
    public IntegerArgumentType() {}
    public static com.mojang.brigadier.arguments.IntegerArgumentType integer() {
        return null;
    }
    public static com.mojang.brigadier.arguments.IntegerArgumentType integer(int arg0) {
        return null;
    }
    public static com.mojang.brigadier.arguments.IntegerArgumentType integer(int arg0, int arg1) {
        return null;
    }
    public static int getInteger(com.mojang.brigadier.context.CommandContext arg0, java.lang.String arg1) {
        return 0;
    }
    public int getMinimum() {
        return 0;
    }
    public int getMaximum() {
        return 0;
    }
    public java.lang.Integer parse(com.mojang.brigadier.StringReader arg0) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.arguments.IntegerArgumentType.parse(Lcom/mojang/brigadier/StringReader;)Ljava/lang/Integer;");
        return null;
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.arguments.IntegerArgumentType.equals(Ljava/lang/Object;)Z");
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

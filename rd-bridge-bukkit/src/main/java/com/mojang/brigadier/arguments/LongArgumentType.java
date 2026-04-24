package com.mojang.brigadier.arguments;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class LongArgumentType implements com.mojang.brigadier.arguments.ArgumentType {
    public LongArgumentType() {}
    public static com.mojang.brigadier.arguments.LongArgumentType longArg() {
        return null;
    }
    public static com.mojang.brigadier.arguments.LongArgumentType longArg(long arg0) {
        return null;
    }
    public static com.mojang.brigadier.arguments.LongArgumentType longArg(long arg0, long arg1) {
        return null;
    }
    public static long getLong(com.mojang.brigadier.context.CommandContext arg0, java.lang.String arg1) {
        return 0L;
    }
    public long getMinimum() {
        return 0L;
    }
    public long getMaximum() {
        return 0L;
    }
    public java.lang.Long parse(com.mojang.brigadier.StringReader arg0) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.arguments.LongArgumentType.parse(Lcom/mojang/brigadier/StringReader;)Ljava/lang/Long;");
        return null;
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.arguments.LongArgumentType.equals(Ljava/lang/Object;)Z");
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

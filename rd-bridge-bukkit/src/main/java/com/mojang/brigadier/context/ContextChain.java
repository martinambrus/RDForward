package com.mojang.brigadier.context;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ContextChain {
    public ContextChain(java.util.List arg0, com.mojang.brigadier.context.CommandContext arg1) {}
    public ContextChain() {}
    public static java.util.Optional tryFlatten(com.mojang.brigadier.context.CommandContext arg0) {
        return java.util.Optional.empty();
    }
    public static java.util.Collection runModifier(com.mojang.brigadier.context.CommandContext arg0, java.lang.Object arg1, com.mojang.brigadier.ResultConsumer arg2, boolean arg3) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return java.util.Collections.emptyList();
    }
    public static int runExecutable(com.mojang.brigadier.context.CommandContext arg0, java.lang.Object arg1, com.mojang.brigadier.ResultConsumer arg2, boolean arg3) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return 0;
    }
    public int executeAll(java.lang.Object arg0, com.mojang.brigadier.ResultConsumer arg1) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.context.ContextChain.executeAll(Ljava/lang/Object;Lcom/mojang/brigadier/ResultConsumer;)I");
        return 0;
    }
    public com.mojang.brigadier.context.ContextChain$Stage getStage() {
        return null;
    }
    public com.mojang.brigadier.context.CommandContext getTopContext() {
        return null;
    }
    public com.mojang.brigadier.context.ContextChain nextStage() {
        return null;
    }
}

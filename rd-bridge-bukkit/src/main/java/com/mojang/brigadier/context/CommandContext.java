package com.mojang.brigadier.context;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class CommandContext {
    public CommandContext(java.lang.Object arg0, java.lang.String arg1, java.util.Map arg2, com.mojang.brigadier.Command arg3, com.mojang.brigadier.tree.CommandNode arg4, java.util.List arg5, com.mojang.brigadier.context.StringRange arg6, com.mojang.brigadier.context.CommandContext arg7, com.mojang.brigadier.RedirectModifier arg8, boolean arg9) {}
    public CommandContext() {}
    public com.mojang.brigadier.context.CommandContext copyFor(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.context.CommandContext.copyFor(Ljava/lang/Object;)Lcom/mojang/brigadier/context/CommandContext;");
        return this;
    }
    public com.mojang.brigadier.context.CommandContext getChild() {
        return null;
    }
    public com.mojang.brigadier.context.CommandContext getLastChild() {
        return null;
    }
    public com.mojang.brigadier.Command getCommand() {
        return null;
    }
    public java.lang.Object getSource() {
        return null;
    }
    public java.lang.Object getArgument(java.lang.String arg0, java.lang.Class arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.context.CommandContext.getArgument(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;");
        return null;
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.context.CommandContext.equals(Ljava/lang/Object;)Z");
        return false;
    }
    public int hashCode() {
        return 0;
    }
    public com.mojang.brigadier.RedirectModifier getRedirectModifier() {
        return null;
    }
    public com.mojang.brigadier.context.StringRange getRange() {
        return null;
    }
    public java.lang.String getInput() {
        return null;
    }
    public com.mojang.brigadier.tree.CommandNode getRootNode() {
        return null;
    }
    public java.util.List getNodes() {
        return java.util.Collections.emptyList();
    }
    public boolean hasNodes() {
        return false;
    }
    public boolean isForked() {
        return false;
    }
}

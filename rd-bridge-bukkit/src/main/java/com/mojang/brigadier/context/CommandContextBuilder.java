package com.mojang.brigadier.context;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class CommandContextBuilder {
    public CommandContextBuilder(com.mojang.brigadier.CommandDispatcher arg0, java.lang.Object arg1, com.mojang.brigadier.tree.CommandNode arg2, int arg3) {}
    public CommandContextBuilder() {}
    public com.mojang.brigadier.context.CommandContextBuilder withSource(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.context.CommandContextBuilder.withSource(Ljava/lang/Object;)Lcom/mojang/brigadier/context/CommandContextBuilder;");
        return this;
    }
    public java.lang.Object getSource() {
        return null;
    }
    public com.mojang.brigadier.tree.CommandNode getRootNode() {
        return null;
    }
    public com.mojang.brigadier.context.CommandContextBuilder withArgument(java.lang.String arg0, com.mojang.brigadier.context.ParsedArgument arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.context.CommandContextBuilder.withArgument(Ljava/lang/String;Lcom/mojang/brigadier/context/ParsedArgument;)Lcom/mojang/brigadier/context/CommandContextBuilder;");
        return this;
    }
    public java.util.Map getArguments() {
        return java.util.Collections.emptyMap();
    }
    public com.mojang.brigadier.context.CommandContextBuilder withCommand(com.mojang.brigadier.Command arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.context.CommandContextBuilder.withCommand(Lcom/mojang/brigadier/Command;)Lcom/mojang/brigadier/context/CommandContextBuilder;");
        return this;
    }
    public com.mojang.brigadier.context.CommandContextBuilder withNode(com.mojang.brigadier.tree.CommandNode arg0, com.mojang.brigadier.context.StringRange arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.context.CommandContextBuilder.withNode(Lcom/mojang/brigadier/tree/CommandNode;Lcom/mojang/brigadier/context/StringRange;)Lcom/mojang/brigadier/context/CommandContextBuilder;");
        return this;
    }
    public com.mojang.brigadier.context.CommandContextBuilder copy() {
        return null;
    }
    public com.mojang.brigadier.context.CommandContextBuilder withChild(com.mojang.brigadier.context.CommandContextBuilder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.context.CommandContextBuilder.withChild(Lcom/mojang/brigadier/context/CommandContextBuilder;)Lcom/mojang/brigadier/context/CommandContextBuilder;");
        return this;
    }
    public com.mojang.brigadier.context.CommandContextBuilder getChild() {
        return null;
    }
    public com.mojang.brigadier.context.CommandContextBuilder getLastChild() {
        return null;
    }
    public com.mojang.brigadier.Command getCommand() {
        return null;
    }
    public java.util.List getNodes() {
        return java.util.Collections.emptyList();
    }
    public com.mojang.brigadier.context.CommandContext build(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.context.CommandContextBuilder.build(Ljava/lang/String;)Lcom/mojang/brigadier/context/CommandContext;");
        return null;
    }
    public com.mojang.brigadier.CommandDispatcher getDispatcher() {
        return null;
    }
    public com.mojang.brigadier.context.StringRange getRange() {
        return null;
    }
    public com.mojang.brigadier.context.SuggestionContext findSuggestionContext(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.context.CommandContextBuilder.findSuggestionContext(I)Lcom/mojang/brigadier/context/SuggestionContext;");
        return null;
    }
}

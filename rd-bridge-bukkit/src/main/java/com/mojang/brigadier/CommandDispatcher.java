package com.mojang.brigadier;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class CommandDispatcher {
    public static final java.lang.String ARGUMENT_SEPARATOR = " ";
    public static final char ARGUMENT_SEPARATOR_CHAR = (char) 32;
    public CommandDispatcher(com.mojang.brigadier.tree.RootCommandNode arg0) {}
    public CommandDispatcher() {}
    public com.mojang.brigadier.tree.LiteralCommandNode register(com.mojang.brigadier.builder.LiteralArgumentBuilder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.register(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;)Lcom/mojang/brigadier/tree/LiteralCommandNode;");
        return null;
    }
    public void setConsumer(com.mojang.brigadier.ResultConsumer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V");
    }
    public int execute(java.lang.String arg0, java.lang.Object arg1) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.execute(Ljava/lang/String;Ljava/lang/Object;)I");
        return 0;
    }
    public int execute(com.mojang.brigadier.StringReader arg0, java.lang.Object arg1) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.execute(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)I");
        return 0;
    }
    public int execute(com.mojang.brigadier.ParseResults arg0) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.execute(Lcom/mojang/brigadier/ParseResults;)I");
        return 0;
    }
    public com.mojang.brigadier.ParseResults parse(java.lang.String arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.parse(Ljava/lang/String;Ljava/lang/Object;)Lcom/mojang/brigadier/ParseResults;");
        return null;
    }
    public com.mojang.brigadier.ParseResults parse(com.mojang.brigadier.StringReader arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.parse(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)Lcom/mojang/brigadier/ParseResults;");
        return null;
    }
    public java.lang.String[] getAllUsage(com.mojang.brigadier.tree.CommandNode arg0, java.lang.Object arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.getAllUsage(Lcom/mojang/brigadier/tree/CommandNode;Ljava/lang/Object;Z)[Ljava/lang/String;");
        return new java.lang.String[0];
    }
    public java.util.Map getSmartUsage(com.mojang.brigadier.tree.CommandNode arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.getSmartUsage(Lcom/mojang/brigadier/tree/CommandNode;Ljava/lang/Object;)Ljava/util/Map;");
        return java.util.Collections.emptyMap();
    }
    public java.util.concurrent.CompletableFuture getCompletionSuggestions(com.mojang.brigadier.ParseResults arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;)Ljava/util/concurrent/CompletableFuture;");
        return null;
    }
    public java.util.concurrent.CompletableFuture getCompletionSuggestions(com.mojang.brigadier.ParseResults arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;I)Ljava/util/concurrent/CompletableFuture;");
        return null;
    }
    public com.mojang.brigadier.tree.RootCommandNode getRoot() {
        return null;
    }
    public java.util.Collection getPath(com.mojang.brigadier.tree.CommandNode arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.getPath(Lcom/mojang/brigadier/tree/CommandNode;)Ljava/util/Collection;");
        return java.util.Collections.emptyList();
    }
    public com.mojang.brigadier.tree.CommandNode findNode(java.util.Collection arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.findNode(Ljava/util/Collection;)Lcom/mojang/brigadier/tree/CommandNode;");
        return null;
    }
    public void findAmbiguities(com.mojang.brigadier.AmbiguityConsumer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.CommandDispatcher.findAmbiguities(Lcom/mojang/brigadier/AmbiguityConsumer;)V");
    }
}

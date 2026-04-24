package com.mojang.brigadier.tree;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class RootCommandNode extends com.mojang.brigadier.tree.CommandNode {
    public RootCommandNode() { super((com.mojang.brigadier.Command) null, (java.util.function.Predicate) null, (com.mojang.brigadier.tree.CommandNode) null, (com.mojang.brigadier.RedirectModifier) null, false); }
    public java.lang.String getName() {
        return null;
    }
    public java.lang.String getUsageText() {
        return null;
    }
    public void parse(com.mojang.brigadier.StringReader arg0, com.mojang.brigadier.context.CommandContextBuilder arg1) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.RootCommandNode.parse(Lcom/mojang/brigadier/StringReader;Lcom/mojang/brigadier/context/CommandContextBuilder;)V");
    }
    public java.util.concurrent.CompletableFuture listSuggestions(com.mojang.brigadier.context.CommandContext arg0, com.mojang.brigadier.suggestion.SuggestionsBuilder arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.RootCommandNode.listSuggestions(Lcom/mojang/brigadier/context/CommandContext;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;");
        return null;
    }
    public boolean isValidInput(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.RootCommandNode.isValidInput(Ljava/lang/String;)Z");
        return false;
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.RootCommandNode.equals(Ljava/lang/Object;)Z");
        return false;
    }
    public com.mojang.brigadier.builder.ArgumentBuilder createBuilder() {
        return null;
    }
    protected java.lang.String getSortedKey() {
        return null;
    }
    public java.util.Collection getExamples() {
        return java.util.Collections.emptyList();
    }
    public java.lang.String toString() {
        return null;
    }
}

package com.mojang.brigadier.tree;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ArgumentCommandNode extends com.mojang.brigadier.tree.CommandNode {
    public ArgumentCommandNode(java.lang.String arg0, com.mojang.brigadier.arguments.ArgumentType arg1, com.mojang.brigadier.Command arg2, java.util.function.Predicate arg3, com.mojang.brigadier.tree.CommandNode arg4, com.mojang.brigadier.RedirectModifier arg5, boolean arg6, com.mojang.brigadier.suggestion.SuggestionProvider arg7) { super((com.mojang.brigadier.Command) null, (java.util.function.Predicate) null, (com.mojang.brigadier.tree.CommandNode) null, (com.mojang.brigadier.RedirectModifier) null, false); }
    public ArgumentCommandNode() { super((com.mojang.brigadier.Command) null, (java.util.function.Predicate) null, (com.mojang.brigadier.tree.CommandNode) null, (com.mojang.brigadier.RedirectModifier) null, false); }
    public com.mojang.brigadier.arguments.ArgumentType getType() {
        return null;
    }
    public java.lang.String getName() {
        return null;
    }
    public java.lang.String getUsageText() {
        return null;
    }
    public com.mojang.brigadier.suggestion.SuggestionProvider getCustomSuggestions() {
        return null;
    }
    public void parse(com.mojang.brigadier.StringReader arg0, com.mojang.brigadier.context.CommandContextBuilder arg1) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.ArgumentCommandNode.parse(Lcom/mojang/brigadier/StringReader;Lcom/mojang/brigadier/context/CommandContextBuilder;)V");
    }
    public java.util.concurrent.CompletableFuture listSuggestions(com.mojang.brigadier.context.CommandContext arg0, com.mojang.brigadier.suggestion.SuggestionsBuilder arg1) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.ArgumentCommandNode.listSuggestions(Lcom/mojang/brigadier/context/CommandContext;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;");
        return null;
    }
    public com.mojang.brigadier.builder.RequiredArgumentBuilder createBuilder() {
        return null;
    }
    public boolean isValidInput(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.ArgumentCommandNode.isValidInput(Ljava/lang/String;)Z");
        return false;
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.ArgumentCommandNode.equals(Ljava/lang/Object;)Z");
        return false;
    }
    public int hashCode() {
        return 0;
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

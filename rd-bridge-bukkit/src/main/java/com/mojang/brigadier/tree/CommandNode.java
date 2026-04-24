package com.mojang.brigadier.tree;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class CommandNode implements java.lang.Comparable {
    protected CommandNode(com.mojang.brigadier.Command arg0, java.util.function.Predicate arg1, com.mojang.brigadier.tree.CommandNode arg2, com.mojang.brigadier.RedirectModifier arg3, boolean arg4) {}
    protected CommandNode() {}
    public com.mojang.brigadier.Command getCommand() {
        return null;
    }
    public java.util.Collection getChildren() {
        return java.util.Collections.emptyList();
    }
    public com.mojang.brigadier.tree.CommandNode getChild(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.CommandNode.getChild(Ljava/lang/String;)Lcom/mojang/brigadier/tree/CommandNode;");
        return this;
    }
    public com.mojang.brigadier.tree.CommandNode getRedirect() {
        return null;
    }
    public com.mojang.brigadier.RedirectModifier getRedirectModifier() {
        return null;
    }
    public boolean canUse(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.CommandNode.canUse(Ljava/lang/Object;)Z");
        return false;
    }
    public void addChild(com.mojang.brigadier.tree.CommandNode arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.CommandNode.addChild(Lcom/mojang/brigadier/tree/CommandNode;)V");
    }
    public void findAmbiguities(com.mojang.brigadier.AmbiguityConsumer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.CommandNode.findAmbiguities(Lcom/mojang/brigadier/AmbiguityConsumer;)V");
    }
    protected abstract boolean isValidInput(java.lang.String arg0);
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.CommandNode.equals(Ljava/lang/Object;)Z");
        return false;
    }
    public int hashCode() {
        return 0;
    }
    public java.util.function.Predicate getRequirement() {
        return null;
    }
    public abstract java.lang.String getName();
    public abstract java.lang.String getUsageText();
    public abstract void parse(com.mojang.brigadier.StringReader arg0, com.mojang.brigadier.context.CommandContextBuilder arg1) throws com.mojang.brigadier.exceptions.CommandSyntaxException;
    public abstract java.util.concurrent.CompletableFuture listSuggestions(com.mojang.brigadier.context.CommandContext arg0, com.mojang.brigadier.suggestion.SuggestionsBuilder arg1) throws com.mojang.brigadier.exceptions.CommandSyntaxException;
    public abstract com.mojang.brigadier.builder.ArgumentBuilder createBuilder();
    protected abstract java.lang.String getSortedKey();
    public java.util.Collection getRelevantNodes(com.mojang.brigadier.StringReader arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.CommandNode.getRelevantNodes(Lcom/mojang/brigadier/StringReader;)Ljava/util/Collection;");
        return java.util.Collections.emptyList();
    }
    public int compareTo(com.mojang.brigadier.tree.CommandNode arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.CommandNode.compareTo(Lcom/mojang/brigadier/tree/CommandNode;)I");
        return 0;
    }
    public boolean isFork() {
        return false;
    }
    public abstract java.util.Collection getExamples();
    public int compareTo(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.tree.CommandNode.compareTo(Ljava/lang/Object;)I");
        return 0;
    }
}

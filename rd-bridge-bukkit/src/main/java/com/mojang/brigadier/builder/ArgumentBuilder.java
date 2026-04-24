package com.mojang.brigadier.builder;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class ArgumentBuilder {
    public ArgumentBuilder() {}
    protected abstract com.mojang.brigadier.builder.ArgumentBuilder getThis();
    public com.mojang.brigadier.builder.ArgumentBuilder then(com.mojang.brigadier.builder.ArgumentBuilder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.builder.ArgumentBuilder.then(Lcom/mojang/brigadier/builder/ArgumentBuilder;)Lcom/mojang/brigadier/builder/ArgumentBuilder;");
        return this;
    }
    public com.mojang.brigadier.builder.ArgumentBuilder then(com.mojang.brigadier.tree.CommandNode arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.builder.ArgumentBuilder.then(Lcom/mojang/brigadier/tree/CommandNode;)Lcom/mojang/brigadier/builder/ArgumentBuilder;");
        return this;
    }
    public java.util.Collection getArguments() {
        return java.util.Collections.emptyList();
    }
    public com.mojang.brigadier.builder.ArgumentBuilder executes(com.mojang.brigadier.Command arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.builder.ArgumentBuilder.executes(Lcom/mojang/brigadier/Command;)Lcom/mojang/brigadier/builder/ArgumentBuilder;");
        return this;
    }
    public com.mojang.brigadier.Command getCommand() {
        return null;
    }
    public com.mojang.brigadier.builder.ArgumentBuilder requires(java.util.function.Predicate arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.builder.ArgumentBuilder.requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;");
        return this;
    }
    public java.util.function.Predicate getRequirement() {
        return null;
    }
    public com.mojang.brigadier.builder.ArgumentBuilder redirect(com.mojang.brigadier.tree.CommandNode arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.builder.ArgumentBuilder.redirect(Lcom/mojang/brigadier/tree/CommandNode;)Lcom/mojang/brigadier/builder/ArgumentBuilder;");
        return this;
    }
    public com.mojang.brigadier.builder.ArgumentBuilder redirect(com.mojang.brigadier.tree.CommandNode arg0, com.mojang.brigadier.SingleRedirectModifier arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.builder.ArgumentBuilder.redirect(Lcom/mojang/brigadier/tree/CommandNode;Lcom/mojang/brigadier/SingleRedirectModifier;)Lcom/mojang/brigadier/builder/ArgumentBuilder;");
        return this;
    }
    public com.mojang.brigadier.builder.ArgumentBuilder fork(com.mojang.brigadier.tree.CommandNode arg0, com.mojang.brigadier.RedirectModifier arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.builder.ArgumentBuilder.fork(Lcom/mojang/brigadier/tree/CommandNode;Lcom/mojang/brigadier/RedirectModifier;)Lcom/mojang/brigadier/builder/ArgumentBuilder;");
        return this;
    }
    public com.mojang.brigadier.builder.ArgumentBuilder forward(com.mojang.brigadier.tree.CommandNode arg0, com.mojang.brigadier.RedirectModifier arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.builder.ArgumentBuilder.forward(Lcom/mojang/brigadier/tree/CommandNode;Lcom/mojang/brigadier/RedirectModifier;Z)Lcom/mojang/brigadier/builder/ArgumentBuilder;");
        return this;
    }
    public com.mojang.brigadier.tree.CommandNode getRedirect() {
        return null;
    }
    public com.mojang.brigadier.RedirectModifier getRedirectModifier() {
        return null;
    }
    public boolean isFork() {
        return false;
    }
    public abstract com.mojang.brigadier.tree.CommandNode build();
}

package io.papermc.paper.command.brigadier;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Commands extends io.papermc.paper.plugin.lifecycle.event.registrar.Registrar {
    static com.mojang.brigadier.builder.LiteralArgumentBuilder literal(java.lang.String arg0) {
        return null;
    }
    static com.mojang.brigadier.builder.RequiredArgumentBuilder argument(java.lang.String arg0, com.mojang.brigadier.arguments.ArgumentType arg1) {
        return null;
    }
    static java.util.function.Predicate restricted(java.util.function.Predicate arg0) {
        return null;
    }
    com.mojang.brigadier.CommandDispatcher getDispatcher();
    default java.util.Set register(com.mojang.brigadier.tree.LiteralCommandNode arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.command.brigadier.Commands.register(Lcom/mojang/brigadier/tree/LiteralCommandNode;)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    default java.util.Set register(com.mojang.brigadier.tree.LiteralCommandNode arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.command.brigadier.Commands.register(Lcom/mojang/brigadier/tree/LiteralCommandNode;Ljava/lang/String;)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    default java.util.Set register(com.mojang.brigadier.tree.LiteralCommandNode arg0, java.util.Collection arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.command.brigadier.Commands.register(Lcom/mojang/brigadier/tree/LiteralCommandNode;Ljava/util/Collection;)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    java.util.Set register(com.mojang.brigadier.tree.LiteralCommandNode arg0, java.lang.String arg1, java.util.Collection arg2);
    java.util.Set register(io.papermc.paper.plugin.configuration.PluginMeta arg0, com.mojang.brigadier.tree.LiteralCommandNode arg1, java.lang.String arg2, java.util.Collection arg3);
    java.util.Set registerWithFlags(io.papermc.paper.plugin.configuration.PluginMeta arg0, com.mojang.brigadier.tree.LiteralCommandNode arg1, java.lang.String arg2, java.util.Collection arg3, java.util.Set arg4);
    default java.util.Set register(java.lang.String arg0, io.papermc.paper.command.brigadier.BasicCommand arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.command.brigadier.Commands.register(Ljava/lang/String;Lio/papermc/paper/command/brigadier/BasicCommand;)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    default java.util.Set register(java.lang.String arg0, java.lang.String arg1, io.papermc.paper.command.brigadier.BasicCommand arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.command.brigadier.Commands.register(Ljava/lang/String;Ljava/lang/String;Lio/papermc/paper/command/brigadier/BasicCommand;)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    default java.util.Set register(java.lang.String arg0, java.util.Collection arg1, io.papermc.paper.command.brigadier.BasicCommand arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.command.brigadier.Commands.register(Ljava/lang/String;Ljava/util/Collection;Lio/papermc/paper/command/brigadier/BasicCommand;)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    java.util.Set register(java.lang.String arg0, java.lang.String arg1, java.util.Collection arg2, io.papermc.paper.command.brigadier.BasicCommand arg3);
    java.util.Set register(io.papermc.paper.plugin.configuration.PluginMeta arg0, java.lang.String arg1, java.lang.String arg2, java.util.Collection arg3, io.papermc.paper.command.brigadier.BasicCommand arg4);
}

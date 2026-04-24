package io.papermc.paper.command.brigadier.argument;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CustomArgumentType extends com.mojang.brigadier.arguments.ArgumentType {
    java.lang.Object parse(com.mojang.brigadier.StringReader arg0) throws com.mojang.brigadier.exceptions.CommandSyntaxException;
    default java.lang.Object parse(com.mojang.brigadier.StringReader arg0, java.lang.Object arg1) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.command.brigadier.argument.CustomArgumentType.parse(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)Ljava/lang/Object;");
        return null;
    }
    com.mojang.brigadier.arguments.ArgumentType getNativeType();
    default java.util.Collection getExamples() {
        return java.util.Collections.emptyList();
    }
    default java.util.concurrent.CompletableFuture listSuggestions(com.mojang.brigadier.context.CommandContext arg0, com.mojang.brigadier.suggestion.SuggestionsBuilder arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.command.brigadier.argument.CustomArgumentType.listSuggestions(Lcom/mojang/brigadier/context/CommandContext;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;");
        return null;
    }
}

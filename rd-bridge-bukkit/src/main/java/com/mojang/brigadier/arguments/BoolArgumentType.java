package com.mojang.brigadier.arguments;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BoolArgumentType implements com.mojang.brigadier.arguments.ArgumentType {
    public BoolArgumentType() {}
    public static com.mojang.brigadier.arguments.BoolArgumentType bool() {
        return null;
    }
    public static boolean getBool(com.mojang.brigadier.context.CommandContext arg0, java.lang.String arg1) {
        return false;
    }
    public java.lang.Boolean parse(com.mojang.brigadier.StringReader arg0) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.arguments.BoolArgumentType.parse(Lcom/mojang/brigadier/StringReader;)Ljava/lang/Boolean;");
        return null;
    }
    public java.util.concurrent.CompletableFuture listSuggestions(com.mojang.brigadier.context.CommandContext arg0, com.mojang.brigadier.suggestion.SuggestionsBuilder arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.arguments.BoolArgumentType.listSuggestions(Lcom/mojang/brigadier/context/CommandContext;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;");
        return null;
    }
    public java.util.Collection getExamples() {
        return java.util.Collections.emptyList();
    }
}

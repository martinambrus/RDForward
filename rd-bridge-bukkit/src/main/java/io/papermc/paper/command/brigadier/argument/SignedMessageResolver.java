package io.papermc.paper.command.brigadier.argument;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SignedMessageResolver {
    java.lang.String content();
    java.util.concurrent.CompletableFuture resolveSignedMessage(java.lang.String arg0, com.mojang.brigadier.context.CommandContext arg1) throws com.mojang.brigadier.exceptions.CommandSyntaxException;
}

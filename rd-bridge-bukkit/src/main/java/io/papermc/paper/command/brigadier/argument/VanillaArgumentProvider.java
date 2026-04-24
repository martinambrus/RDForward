package io.papermc.paper.command.brigadier.argument;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
interface VanillaArgumentProvider {
    public static final java.util.Optional PROVIDER = java.util.Optional.empty();
    static io.papermc.paper.command.brigadier.argument.VanillaArgumentProvider provider() {
        return null;
    }
    com.mojang.brigadier.arguments.ArgumentType entity();
    com.mojang.brigadier.arguments.ArgumentType player();
    com.mojang.brigadier.arguments.ArgumentType entities();
    com.mojang.brigadier.arguments.ArgumentType players();
    com.mojang.brigadier.arguments.ArgumentType playerProfiles();
    com.mojang.brigadier.arguments.ArgumentType blockPosition();
    com.mojang.brigadier.arguments.ArgumentType columnBlockPosition();
    com.mojang.brigadier.arguments.ArgumentType finePosition(boolean arg0);
    com.mojang.brigadier.arguments.ArgumentType columnFinePosition(boolean arg0);
    com.mojang.brigadier.arguments.ArgumentType rotation();
    com.mojang.brigadier.arguments.ArgumentType angle();
    com.mojang.brigadier.arguments.ArgumentType axes();
    com.mojang.brigadier.arguments.ArgumentType blockState();
    com.mojang.brigadier.arguments.ArgumentType blockInWorldPredicate();
    com.mojang.brigadier.arguments.ArgumentType itemStack();
    com.mojang.brigadier.arguments.ArgumentType itemStackPredicate();
    com.mojang.brigadier.arguments.ArgumentType namedColor();
    com.mojang.brigadier.arguments.ArgumentType hexColor();
    com.mojang.brigadier.arguments.ArgumentType component();
    com.mojang.brigadier.arguments.ArgumentType style();
    com.mojang.brigadier.arguments.ArgumentType signedMessage();
    com.mojang.brigadier.arguments.ArgumentType scoreboardDisplaySlot();
    com.mojang.brigadier.arguments.ArgumentType namespacedKey();
    com.mojang.brigadier.arguments.ArgumentType key();
    com.mojang.brigadier.arguments.ArgumentType integerRange();
    com.mojang.brigadier.arguments.ArgumentType doubleRange();
    com.mojang.brigadier.arguments.ArgumentType world();
    com.mojang.brigadier.arguments.ArgumentType gameMode();
    com.mojang.brigadier.arguments.ArgumentType heightMap();
    com.mojang.brigadier.arguments.ArgumentType uuid();
    com.mojang.brigadier.arguments.ArgumentType objectiveCriteria();
    com.mojang.brigadier.arguments.ArgumentType entityAnchor();
    com.mojang.brigadier.arguments.ArgumentType time(int arg0);
    com.mojang.brigadier.arguments.ArgumentType templateMirror();
    com.mojang.brigadier.arguments.ArgumentType templateRotation();
    com.mojang.brigadier.arguments.ArgumentType resourceKey(io.papermc.paper.registry.RegistryKey arg0);
    com.mojang.brigadier.arguments.ArgumentType resource(io.papermc.paper.registry.RegistryKey arg0);
}

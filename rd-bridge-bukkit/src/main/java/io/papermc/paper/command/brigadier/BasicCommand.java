package io.papermc.paper.command.brigadier;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BasicCommand {
    void execute(io.papermc.paper.command.brigadier.CommandSourceStack arg0, java.lang.String[] arg1);
    default java.util.Collection suggest(io.papermc.paper.command.brigadier.CommandSourceStack arg0, java.lang.String[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.command.brigadier.BasicCommand.suggest(Lio/papermc/paper/command/brigadier/CommandSourceStack;[Ljava/lang/String;)Ljava/util/Collection;");
        return java.util.Collections.emptyList();
    }
    default boolean canUse(org.bukkit.command.CommandSender arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.command.brigadier.BasicCommand.canUse(Lorg/bukkit/command/CommandSender;)Z");
        return false;
    }
    default java.lang.String permission() {
        return null;
    }
}

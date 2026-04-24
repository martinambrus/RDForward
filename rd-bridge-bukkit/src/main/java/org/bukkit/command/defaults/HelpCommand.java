package org.bukkit.command.defaults;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class HelpCommand extends org.bukkit.command.defaults.BukkitCommand {
    public HelpCommand() { super((java.lang.String) null); }
    public boolean execute(org.bukkit.command.CommandSender arg0, java.lang.String arg1, java.lang.String[] arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.defaults.HelpCommand.execute(Lorg/bukkit/command/CommandSender;Ljava/lang/String;[Ljava/lang/String;)Z");
        return false;
    }
    public java.util.List tabComplete(org.bukkit.command.CommandSender arg0, java.lang.String arg1, java.lang.String[] arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.defaults.HelpCommand.tabComplete(Lorg/bukkit/command/CommandSender;Ljava/lang/String;[Ljava/lang/String;)Ljava/util/List;");
        return java.util.Collections.emptyList();
    }
    protected org.bukkit.help.HelpTopic findPossibleMatches(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.defaults.HelpCommand.findPossibleMatches(Ljava/lang/String;)Lorg/bukkit/help/HelpTopic;");
        return null;
    }
    protected static int damerauLevenshteinDistance(java.lang.String arg0, java.lang.String arg1) {
        return 0;
    }
}

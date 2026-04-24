package org.bukkit.command;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class SimpleCommandMap implements org.bukkit.command.CommandMap {
    public SimpleCommandMap(org.bukkit.Server arg0, java.util.Map arg1) {}
    public SimpleCommandMap() {}
    public void setFallbackCommands() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.SimpleCommandMap.setFallbackCommands()V");
    }
    public void registerAll(java.lang.String arg0, java.util.List arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.SimpleCommandMap.registerAll(Ljava/lang/String;Ljava/util/List;)V");
    }
    public boolean register(java.lang.String arg0, org.bukkit.command.Command arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.SimpleCommandMap.register(Ljava/lang/String;Lorg/bukkit/command/Command;)Z");
        return false;
    }
    public boolean register(java.lang.String arg0, java.lang.String arg1, org.bukkit.command.Command arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.SimpleCommandMap.register(Ljava/lang/String;Ljava/lang/String;Lorg/bukkit/command/Command;)Z");
        return false;
    }
    public boolean dispatch(org.bukkit.command.CommandSender arg0, java.lang.String arg1) throws org.bukkit.command.CommandException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.SimpleCommandMap.dispatch(Lorg/bukkit/command/CommandSender;Ljava/lang/String;)Z");
        return false;
    }
    public void clearCommands() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.SimpleCommandMap.clearCommands()V");
    }
    public org.bukkit.command.Command getCommand(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.SimpleCommandMap.getCommand(Ljava/lang/String;)Lorg/bukkit/command/Command;");
        return null;
    }
    public java.util.List tabComplete(org.bukkit.command.CommandSender arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.SimpleCommandMap.tabComplete(Lorg/bukkit/command/CommandSender;Ljava/lang/String;)Ljava/util/List;");
        return java.util.Collections.emptyList();
    }
    public java.util.List tabComplete(org.bukkit.command.CommandSender arg0, java.lang.String arg1, org.bukkit.Location arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.SimpleCommandMap.tabComplete(Lorg/bukkit/command/CommandSender;Ljava/lang/String;Lorg/bukkit/Location;)Ljava/util/List;");
        return java.util.Collections.emptyList();
    }
    public java.util.Collection getCommands() {
        return java.util.Collections.emptyList();
    }
    public void registerServerAliases() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.SimpleCommandMap.registerServerAliases()V");
    }
    public java.util.Map getKnownCommands() {
        return java.util.Collections.emptyMap();
    }
}

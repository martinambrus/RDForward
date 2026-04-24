package org.bukkit.command;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CommandMap {
    void registerAll(java.lang.String arg0, java.util.List arg1);
    boolean register(java.lang.String arg0, java.lang.String arg1, org.bukkit.command.Command arg2);
    boolean register(java.lang.String arg0, org.bukkit.command.Command arg1);
    boolean dispatch(org.bukkit.command.CommandSender arg0, java.lang.String arg1) throws org.bukkit.command.CommandException;
    void clearCommands();
    org.bukkit.command.Command getCommand(java.lang.String arg0);
    java.util.List tabComplete(org.bukkit.command.CommandSender arg0, java.lang.String arg1) throws java.lang.IllegalArgumentException;
    java.util.List tabComplete(org.bukkit.command.CommandSender arg0, java.lang.String arg1, org.bukkit.Location arg2) throws java.lang.IllegalArgumentException;
    java.util.Map getKnownCommands();
}

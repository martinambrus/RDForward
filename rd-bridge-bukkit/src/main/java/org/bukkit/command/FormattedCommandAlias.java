// @rdforward:preserve - hand-tuned: declares buildCommand(String, String[])
// for plugins that reflect against it (Essentials's
// ReflFormattedCommandAliasProvider probes the method via getDeclaredMethod
// and prints a stack trace if absent).
package org.bukkit.command;

@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class FormattedCommandAlias extends org.bukkit.command.Command {
    private final java.lang.String[] formatStrings;

    public FormattedCommandAlias(java.lang.String alias, java.lang.String[] formatStrings) {
        super(alias);
        this.formatStrings = formatStrings == null ? new java.lang.String[0] : formatStrings;
    }

    /** Stub no-arg ctor retained for reflection-based instantiation paths
     *  that some legacy plugins use. */
    public FormattedCommandAlias() {
        super((java.lang.String) null);
        this.formatStrings = new java.lang.String[0];
    }

    public boolean execute(org.bukkit.command.CommandSender arg0, java.lang.String arg1, java.lang.String[] arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.FormattedCommandAlias.execute(Lorg/bukkit/command/CommandSender;Ljava/lang/String;[Ljava/lang/String;)Z");
        return false;
    }

    public java.lang.String getTimingName() {
        return null;
    }

    /** Mirrors CraftBukkit's private {@code buildCommand(String formatString,
     *  String[] args)} that expands {@code $1}/{@code $$2-}/etc. tokens into
     *  the matching {@code args} entries. EssentialsX's
     *  {@code ReflFormattedCommandAliasProvider} reflects against this exact
     *  signature; without it the constructor logs a NoSuchMethodException at
     *  startup. Stub returns the formatString unchanged — sufficient for
     *  Essentials to skip the alias-formatting fast path without crashing. */
    private java.lang.String buildCommand(java.lang.String formatString, java.lang.String[] args) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null,
                "org.bukkit.command.FormattedCommandAlias.buildCommand(Ljava/lang/String;[Ljava/lang/String;)Ljava/lang/String;");
        return formatString;
    }
}

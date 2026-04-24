package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CommandBlock extends org.bukkit.block.TileState, io.papermc.paper.command.CommandBlockHolder {
    java.lang.String getCommand();
    void setCommand(java.lang.String arg0);
    java.lang.String getName();
    void setName(java.lang.String arg0);
    net.kyori.adventure.text.Component name();
    void name(net.kyori.adventure.text.Component arg0);
}

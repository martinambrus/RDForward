package com.destroystokyo.paper.event.brigadier;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class CommandRegisteredEvent extends org.bukkit.event.server.ServerEvent implements org.bukkit.event.Cancellable {
    public CommandRegisteredEvent(java.lang.String arg0, com.destroystokyo.paper.brigadier.BukkitBrigadierCommand arg1, org.bukkit.command.Command arg2, com.mojang.brigadier.tree.RootCommandNode arg3, com.mojang.brigadier.tree.LiteralCommandNode arg4, com.mojang.brigadier.tree.ArgumentCommandNode arg5) {}
    public CommandRegisteredEvent() {}
    public java.lang.String getCommandLabel() {
        return null;
    }
    public com.destroystokyo.paper.brigadier.BukkitBrigadierCommand getBrigadierCommand() {
        return null;
    }
    public org.bukkit.command.Command getCommand() {
        return null;
    }
    public com.mojang.brigadier.tree.RootCommandNode getRoot() {
        return null;
    }
    public com.mojang.brigadier.tree.ArgumentCommandNode getDefaultArgs() {
        return null;
    }
    public com.mojang.brigadier.tree.LiteralCommandNode getLiteral() {
        return null;
    }
    public void setLiteral(com.mojang.brigadier.tree.LiteralCommandNode arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent.setLiteral(Lcom/mojang/brigadier/tree/LiteralCommandNode;)V");
    }
    public boolean isRawCommand() {
        return false;
    }
    public void setRawCommand(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent.setRawCommand(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

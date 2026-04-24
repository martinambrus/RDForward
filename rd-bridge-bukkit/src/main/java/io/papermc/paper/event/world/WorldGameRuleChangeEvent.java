package io.papermc.paper.event.world;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class WorldGameRuleChangeEvent extends org.bukkit.event.world.WorldEvent implements org.bukkit.event.Cancellable {
    public WorldGameRuleChangeEvent(org.bukkit.World arg0, org.bukkit.command.CommandSender arg1, org.bukkit.GameRule arg2, java.lang.String arg3) { super((org.bukkit.World) null); }
    public WorldGameRuleChangeEvent() { super((org.bukkit.World) null); }
    public org.bukkit.command.CommandSender getCommandSender() {
        return null;
    }
    public org.bukkit.GameRule getGameRule() {
        return null;
    }
    public java.lang.String getValue() {
        return null;
    }
    public void setValue(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.world.WorldGameRuleChangeEvent.setValue(Ljava/lang/String;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.world.WorldGameRuleChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

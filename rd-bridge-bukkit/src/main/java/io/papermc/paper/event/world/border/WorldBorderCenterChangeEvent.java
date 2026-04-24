package io.papermc.paper.event.world.border;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class WorldBorderCenterChangeEvent extends io.papermc.paper.event.world.border.WorldBorderEvent implements org.bukkit.event.Cancellable {
    public WorldBorderCenterChangeEvent(org.bukkit.World arg0, org.bukkit.WorldBorder arg1, org.bukkit.Location arg2, org.bukkit.Location arg3) { super((org.bukkit.World) null, (org.bukkit.WorldBorder) null); }
    public WorldBorderCenterChangeEvent() { super((org.bukkit.World) null, (org.bukkit.WorldBorder) null); }
    public org.bukkit.Location getOldCenter() {
        return null;
    }
    public org.bukkit.Location getNewCenter() {
        return null;
    }
    public void setNewCenter(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent.setNewCenter(Lorg/bukkit/Location;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

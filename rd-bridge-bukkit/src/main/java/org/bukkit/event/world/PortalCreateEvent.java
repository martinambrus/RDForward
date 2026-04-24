package org.bukkit.event.world;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PortalCreateEvent extends org.bukkit.event.world.WorldEvent implements org.bukkit.event.Cancellable {
    public PortalCreateEvent(java.util.List arg0, org.bukkit.World arg1, org.bukkit.event.world.PortalCreateEvent$CreateReason arg2) { super((org.bukkit.World) null); }
    public PortalCreateEvent(java.util.List arg0, org.bukkit.World arg1, org.bukkit.entity.Entity arg2, org.bukkit.event.world.PortalCreateEvent$CreateReason arg3) { super((org.bukkit.World) null); }
    public PortalCreateEvent() { super((org.bukkit.World) null); }
    public java.util.List getBlocks() {
        return java.util.Collections.emptyList();
    }
    public org.bukkit.entity.Entity getEntity() {
        return null;
    }
    public org.bukkit.event.world.PortalCreateEvent$CreateReason getReason() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.PortalCreateEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

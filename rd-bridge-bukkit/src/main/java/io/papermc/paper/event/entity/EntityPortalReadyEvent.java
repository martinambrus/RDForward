package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityPortalReadyEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityPortalReadyEvent(org.bukkit.entity.Entity arg0, org.bukkit.World arg1, org.bukkit.PortalType arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityPortalReadyEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.World getTargetWorld() {
        return null;
    }
    public void setTargetWorld(org.bukkit.World arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityPortalReadyEvent.setTargetWorld(Lorg/bukkit/World;)V");
    }
    public org.bukkit.PortalType getPortalType() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityPortalReadyEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

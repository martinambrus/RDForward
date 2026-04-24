package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityMoveEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityMoveEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.Location arg1, org.bukkit.Location arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityMoveEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.LivingEntity getEntity() {
        return null;
    }
    public org.bukkit.Location getFrom() {
        return null;
    }
    public void setFrom(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityMoveEvent.setFrom(Lorg/bukkit/Location;)V");
    }
    public org.bukkit.Location getTo() {
        return null;
    }
    public void setTo(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityMoveEvent.setTo(Lorg/bukkit/Location;)V");
    }
    public boolean hasChangedPosition() {
        return false;
    }
    public boolean hasExplicitlyChangedPosition() {
        return false;
    }
    public boolean hasChangedBlock() {
        return false;
    }
    public boolean hasExplicitlyChangedBlock() {
        return false;
    }
    public boolean hasChangedOrientation() {
        return false;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityMoveEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

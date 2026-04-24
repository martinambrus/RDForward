package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityKnockbackEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityKnockbackEvent(org.bukkit.entity.Entity arg0, io.papermc.paper.event.entity.EntityKnockbackEvent$Cause arg1, org.bukkit.util.Vector arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityKnockbackEvent() { super((org.bukkit.entity.Entity) null); }
    public io.papermc.paper.event.entity.EntityKnockbackEvent$Cause getCause() {
        return null;
    }
    public org.bukkit.util.Vector getKnockback() {
        return null;
    }
    public void setKnockback(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityKnockbackEvent.setKnockback(Lorg/bukkit/util/Vector;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityKnockbackEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

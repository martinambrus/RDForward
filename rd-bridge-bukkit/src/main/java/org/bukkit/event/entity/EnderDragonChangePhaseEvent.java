package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EnderDragonChangePhaseEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EnderDragonChangePhaseEvent(org.bukkit.entity.EnderDragon arg0, org.bukkit.entity.EnderDragon$Phase arg1, org.bukkit.entity.EnderDragon$Phase arg2) { super((org.bukkit.entity.Entity) null); }
    public EnderDragonChangePhaseEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.EnderDragon getEntity() {
        return null;
    }
    public org.bukkit.entity.EnderDragon$Phase getCurrentPhase() {
        return null;
    }
    public org.bukkit.entity.EnderDragon$Phase getNewPhase() {
        return null;
    }
    public void setNewPhase(org.bukkit.entity.EnderDragon$Phase arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EnderDragonChangePhaseEvent.setNewPhase(Lorg/bukkit/entity/EnderDragon$Phase;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EnderDragonChangePhaseEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

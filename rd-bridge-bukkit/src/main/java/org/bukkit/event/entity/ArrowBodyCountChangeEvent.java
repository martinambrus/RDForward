package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ArrowBodyCountChangeEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public ArrowBodyCountChangeEvent(org.bukkit.entity.LivingEntity arg0, int arg1, int arg2, boolean arg3) { super((org.bukkit.entity.Entity) null); }
    public ArrowBodyCountChangeEvent() { super((org.bukkit.entity.Entity) null); }
    public boolean isReset() {
        return false;
    }
    public int getOldAmount() {
        return 0;
    }
    public int getNewAmount() {
        return 0;
    }
    public void setNewAmount(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.ArrowBodyCountChangeEvent.setNewAmount(I)V");
    }
    public org.bukkit.entity.LivingEntity getEntity() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.ArrowBodyCountChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

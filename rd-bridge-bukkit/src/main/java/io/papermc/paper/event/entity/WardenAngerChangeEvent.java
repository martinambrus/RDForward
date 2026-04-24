package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class WardenAngerChangeEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public WardenAngerChangeEvent(org.bukkit.entity.Warden arg0, org.bukkit.entity.Entity arg1, int arg2, int arg3) { super((org.bukkit.entity.Entity) null); }
    public WardenAngerChangeEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Entity getTarget() {
        return null;
    }
    public int getOldAnger() {
        return 0;
    }
    public int getNewAnger() {
        return 0;
    }
    public void setNewAnger(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.WardenAngerChangeEvent.setNewAnger(I)V");
    }
    public org.bukkit.entity.Warden getEntity() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.WardenAngerChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

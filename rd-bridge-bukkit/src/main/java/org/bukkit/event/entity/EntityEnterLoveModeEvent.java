package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityEnterLoveModeEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityEnterLoveModeEvent(org.bukkit.entity.Animals arg0, org.bukkit.entity.HumanEntity arg1, int arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityEnterLoveModeEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Animals getEntity() {
        return null;
    }
    public org.bukkit.entity.HumanEntity getHumanEntity() {
        return null;
    }
    public int getTicksInLove() {
        return 0;
    }
    public void setTicksInLove(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityEnterLoveModeEvent.setTicksInLove(I)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityEnterLoveModeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

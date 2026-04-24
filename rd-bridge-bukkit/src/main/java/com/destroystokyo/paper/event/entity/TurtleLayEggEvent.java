package com.destroystokyo.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class TurtleLayEggEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public TurtleLayEggEvent(org.bukkit.entity.Turtle arg0, org.bukkit.Location arg1, int arg2) { super((org.bukkit.entity.Entity) null); }
    public TurtleLayEggEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Turtle getEntity() {
        return null;
    }
    public org.bukkit.Location getLocation() {
        return null;
    }
    public int getEggCount() {
        return 0;
    }
    public void setEggCount(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.TurtleLayEggEvent.setEggCount(I)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.TurtleLayEggEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

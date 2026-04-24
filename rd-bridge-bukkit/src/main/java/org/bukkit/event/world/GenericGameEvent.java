package org.bukkit.event.world;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class GenericGameEvent extends org.bukkit.event.world.WorldEvent implements org.bukkit.event.Cancellable {
    public GenericGameEvent(org.bukkit.GameEvent arg0, org.bukkit.Location arg1, org.bukkit.entity.Entity arg2, int arg3, boolean arg4) { super((org.bukkit.World) null); }
    public GenericGameEvent() { super((org.bukkit.World) null); }
    public org.bukkit.GameEvent getEvent() {
        return null;
    }
    public org.bukkit.Location getLocation() {
        return null;
    }
    public org.bukkit.entity.Entity getEntity() {
        return null;
    }
    public int getRadius() {
        return 0;
    }
    public void setRadius(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.GenericGameEvent.setRadius(I)V");
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.GenericGameEvent.setCancelled(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

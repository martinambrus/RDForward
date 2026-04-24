package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ItemDespawnEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public ItemDespawnEvent(org.bukkit.entity.Item arg0, org.bukkit.Location arg1) { super((org.bukkit.entity.Entity) null); }
    public ItemDespawnEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Item getEntity() {
        return null;
    }
    public org.bukkit.Location getLocation() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.ItemDespawnEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityResurrectEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityResurrectEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.inventory.EquipmentSlot arg1) { super((org.bukkit.entity.Entity) null); }
    public EntityResurrectEvent(org.bukkit.entity.LivingEntity arg0) { super((org.bukkit.entity.Entity) null); }
    public EntityResurrectEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.LivingEntity getEntity() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityResurrectEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

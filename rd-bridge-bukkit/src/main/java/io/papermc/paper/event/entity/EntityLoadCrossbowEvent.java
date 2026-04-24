package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityLoadCrossbowEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityLoadCrossbowEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.EquipmentSlot arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityLoadCrossbowEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.LivingEntity getEntity() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getCrossbow() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public boolean shouldConsumeItem() {
        return false;
    }
    public void setConsumeItem(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityLoadCrossbowEvent.setConsumeItem(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityLoadCrossbowEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

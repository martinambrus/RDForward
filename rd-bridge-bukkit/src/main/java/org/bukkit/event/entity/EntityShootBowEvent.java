package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityShootBowEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityShootBowEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.entity.Entity arg2, float arg3) { super((org.bukkit.entity.Entity) null); }
    public EntityShootBowEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.ItemStack arg2, org.bukkit.entity.Entity arg3, float arg4) { super((org.bukkit.entity.Entity) null); }
    public EntityShootBowEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.ItemStack arg2, org.bukkit.entity.Entity arg3, org.bukkit.inventory.EquipmentSlot arg4, float arg5, boolean arg6) { super((org.bukkit.entity.Entity) null); }
    public EntityShootBowEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.LivingEntity getEntity() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getBow() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getConsumable() {
        return null;
    }
    public org.bukkit.entity.Entity getProjectile() {
        return null;
    }
    public void setProjectile(org.bukkit.entity.Entity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityShootBowEvent.setProjectile(Lorg/bukkit/entity/Entity;)V");
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public float getForce() {
        return 0.0f;
    }
    public void setConsumeItem(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityShootBowEvent.setConsumeItem(Z)V");
    }
    public boolean shouldConsumeItem() {
        return false;
    }
    public org.bukkit.inventory.ItemStack getArrowItem() {
        return null;
    }
    public void setConsumeArrow(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityShootBowEvent.setConsumeArrow(Z)V");
    }
    public boolean getConsumeArrow() {
        return false;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityShootBowEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

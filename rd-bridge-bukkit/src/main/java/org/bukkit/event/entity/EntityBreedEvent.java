package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityBreedEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityBreedEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.entity.LivingEntity arg1, org.bukkit.entity.LivingEntity arg2, org.bukkit.entity.LivingEntity arg3, org.bukkit.inventory.ItemStack arg4, int arg5) { super((org.bukkit.entity.Entity) null); }
    public EntityBreedEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.LivingEntity getEntity() {
        return null;
    }
    public org.bukkit.entity.LivingEntity getMother() {
        return null;
    }
    public org.bukkit.entity.LivingEntity getFather() {
        return null;
    }
    public org.bukkit.entity.LivingEntity getBreeder() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getBredWith() {
        return null;
    }
    public int getExperience() {
        return 0;
    }
    public void setExperience(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityBreedEvent.setExperience(I)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityBreedEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

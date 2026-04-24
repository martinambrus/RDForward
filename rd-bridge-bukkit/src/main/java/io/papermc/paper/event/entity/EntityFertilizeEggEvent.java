package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityFertilizeEggEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityFertilizeEggEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.entity.LivingEntity arg1, org.bukkit.entity.Player arg2, org.bukkit.inventory.ItemStack arg3, int arg4) { super((org.bukkit.entity.Entity) null); }
    public EntityFertilizeEggEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.LivingEntity getEntity() {
        return null;
    }
    public org.bukkit.entity.LivingEntity getMother() {
        return null;
    }
    public org.bukkit.entity.LivingEntity getFather() {
        return null;
    }
    public org.bukkit.entity.Player getBreeder() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getBredWith() {
        return null;
    }
    public int getExperience() {
        return 0;
    }
    public void setExperience(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityFertilizeEggEvent.setExperience(I)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityFertilizeEggEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

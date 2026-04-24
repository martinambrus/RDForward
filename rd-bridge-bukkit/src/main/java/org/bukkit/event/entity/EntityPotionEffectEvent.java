package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityPotionEffectEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityPotionEffectEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.potion.PotionEffect arg1, org.bukkit.potion.PotionEffect arg2, org.bukkit.event.entity.EntityPotionEffectEvent$Cause arg3, org.bukkit.event.entity.EntityPotionEffectEvent$Action arg4, boolean arg5) { super((org.bukkit.entity.Entity) null); }
    public EntityPotionEffectEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.potion.PotionEffect getOldEffect() {
        return null;
    }
    public org.bukkit.potion.PotionEffect getNewEffect() {
        return null;
    }
    public org.bukkit.event.entity.EntityPotionEffectEvent$Cause getCause() {
        return null;
    }
    public org.bukkit.event.entity.EntityPotionEffectEvent$Action getAction() {
        return null;
    }
    public org.bukkit.potion.PotionEffectType getModifiedType() {
        return null;
    }
    public boolean isOverride() {
        return false;
    }
    public void setOverride(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityPotionEffectEvent.setOverride(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityPotionEffectEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

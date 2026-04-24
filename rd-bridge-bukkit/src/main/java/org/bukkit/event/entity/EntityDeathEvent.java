package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityDeathEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityDeathEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.damage.DamageSource arg1, java.util.List arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityDeathEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.damage.DamageSource arg1, java.util.List arg2, int arg3) { super((org.bukkit.entity.Entity) null); }
    public EntityDeathEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.LivingEntity getEntity() {
        return null;
    }
    public org.bukkit.damage.DamageSource getDamageSource() {
        return null;
    }
    public int getDroppedExp() {
        return 0;
    }
    public void setDroppedExp(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDeathEvent.setDroppedExp(I)V");
    }
    public java.util.List getDrops() {
        return java.util.Collections.emptyList();
    }
    public double getReviveHealth() {
        return 0.0;
    }
    public void setReviveHealth(double arg0) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDeathEvent.setReviveHealth(D)V");
    }
    public boolean shouldPlayDeathSound() {
        return false;
    }
    public void setShouldPlayDeathSound(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDeathEvent.setShouldPlayDeathSound(Z)V");
    }
    public org.bukkit.Sound getDeathSound() {
        return null;
    }
    public void setDeathSound(org.bukkit.Sound arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDeathEvent.setDeathSound(Lorg/bukkit/Sound;)V");
    }
    public org.bukkit.SoundCategory getDeathSoundCategory() {
        return null;
    }
    public void setDeathSoundCategory(org.bukkit.SoundCategory arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDeathEvent.setDeathSoundCategory(Lorg/bukkit/SoundCategory;)V");
    }
    public float getDeathSoundVolume() {
        return 0.0f;
    }
    public void setDeathSoundVolume(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDeathEvent.setDeathSoundVolume(F)V");
    }
    public float getDeathSoundPitch() {
        return 0.0f;
    }
    public void setDeathSoundPitch(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDeathEvent.setDeathSoundPitch(F)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDeathEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

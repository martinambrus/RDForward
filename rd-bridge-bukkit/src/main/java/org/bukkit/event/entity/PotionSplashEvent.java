package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PotionSplashEvent extends org.bukkit.event.entity.ProjectileHitEvent implements org.bukkit.event.Cancellable {
    public PotionSplashEvent(org.bukkit.entity.ThrownPotion arg0, java.util.Map arg1) { super((org.bukkit.entity.Projectile) null, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null); }
    public PotionSplashEvent(org.bukkit.entity.ThrownPotion arg0, org.bukkit.entity.Entity arg1, org.bukkit.block.Block arg2, org.bukkit.block.BlockFace arg3, java.util.Map arg4) { super((org.bukkit.entity.Projectile) null, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null); }
    public PotionSplashEvent() { super((org.bukkit.entity.Projectile) null, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null); }
    public org.bukkit.entity.ThrownPotion getEntity() {
        return null;
    }
    public org.bukkit.entity.ThrownPotion getPotion() {
        return null;
    }
    public java.util.Collection getAffectedEntities() {
        return java.util.Collections.emptyList();
    }
    public double getIntensity(org.bukkit.entity.LivingEntity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PotionSplashEvent.getIntensity(Lorg/bukkit/entity/LivingEntity;)D");
        return 0.0;
    }
    public void setIntensity(org.bukkit.entity.LivingEntity arg0, double arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PotionSplashEvent.setIntensity(Lorg/bukkit/entity/LivingEntity;D)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PotionSplashEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

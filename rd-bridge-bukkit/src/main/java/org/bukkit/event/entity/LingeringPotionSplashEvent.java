package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class LingeringPotionSplashEvent extends org.bukkit.event.entity.ProjectileHitEvent implements org.bukkit.event.Cancellable {
    public LingeringPotionSplashEvent(org.bukkit.entity.ThrownPotion arg0, org.bukkit.entity.AreaEffectCloud arg1) { super((org.bukkit.entity.Projectile) null, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null); }
    public LingeringPotionSplashEvent(org.bukkit.entity.ThrownPotion arg0, org.bukkit.entity.Entity arg1, org.bukkit.block.Block arg2, org.bukkit.block.BlockFace arg3, org.bukkit.entity.AreaEffectCloud arg4) { super((org.bukkit.entity.Projectile) null, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null); }
    public LingeringPotionSplashEvent() { super((org.bukkit.entity.Projectile) null, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null); }
    public org.bukkit.entity.ThrownPotion getEntity() {
        return null;
    }
    public org.bukkit.entity.AreaEffectCloud getAreaEffectCloud() {
        return null;
    }
    public void allowsEmptyCreation(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.LingeringPotionSplashEvent.allowsEmptyCreation(Z)V");
    }
    public boolean allowsEmptyCreation() {
        return false;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.LingeringPotionSplashEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

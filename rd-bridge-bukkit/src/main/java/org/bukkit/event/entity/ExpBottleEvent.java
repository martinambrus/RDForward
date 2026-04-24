package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ExpBottleEvent extends org.bukkit.event.entity.ProjectileHitEvent {
    public ExpBottleEvent(org.bukkit.entity.ThrownExpBottle arg0, int arg1) { super((org.bukkit.entity.Projectile) null, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null); }
    public ExpBottleEvent(org.bukkit.entity.ThrownExpBottle arg0, org.bukkit.entity.Entity arg1, org.bukkit.block.Block arg2, org.bukkit.block.BlockFace arg3, int arg4) { super((org.bukkit.entity.Projectile) null, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null); }
    public ExpBottleEvent() { super((org.bukkit.entity.Projectile) null, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null); }
    public org.bukkit.entity.ThrownExpBottle getEntity() {
        return null;
    }
    public boolean getShowEffect() {
        return false;
    }
    public void setShowEffect(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.ExpBottleEvent.setShowEffect(Z)V");
    }
    public int getExperience() {
        return 0;
    }
    public void setExperience(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.ExpBottleEvent.setExperience(I)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

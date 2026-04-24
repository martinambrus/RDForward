package io.papermc.paper.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class TargetHitEvent extends org.bukkit.event.entity.ProjectileHitEvent {
    public TargetHitEvent(org.bukkit.entity.Projectile arg0, org.bukkit.block.Block arg1, org.bukkit.block.BlockFace arg2, int arg3) { super((org.bukkit.entity.Projectile) null, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null); }
    public TargetHitEvent() { super((org.bukkit.entity.Projectile) null, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null); }
    public int getSignalStrength() {
        return 0;
    }
    public void setSignalStrength(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.block.TargetHitEvent.setSignalStrength(I)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

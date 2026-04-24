package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockExplodeEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BlockExplodeEvent(org.bukkit.block.Block arg0, org.bukkit.block.BlockState arg1, java.util.List arg2, float arg3, org.bukkit.ExplosionResult arg4) { super((org.bukkit.block.Block) null); }
    public BlockExplodeEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.ExplosionResult getExplosionResult() {
        return null;
    }
    public org.bukkit.block.BlockState getExplodedBlockState() {
        return null;
    }
    public java.util.List blockList() {
        return java.util.Collections.emptyList();
    }
    public float getYield() {
        return 0.0f;
    }
    public void setYield(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockExplodeEvent.setYield(F)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockExplodeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

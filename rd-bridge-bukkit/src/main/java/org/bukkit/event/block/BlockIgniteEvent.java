package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockIgniteEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BlockIgniteEvent(org.bukkit.block.Block arg0, org.bukkit.event.block.BlockIgniteEvent$IgniteCause arg1, org.bukkit.entity.Entity arg2) { super((org.bukkit.block.Block) null); }
    public BlockIgniteEvent(org.bukkit.block.Block arg0, org.bukkit.event.block.BlockIgniteEvent$IgniteCause arg1, org.bukkit.block.Block arg2) { super((org.bukkit.block.Block) null); }
    public BlockIgniteEvent(org.bukkit.block.Block arg0, org.bukkit.event.block.BlockIgniteEvent$IgniteCause arg1, org.bukkit.entity.Entity arg2, org.bukkit.block.Block arg3) { super((org.bukkit.block.Block) null); }
    public BlockIgniteEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.event.block.BlockIgniteEvent$IgniteCause getCause() {
        return null;
    }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public org.bukkit.entity.Entity getIgnitingEntity() {
        return null;
    }
    public org.bukkit.block.Block getIgnitingBlock() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockIgniteEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

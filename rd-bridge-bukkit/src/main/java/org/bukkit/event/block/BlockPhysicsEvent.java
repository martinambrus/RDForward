package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockPhysicsEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BlockPhysicsEvent(org.bukkit.block.Block arg0, org.bukkit.block.data.BlockData arg1, int arg2, int arg3, int arg4) { super((org.bukkit.block.Block) null); }
    public BlockPhysicsEvent(org.bukkit.block.Block arg0, org.bukkit.block.data.BlockData arg1) { super((org.bukkit.block.Block) null); }
    public BlockPhysicsEvent(org.bukkit.block.Block arg0, org.bukkit.block.data.BlockData arg1, org.bukkit.block.Block arg2) { super((org.bukkit.block.Block) null); }
    public BlockPhysicsEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.block.Block getSourceBlock() {
        return null;
    }
    public org.bukkit.Material getChangedType() {
        return null;
    }
    public org.bukkit.block.data.BlockData getChangedBlockData() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockPhysicsEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

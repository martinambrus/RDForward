package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockBurnEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BlockBurnEvent(org.bukkit.block.Block arg0) { super((org.bukkit.block.Block) null); }
    public BlockBurnEvent(org.bukkit.block.Block arg0, org.bukkit.block.Block arg1) { super((org.bukkit.block.Block) null); }
    public BlockBurnEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.block.Block getIgnitingBlock() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockBurnEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockDropItemEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BlockDropItemEvent(org.bukkit.block.Block arg0, org.bukkit.block.BlockState arg1, org.bukkit.entity.Player arg2, java.util.List arg3) { super((org.bukkit.block.Block) null); }
    public BlockDropItemEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.block.BlockState getBlockState() {
        return null;
    }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public java.util.List getItems() {
        return java.util.Collections.emptyList();
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockDropItemEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

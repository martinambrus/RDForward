package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockFertilizeEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BlockFertilizeEvent(org.bukkit.block.Block arg0, org.bukkit.entity.Player arg1, java.util.List arg2) { super((org.bukkit.block.Block) null); }
    public BlockFertilizeEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public java.util.List getBlocks() {
        return java.util.Collections.emptyList();
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockFertilizeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

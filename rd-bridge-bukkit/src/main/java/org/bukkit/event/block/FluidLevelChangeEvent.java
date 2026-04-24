package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class FluidLevelChangeEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public FluidLevelChangeEvent(org.bukkit.block.Block arg0, org.bukkit.block.data.BlockData arg1) { super((org.bukkit.block.Block) null); }
    public FluidLevelChangeEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.block.data.BlockData getNewData() {
        return null;
    }
    public void setNewData(org.bukkit.block.data.BlockData arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.FluidLevelChangeEvent.setNewData(Lorg/bukkit/block/data/BlockData;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.FluidLevelChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

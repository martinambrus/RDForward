package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockCanBuildEvent extends org.bukkit.event.block.BlockEvent {
    public BlockCanBuildEvent(org.bukkit.block.Block arg0, org.bukkit.block.data.BlockData arg1, boolean arg2) { super((org.bukkit.block.Block) null); }
    public BlockCanBuildEvent(org.bukkit.block.Block arg0, org.bukkit.entity.Player arg1, org.bukkit.block.data.BlockData arg2, boolean arg3) { super((org.bukkit.block.Block) null); }
    public BlockCanBuildEvent(org.bukkit.block.Block arg0, org.bukkit.entity.Player arg1, org.bukkit.block.data.BlockData arg2, boolean arg3, org.bukkit.inventory.EquipmentSlot arg4) { super((org.bukkit.block.Block) null); }
    public BlockCanBuildEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public org.bukkit.Material getMaterial() {
        return null;
    }
    public org.bukkit.block.data.BlockData getBlockData() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public boolean isBuildable() {
        return false;
    }
    public void setBuildable(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockCanBuildEvent.setBuildable(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

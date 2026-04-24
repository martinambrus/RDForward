package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockShearEntityEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BlockShearEntityEvent(org.bukkit.block.Block arg0, org.bukkit.entity.Entity arg1, org.bukkit.inventory.ItemStack arg2, java.util.List arg3) { super((org.bukkit.block.Block) null); }
    public BlockShearEntityEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.entity.Entity getEntity() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getTool() {
        return null;
    }
    public java.util.List getDrops() {
        return java.util.Collections.emptyList();
    }
    public void setDrops(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockShearEntityEvent.setDrops(Ljava/util/List;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockShearEntityEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

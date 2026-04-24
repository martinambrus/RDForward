package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockDispenseEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BlockDispenseEvent(org.bukkit.block.Block arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.util.Vector arg2) { super((org.bukkit.block.Block) null); }
    public BlockDispenseEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public void setItem(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockDispenseEvent.setItem(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public org.bukkit.util.Vector getVelocity() {
        return null;
    }
    public void setVelocity(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockDispenseEvent.setVelocity(Lorg/bukkit/util/Vector;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockDispenseEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

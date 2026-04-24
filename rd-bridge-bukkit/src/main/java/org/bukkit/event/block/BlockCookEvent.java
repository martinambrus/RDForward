package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockCookEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BlockCookEvent(org.bukkit.block.Block arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.ItemStack arg2) { super((org.bukkit.block.Block) null); }
    public BlockCookEvent(org.bukkit.block.Block arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.ItemStack arg2, org.bukkit.inventory.CookingRecipe arg3) { super((org.bukkit.block.Block) null); }
    public BlockCookEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.inventory.ItemStack getSource() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getResult() {
        return null;
    }
    public void setResult(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockCookEvent.setResult(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public org.bukkit.inventory.CookingRecipe getRecipe() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockCookEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

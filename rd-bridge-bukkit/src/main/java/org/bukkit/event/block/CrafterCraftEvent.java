package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class CrafterCraftEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public CrafterCraftEvent(org.bukkit.block.Block arg0, org.bukkit.inventory.CraftingRecipe arg1, org.bukkit.inventory.ItemStack arg2) { super((org.bukkit.block.Block) null); }
    public CrafterCraftEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.inventory.ItemStack getResult() {
        return null;
    }
    public void setResult(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.CrafterCraftEvent.setResult(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public org.bukkit.inventory.CraftingRecipe getRecipe() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.CrafterCraftEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

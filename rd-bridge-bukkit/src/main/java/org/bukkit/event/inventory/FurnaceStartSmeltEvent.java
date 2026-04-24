package org.bukkit.event.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class FurnaceStartSmeltEvent extends org.bukkit.event.block.InventoryBlockStartEvent {
    public FurnaceStartSmeltEvent(org.bukkit.block.Block arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.CookingRecipe arg2) { super((org.bukkit.block.Block) null, (org.bukkit.inventory.ItemStack) null); }
    public FurnaceStartSmeltEvent(org.bukkit.block.Block arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.CookingRecipe arg2, int arg3) { super((org.bukkit.block.Block) null, (org.bukkit.inventory.ItemStack) null); }
    public FurnaceStartSmeltEvent() { super((org.bukkit.block.Block) null, (org.bukkit.inventory.ItemStack) null); }
    public org.bukkit.inventory.CookingRecipe getRecipe() {
        return null;
    }
    public int getTotalCookTime() {
        return 0;
    }
    public void setTotalCookTime(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.FurnaceStartSmeltEvent.setTotalCookTime(I)V");
    }
}

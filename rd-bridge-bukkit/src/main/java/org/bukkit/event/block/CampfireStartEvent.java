package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class CampfireStartEvent extends org.bukkit.event.block.InventoryBlockStartEvent {
    public CampfireStartEvent(org.bukkit.block.Block arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.CampfireRecipe arg2) { super((org.bukkit.block.Block) null, (org.bukkit.inventory.ItemStack) null); }
    public CampfireStartEvent() { super((org.bukkit.block.Block) null, (org.bukkit.inventory.ItemStack) null); }
    public org.bukkit.inventory.CampfireRecipe getRecipe() {
        return null;
    }
    public int getTotalCookTime() {
        return 0;
    }
    public void setTotalCookTime(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.CampfireStartEvent.setTotalCookTime(I)V");
    }
}

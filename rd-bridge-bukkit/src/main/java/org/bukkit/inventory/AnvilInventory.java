package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface AnvilInventory extends org.bukkit.inventory.Inventory {
    java.lang.String getRenameText();
    int getRepairCostAmount();
    void setRepairCostAmount(int arg0);
    int getRepairCost();
    void setRepairCost(int arg0);
    int getMaximumRepairCost();
    void setMaximumRepairCost(int arg0);
    default org.bukkit.inventory.ItemStack getFirstItem() {
        return null;
    }
    default void setFirstItem(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.AnvilInventory.setFirstItem(Lorg/bukkit/inventory/ItemStack;)V");
    }
    default org.bukkit.inventory.ItemStack getSecondItem() {
        return null;
    }
    default void setSecondItem(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.AnvilInventory.setSecondItem(Lorg/bukkit/inventory/ItemStack;)V");
    }
    default org.bukkit.inventory.ItemStack getResult() {
        return null;
    }
    default void setResult(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.AnvilInventory.setResult(Lorg/bukkit/inventory/ItemStack;)V");
    }
}

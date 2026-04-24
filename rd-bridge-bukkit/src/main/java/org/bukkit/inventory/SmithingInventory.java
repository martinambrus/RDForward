package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SmithingInventory extends org.bukkit.inventory.Inventory {
    org.bukkit.inventory.ItemStack getResult();
    void setResult(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.Recipe getRecipe();
    default org.bukkit.inventory.ItemStack getInputTemplate() {
        return null;
    }
    default void setInputTemplate(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.SmithingInventory.setInputTemplate(Lorg/bukkit/inventory/ItemStack;)V");
    }
    default org.bukkit.inventory.ItemStack getInputEquipment() {
        return null;
    }
    default void setInputEquipment(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.SmithingInventory.setInputEquipment(Lorg/bukkit/inventory/ItemStack;)V");
    }
    default org.bukkit.inventory.ItemStack getInputMineral() {
        return null;
    }
    default void setInputMineral(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.SmithingInventory.setInputMineral(Lorg/bukkit/inventory/ItemStack;)V");
    }
}

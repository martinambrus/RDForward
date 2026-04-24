package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface LecternInventory extends org.bukkit.inventory.Inventory {
    org.bukkit.block.Lectern getHolder();
    default org.bukkit.inventory.ItemStack getBook() {
        return null;
    }
    default void setBook(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.LecternInventory.setBook(Lorg/bukkit/inventory/ItemStack;)V");
    }
}

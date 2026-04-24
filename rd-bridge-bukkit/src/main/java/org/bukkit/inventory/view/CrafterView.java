package org.bukkit.inventory.view;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CrafterView extends org.bukkit.inventory.InventoryView {
    org.bukkit.inventory.CrafterInventory getTopInventory();
    boolean isSlotDisabled(int arg0);
    boolean isPowered();
    void setSlotDisabled(int arg0, boolean arg1);
}

package org.bukkit.event.enchantment;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EnchantItemEvent extends org.bukkit.event.inventory.InventoryEvent implements org.bukkit.event.Cancellable {
    public EnchantItemEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.InventoryView arg1, org.bukkit.block.Block arg2, org.bukkit.inventory.ItemStack arg3, int arg4, java.util.Map arg5, org.bukkit.enchantments.Enchantment arg6, int arg7, int arg8) { super((org.bukkit.inventory.InventoryView) null); }
    public EnchantItemEvent() { super((org.bukkit.inventory.InventoryView) null); }
    public org.bukkit.entity.Player getEnchanter() {
        return null;
    }
    public org.bukkit.block.Block getEnchantBlock() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public void setItem(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.enchantment.EnchantItemEvent.setItem(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public int getExpLevelCost() {
        return 0;
    }
    public void setExpLevelCost(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.enchantment.EnchantItemEvent.setExpLevelCost(I)V");
    }
    public java.util.Map getEnchantsToAdd() {
        return java.util.Collections.emptyMap();
    }
    public org.bukkit.enchantments.Enchantment getEnchantmentHint() {
        return null;
    }
    public int getLevelHint() {
        return 0;
    }
    public int whichButton() {
        return 0;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.enchantment.EnchantItemEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

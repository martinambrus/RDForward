package org.bukkit.event.enchantment;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PrepareItemEnchantEvent extends org.bukkit.event.inventory.InventoryEvent implements org.bukkit.event.Cancellable {
    public PrepareItemEnchantEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.view.EnchantmentView arg1, org.bukkit.block.Block arg2, org.bukkit.inventory.ItemStack arg3, org.bukkit.enchantments.EnchantmentOffer[] arg4, int arg5) { super((org.bukkit.inventory.InventoryView) null); }
    public PrepareItemEnchantEvent() { super((org.bukkit.inventory.InventoryView) null); }
    public org.bukkit.inventory.view.EnchantmentView getView() {
        return null;
    }
    public org.bukkit.entity.Player getEnchanter() {
        return null;
    }
    public org.bukkit.block.Block getEnchantBlock() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public int[] getExpLevelCostsOffered() {
        return new int[0];
    }
    public org.bukkit.enchantments.EnchantmentOffer[] getOffers() {
        return new org.bukkit.enchantments.EnchantmentOffer[0];
    }
    public int getEnchantmentBonus() {
        return 0;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.enchantment.PrepareItemEnchantEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

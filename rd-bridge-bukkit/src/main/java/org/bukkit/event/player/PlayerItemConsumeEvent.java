package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerItemConsumeEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerItemConsumeEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.EquipmentSlot arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerItemConsumeEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.ItemStack arg1) { super((org.bukkit.entity.Player) null); }
    public PlayerItemConsumeEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public void setItem(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerItemConsumeEvent.setItem(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getReplacement() {
        return null;
    }
    public void setReplacement(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerItemConsumeEvent.setReplacement(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerItemConsumeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

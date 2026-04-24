package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerItemMendEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerItemMendEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.EquipmentSlot arg2, org.bukkit.entity.ExperienceOrb arg3, int arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerItemMendEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.EquipmentSlot arg2, org.bukkit.entity.ExperienceOrb arg3, int arg4, int arg5) { super((org.bukkit.entity.Player) null); }
    public PlayerItemMendEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.entity.ExperienceOrb arg2, int arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerItemMendEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getSlot() {
        return null;
    }
    public org.bukkit.entity.ExperienceOrb getExperienceOrb() {
        return null;
    }
    public int getRepairAmount() {
        return 0;
    }
    public void setRepairAmount(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerItemMendEvent.setRepairAmount(I)V");
    }
    public int getConsumedExperience() {
        return 0;
    }
    public java.util.function.IntUnaryOperator getDurabilityToXpOperation() {
        return null;
    }
    public void setDurabilityToXpOperation(java.util.function.IntUnaryOperator arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerItemMendEvent.setDurabilityToXpOperation(Ljava/util/function/IntUnaryOperator;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerItemMendEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

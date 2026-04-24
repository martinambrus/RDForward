package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerInventorySlotChangeEvent extends org.bukkit.event.player.PlayerEvent {
    public PlayerInventorySlotChangeEvent(org.bukkit.entity.Player arg0, int arg1, org.bukkit.inventory.ItemStack arg2, org.bukkit.inventory.ItemStack arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerInventorySlotChangeEvent() { super((org.bukkit.entity.Player) null); }
    public int getRawSlot() {
        return 0;
    }
    public int getSlot() {
        return 0;
    }
    public org.bukkit.inventory.ItemStack getOldItemStack() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getNewItemStack() {
        return null;
    }
    public boolean shouldTriggerAdvancements() {
        return false;
    }
    public void setShouldTriggerAdvancements(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerInventorySlotChangeEvent.setShouldTriggerAdvancements(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

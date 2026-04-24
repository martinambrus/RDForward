package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerFlowerPotManipulateEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerFlowerPotManipulateEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Block arg1, org.bukkit.inventory.ItemStack arg2, boolean arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerFlowerPotManipulateEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.block.Block getFlowerpot() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public boolean isPlacing() {
        return false;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

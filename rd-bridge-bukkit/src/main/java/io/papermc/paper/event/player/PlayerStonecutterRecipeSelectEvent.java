package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerStonecutterRecipeSelectEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerStonecutterRecipeSelectEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.StonecutterInventory arg1, org.bukkit.inventory.StonecuttingRecipe arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerStonecutterRecipeSelectEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.inventory.StonecutterInventory getStonecutterInventory() {
        return null;
    }
    public org.bukkit.inventory.StonecuttingRecipe getStonecuttingRecipe() {
        return null;
    }
    public void setStonecuttingRecipe(org.bukkit.inventory.StonecuttingRecipe arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent.setStonecuttingRecipe(Lorg/bukkit/inventory/StonecuttingRecipe;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerRecipeDiscoverEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerRecipeDiscoverEvent(org.bukkit.entity.Player arg0, org.bukkit.NamespacedKey arg1, boolean arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerRecipeDiscoverEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.NamespacedKey getRecipe() {
        return null;
    }
    public boolean shouldShowNotification() {
        return false;
    }
    public void shouldShowNotification(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerRecipeDiscoverEvent.shouldShowNotification(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerRecipeDiscoverEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

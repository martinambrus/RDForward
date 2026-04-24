package com.destroystokyo.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerRecipeBookClickEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerRecipeBookClickEvent(org.bukkit.entity.Player arg0, org.bukkit.NamespacedKey arg1, boolean arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerRecipeBookClickEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.NamespacedKey getRecipe() {
        return null;
    }
    public void setRecipe(org.bukkit.NamespacedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent.setRecipe(Lorg/bukkit/NamespacedKey;)V");
    }
    public boolean isMakeAll() {
        return false;
    }
    public void setMakeAll(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent.setMakeAll(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

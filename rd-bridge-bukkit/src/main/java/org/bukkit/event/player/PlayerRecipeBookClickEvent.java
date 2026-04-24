package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerRecipeBookClickEvent extends org.bukkit.event.player.PlayerEvent {
    public PlayerRecipeBookClickEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.Recipe arg1, boolean arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerRecipeBookClickEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.inventory.Recipe getOriginalRecipe() {
        return null;
    }
    public org.bukkit.inventory.Recipe getRecipe() {
        return null;
    }
    public void setRecipe(org.bukkit.inventory.Recipe arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerRecipeBookClickEvent.setRecipe(Lorg/bukkit/inventory/Recipe;)V");
    }
    public boolean isShiftClick() {
        return false;
    }
    public void setShiftClick(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerRecipeBookClickEvent.setShiftClick(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

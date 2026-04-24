package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerPurchaseEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerPurchaseEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.Merchant arg1, org.bukkit.inventory.MerchantRecipe arg2, boolean arg3, boolean arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerPurchaseEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.inventory.Merchant getMerchant() {
        return null;
    }
    public org.bukkit.inventory.MerchantRecipe getTrade() {
        return null;
    }
    public void setTrade(org.bukkit.inventory.MerchantRecipe arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerPurchaseEvent.setTrade(Lorg/bukkit/inventory/MerchantRecipe;)V");
    }
    public boolean isRewardingExp() {
        return false;
    }
    public void setRewardExp(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerPurchaseEvent.setRewardExp(Z)V");
    }
    public boolean willIncreaseTradeUses() {
        return false;
    }
    public void setIncreaseTradeUses(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerPurchaseEvent.setIncreaseTradeUses(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerPurchaseEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerToggleEntityAgeLockEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerToggleEntityAgeLockEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.LivingEntity arg1, org.bukkit.inventory.ItemStack arg2, org.bukkit.inventory.EquipmentSlot arg3, boolean arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerToggleEntityAgeLockEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.entity.LivingEntity getEntity() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public boolean isAgeLocked() {
        return false;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerToggleEntityAgeLockEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerFishEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerFishEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.Entity arg1, org.bukkit.entity.FishHook arg2, org.bukkit.inventory.EquipmentSlot arg3, org.bukkit.event.player.PlayerFishEvent$State arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerFishEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.Entity arg1, org.bukkit.entity.FishHook arg2, org.bukkit.event.player.PlayerFishEvent$State arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerFishEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.entity.Entity getCaught() {
        return null;
    }
    public org.bukkit.entity.FishHook getHook() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public org.bukkit.event.player.PlayerFishEvent$State getState() {
        return null;
    }
    public int getExpToDrop() {
        return 0;
    }
    public void setExpToDrop(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerFishEvent.setExpToDrop(I)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerFishEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

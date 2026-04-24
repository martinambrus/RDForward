package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerUnleashEntityEvent extends org.bukkit.event.entity.EntityUnleashEvent implements org.bukkit.event.Cancellable {
    public PlayerUnleashEntityEvent(org.bukkit.entity.Entity arg0, org.bukkit.entity.Player arg1, org.bukkit.inventory.EquipmentSlot arg2, boolean arg3) { super((org.bukkit.entity.Entity) null, (org.bukkit.event.entity.EntityUnleashEvent$UnleashReason) null, false); }
    public PlayerUnleashEntityEvent(org.bukkit.entity.Entity arg0, org.bukkit.entity.Player arg1, org.bukkit.inventory.EquipmentSlot arg2) { super((org.bukkit.entity.Entity) null, (org.bukkit.event.entity.EntityUnleashEvent$UnleashReason) null, false); }
    public PlayerUnleashEntityEvent(org.bukkit.entity.Entity arg0, org.bukkit.entity.Player arg1) { super((org.bukkit.entity.Entity) null, (org.bukkit.event.entity.EntityUnleashEvent$UnleashReason) null, false); }
    public PlayerUnleashEntityEvent() { super((org.bukkit.entity.Entity) null, (org.bukkit.event.entity.EntityUnleashEvent$UnleashReason) null, false); }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerUnleashEntityEvent.setCancelled(Z)V");
    }
}

package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityPlaceEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityPlaceEvent(org.bukkit.entity.Entity arg0, org.bukkit.entity.Player arg1, org.bukkit.block.Block arg2, org.bukkit.block.BlockFace arg3, org.bukkit.inventory.EquipmentSlot arg4) { super((org.bukkit.entity.Entity) null); }
    public EntityPlaceEvent(org.bukkit.entity.Entity arg0, org.bukkit.entity.Player arg1, org.bukkit.block.Block arg2, org.bukkit.block.BlockFace arg3) { super((org.bukkit.entity.Entity) null); }
    public EntityPlaceEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public org.bukkit.block.Block getBlock() {
        return null;
    }
    public org.bukkit.block.BlockFace getBlockFace() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityPlaceEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

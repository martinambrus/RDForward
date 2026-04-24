package com.destroystokyo.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerUseUnknownEntityEvent extends org.bukkit.event.player.PlayerEvent {
    public PlayerUseUnknownEntityEvent(org.bukkit.entity.Player arg0, int arg1, boolean arg2, org.bukkit.inventory.EquipmentSlot arg3, org.bukkit.util.Vector arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerUseUnknownEntityEvent() { super((org.bukkit.entity.Player) null); }
    public int getEntityId() {
        return 0;
    }
    public boolean isAttack() {
        return false;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public org.bukkit.util.Vector getClickedRelativePosition() {
        return null;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}

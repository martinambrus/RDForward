package com.destroystokyo.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerLaunchProjectileEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerLaunchProjectileEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.entity.Projectile arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerLaunchProjectileEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.entity.Projectile getProjectile() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getItemStack() {
        return null;
    }
    public boolean shouldConsume() {
        return false;
    }
    public void setShouldConsume(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent.setShouldConsume(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
